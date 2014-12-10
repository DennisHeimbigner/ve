/*
This software is released uder the Licence terms
described in the file LICENSE.txt.
*/

#include "config.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "ve.h"
#include "velist.h"
#include "veinternal.h"

/**************************************************/
/* Constants*/

#define MAXTOKENSIZE 4096

/**
 * Significant Characters
 */
#define ESCAPE '\\'
#define COMMENTCHAR '#'
#define NULCHAR '\0'
#define LBRACE '{'
#define RBRACE '}'
#define LPAREN '('
#define RPAREN ')'

#define WORDSTRING1 "abcdefghijklmnopqrstuvwxyz"\
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" \
        "0123456789" \
        "_$+-"

/**
 * Legal first char of a word
 */

static const char* WORDCHAR1 = WORDSTRING1;

/**
 * Legal non-first char of a word
 */
static const char* WORDCHARN = WORDSTRING1 ".";

/**
 * End-of-string marker
 */
#define EOS NULCHAR

/* Use integers instead of enum*/
#define TOKEN_EOF 0 
#define TOKEN_NONE -1
#define TOKEN_WORD -2
#define TOKEN_STRING -3
#define TOKEN_NUMBER -4

typedef int VEparenstate;
#define UNDEF 0
#define LEFT 1
#define RIGHT 2

/* forward */
static char* dumptoken(int token, char* lval);
static void reportposition(VEposition yypos);
static VEerror parseR(VEparser* vep, VElist* actions);
static VEerror collectargs(VEaction*, VEparser*, int arity, VEarg* argv);

static int compatible(VEargtype argtype, VEargtype sig);

/**********************************************/
/* TextStream*/

typedef struct VEtextstream {
    /* Don't bother with accessors*/
    char* text; /* source of text to lex*/
    int mark;
    int next; /* next unread character*/
    int textlen;
} VEtextstream;

static VEtextstream*
new_textstream(char* text)
{
    VEtextstream* vet = (VEtextstream*)calloc(1,sizeof(VEtextstream));
    if(vet == NULL)
	return NULL;
    vet->textlen = strlen(text);
    vet->text = (char*)malloc(vet->textlen+1);
    strncpy(vet->text,text,vet->textlen);
    return vet;
}

static void
free_textstream(VEtextstream* vet)
{
    if(vet != NULL) {
	nullfree(vet->text);
	free(vet);
    }
}

static int
peek(VEtextstream* vet)
{
    return vet->text[vet->next];
}

static void
backup(VEtextstream* vet)
{
    if(vet->next <= 0)
        vet->next = 0;
    else if(vet->next < vet->textlen)
        vet->next--;
}

static int
read(VEtextstream* vet)
{
    char c = peek(vet);
    if(c != NULCHAR) vet->next++;
    return c;
}

static void
mark(VEtextstream* vet)
{
    vet->mark = vet->next;
}

static void
setNext(VEtextstream* vet, int next)
{
    vet->next = next;
}

static void
to_position(VEtextstream* vet, VEposition* pos)
{
    int i;
    int lineno = 0;
    int linepos = 0;
    for(i = 0;i < vet->mark;i++) {
        if(vet->text[i] == '\n') {
            lineno++;
            linepos = i;
        }
    }
    int charno = (vet->mark - linepos);
    if(pos) {pos->lineno = lineno; pos->charno = charno;}
}

/**************************************************/
/* Lexer*/

typedef struct VElexer {/* lexer state */
    VEtextstream* text;
    VEparser* parser;
    VEposition yypos;
    int tokenmark;
    struct VETEXT {
        int len;
        char text[MAXTOKENSIZE];
    } yytext;
    int debuglevel;
} VElexer;

static VEerror
new_lexer(VEparser* parser, VElexer** lexerp)
{
    VElexer* vel = (VElexer*)calloc(1,sizeof(VElexer));
    if(vel == NULL)
        return VE_ENOMEM;
    vel->parser = parser;
    vel->tokenmark = -1;
    if(lexerp) *lexerp = vel;
    return VE_NOERR;
}

