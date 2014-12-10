/*
This sotware is released under the Licence terms
described in the file LICENSE.txt.
*/

#include "config.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "ve.h"
#include "XGetopt.h"

#define nullfree(x) {if((x) != NULL) {free(x);}}

/*************************************************/
/* Constants */

static int DEBUG = 1;
static int PARSEDEBUG = 1;

#define READBUFFERSIZE 1024

/*************************************************/
/* Forward */
static void usage();
static void statfatal(VEerror stat);
static void fatal(const char* msg);
static char* readfile(FILE* file);

/*************************************************/

#define OPTIONS "hf:o:W:"

static void
getopts(int argc, char** argv, VEconfiguration* cfg)
{
    /* Get command line options */
    int c;
    char* p;
    FILE* tmp;
    FILE* infile = NULL;
    
    /* Initialize */
    memset(cfg,0,sizeof(cfg));

    while((c = getopt(argc, argv, _T(OPTIONS))) != EOF) {
        switch (c) {
	case _T('h'):
            usage();
            exit(1);
	    break;
	case _T('f'): 
            // verify readability of input file
	    if(infile != NULL && infile != stdin) fclose(infile);
	    if(strcmp(optarg,"-")==0) 
		infile = stdin;
	    else
	        infile = fopen(optarg,"r");
	    if(infile == NULL)
                fatal("Input file not readable");
	    break;
	case _T('o'):
            // verify readability of input file
	    if(cfg->output != NULL) fclose(cfg->output);
	    if(strcmp(optarg,"-") == 0)
		cfg->output = stdout;
	    else {
	        cfg->output = fopen(optarg,"w");
	        if(cfg->output == NULL)
                    fatal("Output file not writeable");
	    }
	    break;
	case _T('W'):
	    for(p=optarg;(c=*p);p++) {
                switch (c) {
                case _T('d'):
                    cfg->debug = 1;
                    break;
                case _T('p'):
                    cfg->parsedebug = 1;
                    break;
                case _T('v'):
                    cfg->verbose = 1;
                default:
		    fatal("Unknown -W argumentn");
                    break;
                }
            }
	    break;
	case _T('?'): /* fall thru */
	default:
	    fprintf(stderr, "Illegal option %s\n", argv[optind-1]);
	    usage();
	    exit(1);
  	    break;
	}
    }

    if(infile == NULL && optarg == NULL)
        fatal("No input specified");

    /* Read the whole input file */
    cfg->input = readfile(infile);
    if(cfg->input == NULL)
	fatal("Could not read input");

    /* Overrides */
    if(DEBUG)
        cfg->debug = 1;
    if(PARSEDEBUG)
        cfg->parsedebug = 1;
    if(cfg->debug)
        cfg->verbose = 1;
}

static void
statfatal(VEerror stat)
{
    fatal(ve_strerror(stat));
}

static void
fatal(const char* msg)
{
    fprintf(stderr,"%s\n",msg);
    fprintf(stderr,"Use -h flag for help\n");
    fflush(stderr);
    exit(1);
}

static void usage()
{
    fprintf(stderr,
        "usage: test<1,2,3> <options>*\n"
           "where the options are:\n"
           "-h              -- display this message and exit.\n"
           "-f file         -- where to read input\n"
           "-o file         -- where to send output\n"
           "-F input-format -- specify the input format\n"
           "                   (currently only the ve format is suppported)\n"
           "-W woption      -- specify various debug options:\n"
           "                   'dn' -- turn on level n debug"
           "                   'p'  -- turn on parsing debug output."
           "                   'v'  -- turn on verbose output."
    );
    fflush(stderr);
}


static char*
readfile(FILE* file)
{
    char* buf = NULL;
    int bufsize = 0;
    int offset = 0; /* insertion point */    
    int avail = 0;

    /* Do not use fseek in case the input is stderr */
    buf = (char*)malloc(READBUFFERSIZE+1);
    if(buf == NULL) return NULL;
    bufsize = READBUFFERSIZE;
    avail = bufsize; 
    offset = 0;

    for(;;) {
	if(avail == 0) {
	    char* newbuf = (char*)realloc(buf,bufsize+READBUFFERSIZE+1);
	    if(newbuf == NULL) return NULL;
	    buf = newbuf;
	    avail += READBUFFERSIZE;
	}
        int c = fgetc(file);
        if(ferror(file)) goto fail;
        if(c == EOF) break;
        buf[offset++] = c;
	
    }        
    buf[offset] = '\0';
    return buf;
fail:
    if(buf != NULL) free(buf);
    return NULL;
}
