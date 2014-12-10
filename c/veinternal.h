/*
This software is released uder the Licence terms
described in the file LICENSE.txt.
*/

#ifndef VEINTERNAL_H
#define VEINTERNAL_H

/* Internal only types */

/* forward */
typedef struct VEparser VEparser;

extern int VE_DEBUG;
extern int VE_PARSEDEBUG;

extern VEerror ve_evaluate_internal(VE* ve, void* state, VEaction** actions);

extern VEerror ve_new_action(VEverb*, VEaction**);
extern VEerror ve_free_action(VEaction*);
extern void    ve_set_argv(VEaction*, VEarg*);

extern VEerror ve_new_parser(VE*,VEparser**);
extern VEerror ve_free_parser(VEparser*);
extern void ve_setdebuglevel(VEparser* vep, int level);
extern VEerror ve_parse_internal(VEparser* vep, char* text, VElist* actions);

extern int ve_compute_len(void** list); /* assume null terminated */
extern VEerror ve_lookup(VE* ve, const char* name, VEverb** verbp);

#define nullfree(x) {if((x) != NULL) {free(x);}}

#endif /*VEINTERNAL_H*/
