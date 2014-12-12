/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

#include "config.h"
#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>
#include <string.h>

#include "ve.h"
#include "velist.h"
#include "veinternal.h"

/*forward*/
static VEerror validate(VEconfiguration* cfg);

int VE_DEBUG = 1;
int VE_PARSEDEBUG = 1;

VEerror
ve_new_ve(VEconfiguration* cfg, VE** vep)
{
    VEerror stat = VE_NOERR;
    VE* ve;
    stat = validate(cfg);
    if(stat != VE_NOERR) return stat;
    ve = (VE*)calloc(1,sizeof(VE));
    if(ve == NULL) return VE_ENOMEM;
    stat = ve_init_ve(ve,cfg);
    if(stat != VE_NOERR) {
	ve_free_ve(ve);
    } else {
        *ve->cfg = *cfg;
        if(vep) *vep = ve;
    }
    return stat;
}

VEerror
ve_free_ve(VE* ve)
{
    VEerror stat = VE_NOERR;
    VEaction** p;
    nullfree(ve->cfg);
    for(p=ve->program;*p;p++) {
        stat = ve_free_action(*p);
	if(stat != VE_NOERR) break;
    }
    nullfree(ve->program);
    return stat;
}

VEerror
ve_init_ve(VE* ve, VEconfiguration* cfg)
{
    VEerror stat = VE_NOERR;
    if(ve == NULL || cfg == NULL) return VE_EINVAL;
    memset(ve,0,sizeof(VE));
    ve->cfg = (VEconfiguration*)calloc(1,sizeof(VEconfiguration));
    if(ve->cfg == NULL) {return VE_ENOMEM;}
    *ve->cfg = *cfg;
    ve->verbs = ve->cfg->verbs; /* for convenience */
    stat = validate(ve->cfg);
    return stat;
}

static VEerror
validate(VEconfiguration* cfg)
{
    VEerror stat = VE_NOERR;
    if(cfg->verbs == NULL)
	stat = VE_EVERBS;
    if(VE_DEBUG)
        cfg->debug = 1;
    if(VE_PARSEDEBUG)
        cfg->parsedebug = 1;
    if(cfg->debug)
        cfg->verbose = 1;
    return stat;
}

static void
reset(VE* ve)
{
    VEaction** p;
    if(ve->program != NULL) {
        for(p=ve->program;*p;p++)
	    (void)ve_free_action(*p);
	free(ve->program);
    }
    ve->program = NULL;
}

VEerror
ve_parse(VE* ve, char* input)
{
    VEerror stat = VE_NOERR;
    VEparser* parser;

    if(input == NULL || strlen(input) == 0)
	return VE_EINPUT;
    /* reset */
    reset(ve);
    ve->input = input;    
    /* Create the input parser */
    stat = ve_new_parser(ve,&parser);
    if(stat == VE_NOERR) {
	VElist* program;
        if(ve->cfg->parsedebug)
	    ve_setdebuglevel(parser,1);
	program = velistnew();
        stat = ve_parse_internal(parser,ve->input,program);
        (void)ve_free_parser(parser);
	ve->program = (VEaction**)velistdup(program);
	velistfree(program);
    }
    return stat;
}

/*************************************************/
/* Execution */

VEerror
ve_evaluate(VE* ve, void* state)
{
    return ve_evaluate_internal(ve,state,ve->program);
}

VEerror
ve_new_arg(VEargtype at, void* value, VEarg* argp)
{
    VEerror stat = VE_NOERR;
    VEarg arg;
    arg.argtype = at;
    arg.value = value;
    if(argp) *argp = arg;
    return stat;
}

VEerror
ve_free_arg(VEarg arg)
{
    VEerror stat = VE_NOERR;
    nullfree(arg.value);
    return stat;
}