static void
free_lexer(VElexer* lexer)
{
    if(lexer != NULL) {
	free_textstream(lexer->text);
	free(lexer);
    }
}

static void
set_position(VElexer* vel)
{
    to_position(vel->text,&vel->yypos);
}

static void
append(struct VETEXT* yytext, int c)
{
    if(yytext->len >= sizeof(yytext->text)-1)
	fprintf(stderr,"yytext overflow\n");
    else
	yytext->text[yytext->len++] = c;
    yytext->text[yytext->len] = '\0'; /* make sure nul terminated */
}

/**************************************************/
/* Parser */

struct VEparser {
    VE* ve;
    struct {
	int len;
        VEaction** actions;
    } actions;
    int debuglevel;
    VElexer* lexer;
};

VEerror
ve_new_parser(VE* ve, VEparser** parserp)
{
    VEerror stat = VE_NOERR;
    VEparser* vep = (VEparser*)calloc(1,sizeof(VEparser));
    if(vep == NULL)
	return VE_ENOMEM;
    vep->ve = ve;
    stat = new_lexer(vep,&vep->lexer);
    if(stat == VE_NOERR)
        if(parserp) *parserp = vep;
    return stat;
}

VEerror
ve_free_parser(VEparser* vep)
{
    if(vep != NULL) {
	int i;
	free_lexer(vep->lexer);
	for(i=0;i<vep->actions.len;i++)
	    (void)ve_free_action(vep->actions.actions[i]);	
	free(vep);
    }
    return VE_NOERR;
}

/**
 * Entry point for the scanner.
 * Returns the token corresponding
 * to the next token and stores the value.
 *
 * @param yytext store the value here
 * @return the token identifier corresponding to the next token.
 */

static VEerror
yylex(VElexer* lexer, int* tokenp)
{
    VEerror stat = VE_NOERR;
    int token = TOKEN_NONE;
    int c = 0;
    lexer->yytext.len = 0;

    if(lexer->tokenmark >= 0) {
        setNext(lexer->text,lexer->tokenmark);
        lexer->tokenmark = -1;
    }
    token = TOKEN_NONE;
    while(token == TOKEN_NONE) {
        mark(lexer->text);
        c = read(lexer->text);
        if(c == EOS) {
            token = EOF;
        } else if(c == '\n')
            token = c;
        else if(c == COMMENTCHAR) {
            /* move to end of line and return EOL or EOS*/
            for(;;) {
                c = read(lexer->text);
                if(c == EOS) {
                    token = EOF;
                    break;
                } else if(c == '\n') {
                    token = c;
                    break;
                }
            }
        } else if(c <= ' ' || c == '\177') {
            /* whitespace: ignore */
        } else if(c == '"' || c == '\'') {
            int delim = c;
            int more = 1;
            /* We have a string token */
            while(more && (c = read(lexer->text)) > 0) {
                switch (c) {
                case EOS:
		    to_position(lexer->text,&lexer->yypos);
                    fprintf(stderr,"Unterminated string constant\n");
		    return VE_EPARSE;
                case '"':
                    more = (delim != c);
                    break;
                case '\'':
                    more = (delim != c);
                    break;
                case ESCAPE:
                    switch (c) {
                    case 'r':
                        c = '\r';
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    default:
                        break;
                    }
                    break;
                default:
                    break;
                }
                if(more) append(&lexer->yytext,c);
            }
            token = TOKEN_STRING;
        } else if(strchr(WORDCHAR1,c) != NULL) {
	    long num;
	    char* ep;
            append(&lexer->yytext,c);
            for(;;) {
                c = read(lexer->text);
                if(strchr(WORDCHARN,c) == NULL)
		    break; /* not a word character*/
                append(&lexer->yytext,c);
            }
            /* pushback the delimiter*/
            backup(lexer->text);
            /* See if this looks like an integer*/
            num = strtol(lexer->yytext.text,&ep,10);
	    if(*ep == NULCHAR)
                token = TOKEN_NUMBER;
	    else
                token = TOKEN_WORD;
        } else {/* Treat as a single char delimiter*/
            token = c;
        }
    }
    set_position(lexer);
    if(lexer->debuglevel > 0)
        fprintf(stderr,"TOKEN = |%s|\n",dumptoken(token, lexer->yytext.text));
    if(tokenp) *tokenp = token;
    return stat;
}

