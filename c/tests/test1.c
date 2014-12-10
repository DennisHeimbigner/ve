/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

#include "testcommon.h"

/* Forward */
typedef struct Teststate Teststate;

static void loadverbs(VEconfiguration* cfg);
static void* createstate(VEconfiguration* cfg);
static void printstate(VEconfiguration* cfg, void* state);
static VEerror evaluate(VE*, VEverb*, int arity, VEarg* argv, void* state);

int
main(int argc, char** argv)
{
    VEerror stat = VE_NOERR;
    VE* ve;
    void* state;
    VEconfiguration cfg;

    getopts(argc,argv,&cfg);
    loadverbs(&cfg);
    state = createstate(&cfg);

    stat = ve_new_ve(&cfg,&ve);
    if(stat != VE_NOERR) goto fail;
    stat = ve_evaluate(ve,state);
    if(stat != VE_NOERR) goto fail;
    printstate(&cfg,state);
    exit(0);
fail:
    statfatal(stat);
    exit(1);
}

/**************************************************/
/* Everything below here should be test dependent */

#define LPAREN "("
#define RPAREN ")"

typedef enum Verbtag {
	Undef = 0,
        Startofline = 1,
        Endofline = 2,
        Find = 3,
        Then = 4,
        Maybe = 5,
        Anything = 6,
        Anythingbut = 7,
        Anythingbutnot = 8,
        Something = 9,
        Somethingbut = 10,
        Linebreak = 11,
        Br = 12,
        Tab = 13,
        Word = 14,
        Anyof = 15,
        Any = 16,
        Or = 17,
        Begincapture = 18,
        Endcapture = 19,
        Begin = 20,
        End = 21,
        Either = 22,
        Stop = 23,
	MAX = 24,
} Verbtag;

static const char* verbnames[] = {
"undef",
"startofline",
"endofline",
"find",
"then",
"maybe",
"anything",
"anythingbut",
"anythingbutnot",
"something",
"somethingbut",
"linebreak",
"br",
"tab",
"word",
"anyof",
"any",
"or",
"begincapture",
"endcapture",
"begin",
"end",
"either",
"stop",
NULL,
};

struct Teststate {
    char buf[4096];
};

static void*
createstate(VEconfiguration* cfg)
{
    Teststate* state = (Teststate*)malloc(sizeof(Teststate));
    state->buf[0] = '\0';
    return (void*)state;
}

static void
printstate(VEconfiguration* cfg, void* state)
{
    printf("Result= %s\n", ((Teststate*)state)->buf);
}

static VEverb*
makeverb(Verbtag tag, int arity, /*VEargtype*/...)
{
    VEerror stat = VE_NOERR;
    VEverb* verb;
    VEargtype* signature;
    int i;
    va_list ap;

    signature = (VEargtype*)malloc(sizeof(VEargtype)*(arity+1));
    va_start(ap,arity);
    for(i=0;i<arity;i++) {
	signature[i] = va_arg(ap,VEargtype);
    }
    va_end(ap);
    stat = ve_new_verb(verbnames[(int)tag],signature,&verb);
    if(stat != VE_NOERR) fatal("verb creation failure");
    verb->uid = (int)tag;
    /* all verbs use the same evaluator */
    verb->evaluate = evaluate;    
    return (VEverb*)verb;
}

/*
Initial set of Verbs; 
The verb names are intended to be case insensitive.
*/

static void
loadverbs(VEconfiguration* cfg)
{
    VEverb** verbs;
    cfg->verbs = (VEverb**)malloc(sizeof(VEverb*)*(MAX+1));
    verbs = cfg->verbs;
    
    verbs[(int)Startofline] = makeverb(Startofline,0);
    verbs[(int)Endofline] = makeverb(Endofline,0);
    verbs[(int)Find] = makeverb(Find,0,VE_STRING);
    verbs[(int)Then] = makeverb(Then,0,VE_STRING);
    verbs[(int)Maybe] = makeverb(Maybe,0,VE_STRING);
    verbs[(int)Anything] = makeverb(Anything,0,VE_STRING);
    verbs[(int)Anythingbut] = makeverb(Anythingbut,0,VE_STRING);
    verbs[(int)Anythingbutnot] = makeverb(Anythingbutnot,0,VE_STRING);
    verbs[(int)Something] = makeverb(Something,0);
    verbs[(int)Somethingbut] = makeverb(Somethingbut,0,VE_STRING);
    verbs[(int)Linebreak] = makeverb(Linebreak,0);
    verbs[(int)Br] = makeverb(Br,0);
    verbs[(int)Tab] = makeverb(Tab,0);
    verbs[(int)Word] = makeverb(Word,0);
    verbs[(int)Anyof] = makeverb(Anyof,0,VE_STRING);
    verbs[(int)Any] = makeverb(Any,0,VE_STRING);
    verbs[(int)Or] = makeverb(Or,0);
    verbs[(int)Begincapture] = makeverb(Begincapture,0);
    verbs[(int)Endcapture] = makeverb(Endcapture,0);
    verbs[(int)Begin] = makeverb(Begin,0);
    verbs[(int)End] = makeverb(End,0);
    verbs[(int)Either] = makeverb(Either,0);
    verbs[(int)Stop] = makeverb(Stop,0);
}

static VEerror
evaluate(VE* ve, VEverb* verb, int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    char* arg;
    Verbtag tag = (Verbtag)verb->uid;

    switch (tag) {
    case Startofline:
	strcat(state->buf,"^");
	break;
    case Endofline:
	strcat(state->buf,"$");
	break;
    case Find:
	strcat(state->buf,".*");
	strcat(state->buf,(char*)argv[0].value);
	break;
    case Then:
	strcat(state->buf,(char*)argv[0].value);
	break;
    case Maybe:
	arg = (char*)argv[0].value;
	switch (strlen(arg)) {
	case 0:
	    fatal("Maybe: zero length argument");
	case 1:
	    strcat(state->buf,arg);
	    strcat(state->buf,"?");
	    break;
	default:
	    strcat(state->buf,"(");
	    strcat(state->buf,arg);
	    strcat(state->buf,")?");
	    break;
	}
	break;
    case Anything:
	strcat(state->buf,".*");
	break;
    case Anythingbut:
    case Anythingbutnot:
	strcat(state->buf,"[^");
	strcat(state->buf,(char*)argv[0].value);
	strcat(state->buf,"]*");
	break;
    case Something:
	strcat(state->buf,".+");
	break;
    case Somethingbut:
	strcat(state->buf,"[^");
	strcat(state->buf,(char*)argv[0].value);
	strcat(state->buf,"]+");
	break;
    case Linebreak:
	strcat(state->buf,"[\r]?[\n]");
	break;
    case Br:
	strcat(state->buf,"[\r]?[\n]");
	break;
    case Tab:
	strcat(state->buf,"[\t]");
	break;
    case Word:
	strcat(state->buf,"\\w+");
	break;
    case Anyof:
    case Any:
	strcat(state->buf,"[");
	strcat(state->buf,(char*)argv[0].value);
	strcat(state->buf,"]");
	break;
    case Or:
	strcat(state->buf,"|");
	break;
    case Begincapture:
	strcat(state->buf,LPAREN);
	break;
    case Endcapture:
	strcat(state->buf,RPAREN);
	break;
    case Begin:
	strcat(state->buf,LPAREN);
	break;
    case End:
	strcat(state->buf,RPAREN);
	break;
    case Either:
	strcat(state->buf,"");
	break;
    case Stop:
        break;
    default:
	abort();
    }
    return VE_NOERR;
}
