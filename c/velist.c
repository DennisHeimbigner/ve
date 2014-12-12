/* Copyright 2009, UCAR/Unidata and OPeNDAP, Inc.
   See the COPYRIGHT file for more information. */
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "velist.h"

int velistnull(void* e) {return e == NULL;}

#ifndef TRUE
#define TRUE 1
#endif
#ifndef FALSE
#define FALSE 0
#endif

#define DEFAULTALLOC 16
#define ALLOCINCR 16

static int
velistsetalloc(VElist* l, size_t sz)
{
  void** newcontent = NULL;
  if(l == NULL) return FALSE;
  if(sz <= 0) {sz = (l->length?2*l->length:DEFAULTALLOC);}
  if(l->alloc >= sz) {return TRUE;}
  newcontent=(void**)calloc(sz,sizeof(void*));
  if(newcontent != NULL && l->alloc > 0 && l->length > 0 && l->content != NULL) {
    memcpy((void*)newcontent,(void*)l->content,sizeof(void*)*l->length);
  }
  if(l->content != NULL) free(l->content);
  l->content=newcontent;
  l->alloc=sz;
  return TRUE;
}

VElist*
velistnew(void)
{
  VElist* l;
  l = (VElist*)malloc(sizeof(VElist));
  if(l) {
    l->alloc=0;
    l->length=0;
    l->content=NULL;
  }
  return l;
}

int
velistfree(VElist* l)
{
  if(l) {
    l->alloc = 0;
    if(l->content != NULL) {free(l->content); l->content = NULL;}
    free(l);
  }
  return TRUE;
}

void*
velistget(VElist* l, unsigned long index)
{
  if(l == NULL || l->length == 0) return NULL;
  if(index >= l->length) return NULL;
  return l->content[index];
}

int
velistadd(VElist* l, void* elem)
{
  if(l == NULL) return FALSE;
  if(l->length >= l->alloc) velistsetalloc(l,0);
  l->content[l->length] = elem;
  l->length++;
  return TRUE;
}

/* Duplicate and return the content (null terminate) */
void**
velistdup(VElist* l)
{
    void** result = (void**)malloc(sizeof(void*)*(l->length+1));
    memcpy((void*)result,(void*)l->content,sizeof(void*)*l->length);
    result[l->length] = (void*)0;
    return result;
}