/**
 * Allow a one token pushback
 */
static void
pushback(VElexer* vel)
{
    vel->tokenmark = vel->text->mark;
}

/**************************************************/
/* Parser*/

/**************************************************/
/* Externally visible API */

void
ve_setdebuglevel(VEparser* vep, int level)
{
    vep->debuglevel = (level < 0 ? 0 : level);
}

VEerror
ve_parse_internal(VEparser* vep, char* text, VElist* actions)
{
    VEerror stat = VE_NOERR;
    vep->lexer->text = new_textstream(text);
    stat = parseR(vep, actions);
    return stat;
}

/**
 * Recursive parser
 */
static VEerror
parseR(VEparser* vep, VElist* actions)
{
    VEerror stat = VE_NOERR;
    int token = TOKEN_NONE;
    VEaction* action = NULL;
    VElexer* lexer = vep->lexer;

    /* read action per loop*/
    for(;;) {
        const char* name;
        VEverb* verb;

        stat = yylex(lexer,&token);
	if(stat != VE_NOERR)
	    goto done;
        if(vep->ve->cfg->parsedebug) {
            fprintf(stderr,"parser: reading token: %s\n",dumptoken(token, lexer->yytext.text));
	    reportposition(lexer->yypos);
	}
        if(token == EOF)
            goto done;

        switch (token) {
        case '.': /* => EOL*/
        case '\n': /* => EOL*/
            continue;
        case RBRACE:
            pushback(lexer);
            goto done;
        case TOKEN_WORD: /* Verb*/
            name = lexer->yytext.text;
	    stat = ve_lookup(vep->ve,name,&verb);
            if(stat == VE_EUNDEF) {
                fprintf(stderr,"Unknown verb: %s", name);
	        reportposition(lexer->yypos);
	        goto done;
            }
            stat = ve_new_action(verb,&action);
	    if(stat != VE_NOERR) goto done;
            action->position = lexer->yypos;
	    action->argv = (VEarg*)malloc(sizeof(VEarg)*(verb->arity));
            stat = collectargs(action, vep, verb->arity,action->argv);
	    if(stat != VE_NOERR) goto done;
            velistpush(actions,action);
            break;
        default:
	    stat = VE_EPARSE;
            fprintf(stderr,"Expected %s, found: %s", "Verb", lexer->yytext.text);
	    reportposition(lexer->yypos);
	    goto done;
        }
    }
done:
    return stat;
}

