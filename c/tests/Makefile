#VG=valgrind --leak-check=full

SRC=test2.c
HD=testcommon.h 

CPPFLAGS=-I..
CFLAGS=-g -O0 -Wall -Wno-unused-variable -Wno-unused-parameter
LDFLAGS=../libve.a

O=${SRC:.c=.o}

T=${O:.o=.exe}

%.exe : %.o
	gcc ${CFLAGS} -o $@ $< $(LDFLAGS) 

all: ${T}

${T}: ${O}


clean::
	rm -f *.o *.exe

check::
	for t in ${T} ; do ${VG} ./$$t -f rwu1.ve ; done
