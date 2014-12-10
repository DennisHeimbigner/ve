Documentation for building OC with CMake
********************************************************

This document describes building the oc C library and the
ocprint tool using KitWare's CMake utility.  By
integrating CMake, we are able to provide cross-platform
(Windows as well as Linux/Unix) configuration and build support.

Getting CMake
*************

CMake, a software configuration and testing tool, is maintained by
Kitware.  CMake is available in many linux package management
systems, as well as the 'macports' package management system for 
OSX. 

CMake may also be downloaded for these platforms, as well as Windows,
from the CMake website at http://www.cmake.org.

Building oc with CMake
**************************

The oc library and tool(s) requires one 3rd party library for
full functionality: libcurl (see http://curl.haxx.se).

Note that CMake encourages 'out-of-source-tree' builds, i.e. the
directory used to build netCDF is not the root of the netCDF
file structure.  For example, it is fairly common practice to
create a 'build' directory inside the source directory. The examples
in this file will use syntax which assumes the user is currently
located in c:\oc\build.

Building oc
*****************************************

To build, use the following commands.

Windows:
	> cmake -DCMAKE_INSTALL_PREFIX=c:/
	> cmake --build .
	> cmake --build . --target RUN_TESTS
	> cmake --build . --target INSTALL

Linux/Unix:	
  	> cmake -DCMAKE_INSTALL_PREFIX=/usr/local
   	> make test install

The value of ``CMAKE_INSTALL_PREFIX'' specified the directory
into which to install oc.

If you get the message that oc installed correctly, then you are
done!

Linking to oc
*****************

For static builds, and to use netCDF-4 you must link to all the libraries,
oc and curl. This will mean -L options
to your build for the locations of the libraries, and -l (lower-case
L) for the names of the libraries.

For shared builds, only -loc is needed. All other libraries will
be found automatically.

On Windows, when using Visual Studio and compiling a shared library (.dll),
oc will be built with an 'import' library, named oc.lib. Visual
Studio projects should link against this import library, instead of linking
against the oc.dll file directly.

To specify static libraries with CMake, you must use the 'BUILD_SHARED_LIBS=OFF'
flag when invoking CMake.

 	oc> cmake --build . -D"BUILD_SHARED_LIBS=OFF" 

