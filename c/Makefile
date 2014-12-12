C=ve.c veinternal.c veparser.c veerr.c velist.c vefile.c
H=ve.h veinternal.h velist.h vefile.h

LIB=libve.a

CFLAGS=-g -O0 -Wall -Wno-unused-variable -Wno-unused-parameter

O=${C:.c=.o}

all: ${LIB}

${O}: ${C} ${H}

${LIB}: ${O}
	ar -cr ${LIB} ${O}

clean::
	rm -f *.o *.exe ${LIB}

