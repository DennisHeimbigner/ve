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
    stat = ve_parse(ve,text);
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
makeverb(Verbtag tag, VEevaluator eval, int arity, /*VEargtype*/...)
{
    VEerror stat = VE_NOERR;
    VEverb* verb;
    VEargtype* signature;
    int i;
    va_list ap;

    signature = (VEargtype*)malloc(sizeof(VEargtype)*(arity+1));
    if(arity > 0) {
        va_start(ap,arity);
        for(i=0;i<arity;i++) {
	    signature[i] = va_arg(ap,VEargtype);
	}
        va_end(ap);
    }
    signature[arity] = VE_UNDEF;
    stat = ve_new_verb(verbnames[(int)tag],signature,&verb);
    if(stat != VE_NOERR) fatal("verb creation failure");
    verb->uid = (int)tag;
    /* all verbs use the same evaluator */
    verb->evaluate = eval;
    return (VEverb*)verb;
}

/*
Initial set of Verbs; 
The verb names are intended to be case insensitive.
*/

static VEerror eval_startofline(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_endofline(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_find(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_then(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_maybe(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_anything(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_anythingbut(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_anythingbutnot(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_something(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_somethingbut(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_linebreak(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_br(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_tab(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_word(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_anyof(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_any(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_or(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_begincapture(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_endcapture(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_begin(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_end(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_either(VE*,VEverb*,int,VEarg*,void*);
static VEerror eval_stop(VE*,VEverb*,int,VEarg*,void*);

static void
loadverbs(VEconfiguration* cfg)
{
    VEverb** verbs;
    cfg->verbs = (VEverb**)malloc(sizeof(VEverb*)*(MAX+1));
    verbs = cfg->verbs;
    int index = 0;
    
    verbs[index++] = makeverb(Startofline,eval_startofline,0);
    verbs[index++] = makeverb(Endofline,eval_endofline,0);
    verbs[index++] = makeverb(Find,eval_find,0,VE_STRING);
    verbs[index++] = makeverb(Then,eval_then,0,VE_STRING);
    verbs[index++] = makeverb(Maybe,eval_maybe,0,VE_STRING);
    verbs[index++] = makeverb(Anything,eval_anything,0,VE_STRING);
    verbs[index++] = makeverb(Anythingbut,eval_anythingbut,0,VE_STRING);
    verbs[index++] = makeverb(Anythingbutnot,eval_anythingbutnot,0,VE_STRING);
    verbs[index++] = makeverb(Something,eval_something,0);
    verbs[index++] = makeverb(Somethingbut,eval_somethingbut,0,VE_STRING);
    verbs[index++] = makeverb(Linebreak,eval_linebreak,0);
    verbs[index++] = makeverb(Br,eval_br,0);
    verbs[index++] = makeverb(Tab,eval_tab,0);
    verbs[index++] = makeverb(Word,eval_word,0);
    verbs[index++] = makeverb(Anyof,eval_anyof,0,VE_STRING);
    verbs[index++] = makeverb(Any,eval_any,0,VE_STRING);
    verbs[index++] = makeverb(Or,eval_or,0);
    verbs[index++] = makeverb(Begincapture,eval_begincapture,0);
    verbs[index++] = makeverb(Endcapture,eval_endcapture,0);
    verbs[index++] = makeverb(Begin,eval_begin,0);
    verbs[index++] = makeverb(End,eval_end,0);
    verbs[index++] = makeverb(Either,eval_either,0);
    verbs[index++] = makeverb(Stop,eval_stop,0);
}

static VEerror
eval_startofline(VE* ve, VEverb* verb, int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,"^");
    return VE_NOERR;
}

static VEerror
eval_endofline(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,"$");
    return VE_NOERR;
}

static VEerror
eval_find(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,".*");
    return VE_NOERR;
}

static VEerror
eval_then(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    char* arg = (char*)argv[0].value;
    strcat(state->buf,arg);
    return VE_NOERR;
}

static VEerror
eval_maybe(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    char* arg = (char*)argv[0].value;
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
    return VE_NOERR;
}

static VEerror
eval_anything(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,".*");
    return VE_NOERR;
}

static VEerror
eval_anythingbut(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{return eval_anythingbutnot(ve,verb,arity,argv,state0);}

static VEerror
eval_anythingbutnot(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    char* arg = (char*)argv[0].value;
    strcat(state->buf,"[^");
    strcat(state->buf,arg);
    strcat(state->buf,"]*");
    return VE_NOERR;
}

static VEerror
eval_something(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    char* arg = (char*)argv[0].value;
    strcat(state->buf,".+");
    return VE_NOERR;
}

static VEerror
eval_somethingbut(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    char* arg = (char*)argv[0].value;
    strcat(state->buf,"[^");
    strcat(state->buf,(char*)argv[0].value);
    strcat(state->buf,"]+");
    return VE_NOERR;
}

static VEerror
eval_linebreak(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,"[\r]?[\n]");
    return VE_NOERR;
}

static VEerror
eval_br(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,"[\r]?[\n]");
    return VE_NOERR;
}

static VEerror
eval_tab(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,"[\t]");
    return VE_NOERR;
}

static VEerror
eval_word(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,"\\w+");
    return VE_NOERR;
}

static VEerror
eval_anyof(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{return eval_any(ve,verb,arity,argv,state0);}

static VEerror
eval_any(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    char* arg = (char*)argv[0].value;
    strcat(state->buf,"[");
    strcat(state->buf,arg);
    strcat(state->buf,"]");
    return VE_NOERR;
}

static VEerror
eval_or(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,"|");
    return VE_NOERR;
}

static VEerror
eval_begincapture(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,LPAREN);
    return VE_NOERR;
}

static VEerror
eval_endcapture(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,RPAREN);
    return VE_NOERR;
}

static VEerror
eval_begin(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,LPAREN);
    return VE_NOERR;
}

static VEerror
eval_end(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,RPAREN);
    return VE_NOERR;
}

static VEerror
eval_either(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    strcat(state->buf,"");
    return VE_NOERR;
}

static VEerror
eval_stop(VE* ve,VEverb* verb,int arity, VEarg* argv, void* state0)
{
    Teststate* state = (Teststate*)state0;
    return VE_NOERR;
}
