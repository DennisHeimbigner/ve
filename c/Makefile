C=ve.c veinternal.c velist.c veparser.c veerr.c
H=ve.h veinternal.h velist.h

LIB=libve.a

CFLAGS="-Wall -Wno-unused-variable -Wno-unused-parameter"

O=${C:.c=.o}

all: ${LIB}

${O}: ${C} ${H}

${LIB}: ${O}
	ar -cr ${LIB} ${O}

clean::
	rm -f *.o *.exe ${LIB}

