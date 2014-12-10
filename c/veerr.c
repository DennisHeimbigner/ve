/** \file
Error messages and library version.

These functions return the library version, and error messages.

Copyright 2010 University Corporation for Atmospheric
Research/Unidata. See COPYRIGHT file for more info.
*/
    
#include "config.h"
#include <stdlib.h>
#include <string.h>
#include "ve.h"

/*! VE Error Handling

\addtogroup error VE Error Handling

    VE functions return a non-zero status codes on error.

Each ve function returns an integer status value. If the returned
status value indicates an error, you may handle it in any way desired,
from printing an associated error message and exiting to ignoring the
error indication and proceeding (not recommended!).

The ve_strerror() function is available to convert a returned integer
error status into an error message string.

Occasionally, low-level I/O errors may occur in a layer below the
    ve library. For example, if a write operation causes you to exceed
disk quotas or to attempt to write to a device that is no longer
available, you may get an error from a layer below the ve library,
but the resulting write error will still be reflected in the returned
status value.

*/

/** \{ */

/*! Given an error number, return an error message.

This function returns a static reference to an error message string
    corresponding to an integer ve error status or to a system error
number, presumably returned by a previous call to some other ve
function. The error codes are defined in ve.h.

\param ncerr1 error number

\returns short string containing error message.

Here is an example of a simple error handling function that uses
nc_strerror() to print the error message corresponding to the ve
error status returned from any ve function call and then exit:

\code
    #include <ve.h>
...
     void handle_error(int status) {
     if (status != VE_NOERR) {
        fprintf(stderr, "%s\n", ve_strerror(status));
        exit(-1);
        }
     }
\endcode
*/
const char*
ve_strerror(VEerror veerr1)
{
    /* System error? */
    if(veerr1 > 0) {
	const char *cp = (const char *) strerror(veerr1);
	if(cp == NULL)
	    return "Unknown Error";
	return cp;
    }

    /* If we're here, this is a ve error code. */
    switch(veerr1) {
    case VE_NOERR:
	return "VE: No error";
    case VE_EINVAL:
	return "VE: Invalid procedure parameter";
    case VE_ENOMEM:
	return "VE: Out of memory";
    case VE_EINPUT:
	return "VE: Could not read input";
    case VE_EARITY:
	return "VE: Arity mismatch: verb is called with incorrect number of arguments";
    case VE_EPARSE:
	return "VE: Failed to parse input";
    case VE_EUNDEF:
	return "VE: Undefined verb";
    case VE_ETYPE:
	return "VE: Verb called with argument of incompatible type";
    default: break;
    }
    return "VE: Unknown Error";
}

/** \} */