static VEerror
collectargs(VEaction* action, VEparser* vep, int arity, VEarg* argv)
{
    int i;
    VEerror stat = VE_NOERR;
    int token = TOKEN_NONE;
    VEparenstate parenstate = UNDEF; /* track if we are inside parens */
    VElexer* lexer = vep->lexer;

    /* read arg per loop upto end of action*/
    for(i = 0;stat == VE_NOERR && i<arity;i++) {
        stat = yylex(vep->lexer,&token);
	if(stat != VE_NOERR) break;
        switch (token) {
        case EOF:
        case '.':
        case ';':
        case '\n':
	    goto done;
        case LBRACE: {
	    VEaction** block;
            VElist* blocklist = velistnew();
            stat = parseR(vep,blocklist);
	    if(stat != VE_NOERR) goto done;
            stat = yylex(vep->lexer,&token);
            if(token != RBRACE) {
		stat = VE_EPARSE;
                fprintf(stderr,"Unclosed block\n");
		reportposition(vep->lexer->yypos);
		goto done;
	    }
	    block = (VEaction**)velistdup(blocklist);
	    velistfree(blocklist);
	    stat = ve_new_arg(VE_BLOCK,(void*)block,&argv[i]);
	    } break;
        case RBRACE:
	    stat = VE_EPARSE;
            fprintf(stderr,"Too many %c\n",RBRACE);
    	    reportposition(vep->lexer->yypos);
            goto done;
        case LPAREN:
            if(parenstate != UNDEF) {
		stat = VE_EPARSE;
                fprintf(stderr,"Too many parentheses\n");
		reportposition(vep->lexer->yypos);
		goto done;
	    } else 
                parenstate = LEFT;
            break;
        case RPAREN:
            switch (parenstate) {
            case UNDEF:
                fprintf(stderr,"Parentheses mismatch\n");
		reportposition(lexer->yypos);
                goto done;
            case RIGHT:
                fprintf(stderr,"Too many parentheses\n");
		reportposition(lexer->yypos);
                goto done;
            case LEFT:
                parenstate = RIGHT;
                break;
            }
            break;
        case TOKEN_WORD:
            if(parenstate == RIGHT) {
		stat = VE_EARITY;
                fprintf(stderr,"Arguments after right  parenthesis\n");
		reportposition(vep->lexer->yypos);
            } else
	        stat = ve_new_arg(VE_WORD,(void*)strdup(lexer->yytext.text),&argv[i]);
            break;
        case TOKEN_STRING:
            if(parenstate == RIGHT) {
		stat = VE_EARITY;
                fprintf(stderr,"Arguments after right  parenthesis\n");
		reportposition(vep->lexer->yypos);
            } else
	        stat = ve_new_arg(VE_STRING,(void*)strdup(lexer->yytext.text),&argv[i]);
            break;
        case TOKEN_NUMBER:
            if(parenstate == RIGHT) {
                fprintf(stderr,"Arguments after right  parenthesis\n");
		reportposition(vep->lexer->yypos);
            } else
	        stat = ve_new_arg(VE_NUMBER,(void*)strdup(lexer->yytext.text),&argv[i]);
            break;
        default:
            fprintf(stderr,"Expected %s, found %s\n","Argument", lexer->yytext.text);
  	    reportposition(vep->lexer->yypos);
	    stat = VE_EPARSE;
            break;
        }
    }
    /* Validate against the signature*/
    if(stat == VE_NOERR && i < arity) {
        fprintf(stderr,"Mismatch in number of arguments");
        reportposition(vep->lexer->yypos);
	stat = VE_EARITY;
    }
    if(stat != VE_NOERR)
	goto done;
    for(i=0;i < arity;i++) {
        VEargtype at = action->verb->signature[i];
        VEarg arg = argv[i];
        if(!compatible(arg.argtype, at)) {
	    stat = VE_ETYPE;
            fprintf(stderr,"Type mismatch for argument %d\n",i);
            reportposition(lexer->yypos);
	    break;
	}
    }
done:
    return stat;
}
    
/**************************************************/
/* Utils*/
    
static char*
dumptoken(int token, char* lval)
{
    char buf[MAXTOKENSIZE];
    char cs[2] = {0,0};
    buf[0] = '\0';
    if(token >= 0 && token < '\177') {
        if(token == '\n')
           strcat(buf,"\\n");
        else if(token == EOF)
           strcat(buf,"EOF");
        else {
	   cs[0] = (char)token;
	   strcat(buf,cs);
	}
    } else
        switch (token) {
        case TOKEN_STRING:
            strcat(buf,"\"");
            strcat(buf,lval);
            strcat(buf,"\"");
            break;
        case TOKEN_NUMBER:
            strcat(buf,lval);
            break;
        case TOKEN_WORD:
            strcat(buf,lval);
            break;
        default:
            strcat(buf,"UNDEFINED");
            break;
        }
	return strdup(buf);
}

static void
reportposition(VEposition yypos)
{
    fprintf(stderr,"\tat position %d:%d\n",yypos.lineno,yypos.charno);
}

static int
compatible(VEargtype arg, VEargtype sig)
{
    if(arg == sig)
	return 1;
    if(sig == VE_STRING)
	return 1;
    if(arg == VE_STRING)
	return 0;
    if(sig == VE_NUMBER && arg != VE_NUMBER)
	return 0;
    return 1;
}
