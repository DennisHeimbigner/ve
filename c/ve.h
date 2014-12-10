/*
This software is released uder the Licence terms
described in the file LICENSE.txt.
*/

#ifndef VE_H
#define VE_H

#include <stdio.h>

/*!\enum VEerror
Define the set of error return codes.
The set consists of ve errors (negative
values) plus the set of system errors, which
are positive.
*/
typedef enum VEerror {
VE_NOERR=0,
VE_EINVAL=1,
VE_ENOMEM=2,
VE_EINPUT=3,
VE_EARITY=4, /* arity mismatch */
VE_EPARSE=5,
VE_EUNDEF=6,
VE_ETYPE=7,
} VEerror;

typedef enum VEargtype {
VE_UNDEF=0, /* must be 0 */
VE_WORD=1,
VE_STRING=2,
VE_NUMBER=3,
VE_BLOCK=4
} VEargtype;

typedef struct VEposition {
        int charno;
        int lineno;
} VEposition;

/*Forward*/
struct VE;
struct VEverb;

/**
WARNING: As a rule. instances of VEarg are passed by value.
*/
typedef struct VEarg {
    VEargtype argtype;
    void* value;
} VEarg;

typedef VEerror (*VEevaluator)(struct VE*,struct VEverb*,int,VEarg*,void*);

typedef struct VEverb {
    int uid; /* to make each verb switches more efficient */
    char* name;
    int arity;
    VEargtype* signature;
    VEevaluator evaluate;
} VEverb;

typedef struct VEaction {
    VEverb* verb;
    VEarg* argv; /* arity must match verb->arity; not vector is *, not ** */
    VEposition position;
} VEaction;

typedef struct VEconfiguration {
    char* input; /* whole input file */
    FILE* output;
    VEverb** verbs; /* null terminated array of pointers to VEverb instances. */
    /* Debug state */
    int verbose;
    int debug;
    int parsedebug;
    int trace;
} VEconfiguration;

typedef struct VE {
    VEconfiguration* cfg;
    VEaction** program;
    VEverb** verbs; /* null terminated; freed by client */
} VE;

extern VEerror ve_new_ve(VEconfiguration*, VE** vep);
extern VEerror ve_free_ve(VE*);
extern VEerror ve_init_ve(VE* vep, VEconfiguration*);

extern VEerror ve_new_arg(VEargtype, void* value, VEarg* argp);
extern VEerror ve_free_arg(VEarg);

/* Verb create/init/free is convenience for client */
extern VEerror ve_new_verb(const char*, VEargtype*, VEverb**);
extern VEerror ve_free_verb(VEverb*);
extern VEerror ve_init_verb(VEverb* verb, const char*, VEargtype*);

/* API */
extern VEerror ve_evaluate(VE* ve, void* state);

/* Error/Debug */

extern const char* ve_strerror(VEerror veerr1);

#endif /*VE_H*/
