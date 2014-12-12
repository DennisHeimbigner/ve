/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

#include "config.h"
#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>
#include <string.h>

#if defined _WIN32 || defined _WIN64
#define strcasecmp _stricmp
#else
#include <strings.h>
#endif

#include "ve.h"
#include "velist.h"
#include "veinternal.h"

/*forward*/

/* Misc */

int
ve_compute_len(void** list) /* assume null terminated */
{
    int i = 0;
    while(*list++) {i++;}
    return i;
}

VEerror
ve_new_action(VEverb* verb, VEaction** actionp)
{
    VEerror stat = VE_NOERR;
    int arity;
    VEaction* action;

    if(verb == NULL)
	return VE_EINVAL;
    arity = verb->arity;
    action = (VEaction*)calloc(1,sizeof(VEaction));
    if(action == NULL) {return VE_ENOMEM;}
    action->verb = verb;
    if(actionp) *actionp = action;
    return stat;
}

void
ve_set_argv(VEaction* action, VEarg* argv)
{
    action->argv = argv;
}

VEerror
ve_free_action(VEaction* action)
{
    VEerror stat = VE_NOERR;
    int i;
    if(action == NULL) return stat;
    /* do not free verb; will be done elsewhere */
    for(i=0;i<action->verb->arity;i++)
	ve_free_arg(action->argv[i]);
    nullfree(action->argv);
    free(action);
    return stat;
}

VEerror
ve_lookup(VE* ve, const char* name, VEverb** verbp)
{
    VEverb** p;
    VEverb* verb = NULL;

    if(ve == NULL || name == NULL)
	return VE_EINVAL;
    for(p=ve->verbs;*p;p++) {/* Assumes a compact verb set */
	verb = *p;
	if(strcasecmp(verb->name,name) == 0)
	    break;
	verb = NULL;
    }
    if(verbp) *verbp = verb;
    return (verb == NULL ? VE_EVERB : VE_NOERR);
}

VEerror
ve_evaluate_internal(VE* ve, void* state, VEaction** program)
{
    VEerror stat = VE_NOERR;
    VEaction** p;    
    for(p=program;*p;p++) {
	VEaction* a = *p;
        stat = a->verb->evaluate(ve,a->verb,a->verb->arity,a->argv,state);
	if(stat != VE_NOERR) break;
    }
    return stat;
}

/**
Note that the uid and the evaluator must be set separately
*/
VEerror
ve_new_verb(const char* name, VEargtype* signature, VEverb** verbp)
{
    VEerror stat = VE_NOERR;
    VEverb* verb = NULL;

    verb = (VEverb*)calloc(1,sizeof(struct VEverb));
    if(verb == NULL) {stat = VE_ENOMEM; goto fail;}
    stat = ve_init_verb(verb,name,signature);
    if(stat != VE_NOERR) goto fail;
    if(verbp) *verbp = verb;
    return stat;
fail:
    if(verb) ve_free_verb(verb);
    return stat;
}

VEerror
ve_free_verb(VEverb* verb)
{
    if(verb == NULL) return VE_EINVAL;
    nullfree(verb->name);
    nullfree(verb->signature);
    free(verb);
    return VE_NOERR;
}

VEerror
ve_init_verb(VEverb* verb, const char* name, VEargtype* signature)
{
    VEerror stat = VE_NOERR;
    const VEargtype* p;
    int arity;

    if(name == NULL || strlen(name) == 0) {
	stat = VE_EINVAL;
	goto fail;
    }
    for(arity=0,p=signature;*p != VE_UNDEF;p++) {arity++;}
    verb->name = strdup(name);
    verb->arity = arity;
    verb->signature = signature;
    return stat;
fail:
    if(verb) ve_free_verb(verb);
    return stat;
}
