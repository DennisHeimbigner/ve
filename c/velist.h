/* Copyright 2009, UCAR/Unidata and OPeNDAP, Inc.
   See the COPYRIGHT file for more information. */
#ifndef VELIST_H
#define VELIST_H 1

/* Define the type of the elements in the list*/

#if defined(_CPLUSPLUS_) || defined(__CPLUSPLUS__)
#define EXTERNC extern "C"
#else
#define EXTERNC extern
#endif

EXTERNC int velistnull(void*);

typedef struct VElist {
  unsigned long alloc;
  unsigned long length;
  void** content;
} VElist;

EXTERNC VElist* velistnew(void);
EXTERNC int velistfree(VElist*);
EXTERNC int velistsetalloc(VElist*,unsigned long);
EXTERNC int velistsetlength(VElist*,unsigned long);

/* Set the ith element */
EXTERNC int velistset(VElist*,unsigned long,void*);
/* Get value at position i */
EXTERNC void* velistget(VElist*,unsigned long);/* Return the ith element of l */
/* Insert at position i; will push up elements i..|seq|. */
EXTERNC int velistinsert(VElist*,unsigned long,void*);
/* Remove element at position i; will move higher elements down */
EXTERNC void* velistremove(VElist* l, unsigned long i);

/* Tail operations */
EXTERNC int velistpush(VElist*,void*); /* Add at Tail */
EXTERNC void* velistpop(VElist*);
EXTERNC void* velisttop(VElist*);

/* Duplicate and return the content (null terminate) */
EXTERNC void** velistdup(VElist*);

/* Look for value match */
EXTERNC int velistcontains(VElist*, void*);

/* Remove element by value; only removes first encountered */
EXTERNC int velistelemremove(VElist* l, void* elem);

/* remove duplicates */
EXTERNC int velistunique(VElist*);

/* Create a clone of a list */
EXTERNC VElist* velistclone(VElist*);

/* Following are always "in-lined"*/
#define velistclear(l) velistsetlength((l),0)
#define velistextend(l,len) velistsetalloc((l),(len)+(l->alloc))
#define velistcontents(l)  ((l)==NULL?NULL:(l)->content)
#define velistlength(l)  ((l)==NULL?0:(l)->length)

#endif /*VELIST_H*/
