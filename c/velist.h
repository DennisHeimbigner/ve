/* Copyright 2009, UCAR/Unidata and OPeNDAP, Inc.
   See the COPYRIGHT file for more information. */
#ifndef VELIST_H
#define VELIST_H 1

#if defined(_CPLUSPLUS_) || defined(__CPLUSPLUS__)
#define EXTERNC extern "C"
#else
#define EXTERNC extern
#endif

typedef struct VElist {
  unsigned long alloc;
  unsigned long length;
  void** content;
} VElist;

EXTERNC VElist* velistnew(void);
EXTERNC int velistfree(VElist*);

EXTERNC int velistadd(VElist*,void*); /* Add at end */
EXTERNC void* velistget(VElist* l, unsigned long i);

/* Duplicate and return the content (null terminate) */
EXTERNC void** velistdup(VElist*);

/* Following are always "in-lined"*/
#define velistlength(l)  ((l)==NULL?0:(l)->length)

#endif /*VELIST_H*/
