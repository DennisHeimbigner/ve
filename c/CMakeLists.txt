#minimum required cmake version
cmake_minimum_required(version 2.8.8)
 
### verbose make, for debugging.
#set(cmake_verbose_makefile on)

project(ve)
set(package "ve" cache string "")

#project version
set(ve_version 1.0)
set(version ${ve_version})

#library .so version.
#set(ve_so_version 7.2.0)

# set some default c flags if we are using gcc
if(cmake_compiler_is_gnucc)
    set(cmake_c_flags "-g -o0")
endif()

# get system configuration
find_program(uname names uname)
if(uname)
macro(getuname name flag)
    exec_program("${uname}" args "${flag}" output_variable "${name}")
endmacro(getuname)
getuname(osname -s)
getuname(osrel  -r)
getuname(cpu    -m)
set(buildname "${osname}-${osrel}-${cpu}" cache string "build name variable for cdash")
endif()

# for cmake_install_libdir
#include(gnuinstalldirs)
include(installrequiredsystemlibraries)

# only necessary for windows
if(msvc)
    # supress some warnings on windows
    add_definitions(-d_crt_secure_no_warnings)
    set(global property use_folders on)
endif()

# set variable to define the build type.
include(generateexportheader)

# default to shared libs on
option(enable_shared "configure ve as a shared library." on)

set (lib_type static)
if (enable_shared)
    set(lib_type shared)
endif()
#add_definitions()

set(cmake_include_current_dir on)

#add custom cmake module
set(cmake_module_path "${cmake_source_dir}/cmake/modules/"
     cache internal "location of our custom cmake modules.")

# configure-type checks
include (${cmake_root}/modules/checklibraryexists.cmake)
include (${cmake_root}/modules/checkincludefile.cmake)
include (${cmake_root}/modules/checktypesize.cmake)
include (${cmake_root}/modules/checkfunctionexists.cmake)
include (${cmake_root}/modules/checksymbolexists.cmake)
include (${cmake_root}/modules/getprerequisites.cmake)
find_package(pkgconfig quiet)

## enable 'dist and distcheck'.
## file taken from http://ensc.de/cmake/FindMakeDist.cmake
find_package(makedist)
## end 'enable dist and distcheck'

if (not cmake_build_type)
    set(cmake_build_type debug cache string "choose the type of build, options are: none, debug, release."
         force)
endif()

#####
# some utility macros/scripts.
#####
# macro to append files to the extra_dist files.
#####
set(extra_dist "")
macro(add_extra_dist files)
  foreach(f ${files})
    set(extra_dist ${extra_dist} ${cmake_current_source_dir}/${f})
  endforeach()
endmacro()

# a script to print out information at the end of the configuration step.
macro(print_conf_summary)
    message("")
    message("")
    message(status "cmake summary:")
    message("")
    message(status "\tbuild type:\t\t\t" ${cmake_build_type})
    message(status "\tbuilding shared libraries:\t" ${enable_shared})
    if(cmake_prefix_path)
        message(status "\tcmake prefix path:\t\t" "${cmake_prefix_path}")
    endif()
    message(status "\tlinking against:\t\t" "${tll_libs}") 
    message("")
endmacro()

#####
# option checks
#####

# set the appropriate compiler/architecture for universal osx binaries.
if(${cmake_system_name} equal "darwin")
    set(cmake_osx_architectures i386;x86_64)
endif(${cmake_system_name} equal "darwin")

if(enable_coverage_tests)
    set(cmake_c_flags "${cmake_c_flags} -g -o0 -wall -w -fprofile-arcs -ftest-coverage")
    set(cmake_linker_flags "${cmake_linker_flags} -fprofile-arcs -ftest-coverage")
endif()

# option to build dll
if(win32)
    option (enable_dll "build a windows dll." ${build_shared_libs})
    if (enable_dll)
        set(build_dll on cache bool "")
        add_definitions(-ddll_oc)
        add_definitions(-ddll_export)
    endif ()
endif()

# option to use bundled xgetopt in place of getopt(). this is mostly useful
# for msvc builds. if not building utilities, getopt() isn't required at all.
if(msvc)
    option(enable_xgetopt "enable bundled xgetopt instead of external getopt()." on)
    if(enable_xgetopt)
        set(use_x_getopt on cache bool "")
    endif()
endif()

set(math "")

# enable tests

# option to enable remote tests.
option(enable_remote_tests "enable remote tests." on)

if(not msvc)
  option (enable_tests "enable basic tests, run with 'make test'." on)
  if(enable_tests) 
   set(build_testsets on cache bool "")
  endif()
endif()

option (enable_internal_docs "enable documentation of library internals. this is of interest only to those developing the netcdf library." off)
if(enable_internal_docs)
    set(build_internal_docs on)
endif()

# linux specific large file support flags.
# modelled after check in cmakelists.txt for hdf5.
if (not windows)
  option(enable_lfs "enable large (64-bit) files on linux." on)
  # todo: there needs to be a check here to ensure
  # that large file support is actually supported.
  
  if(enable_lfs)
  set(_file_offset_bits 64)
  set(_largefile64_source true)
  set(_largefile_source true)
  endif()
endif()

# provide the option to perform coverage tests.
option (enable_coverage_tests "enable compiler flags needed to perform coverage tests." off)
if(enable_coverage_tests)
    set(cmake_cxx_flags "${cmake_cxx_flags} -g -o0 -wall -w -profile-arcs -ftest-coverage")
    set(cmake_c_flags "${cmake_c_flags} -g -o0 -wall -w -fprofile-arcs -ftest-coverage")
    set(cmake_exe_linker_flags "${cmake_exe_linker_flags} -fprofile-arcs -ftest-coverage")
    message(status "coverage tests: on.")
endif()

option (enable_doxygen "enable generation of doxygen." off)
if(enable_doxygen)
    set(build_docs on cache bool "")
    option(enable_internal_docs "build internal documentation. this is of interest to developers only." off)
    if(enable_internal_docs)
        set(build_internal_docs yes cache string "")
    else()
        set(build_internal_docs no cache string "")
    endif()
endif()

# set some of the options as advanced.
mark_as_advanced(enable_internal_docs valgrind_tests enable_coverage_tests)
mark_as_advanced(enable_remote_tests use_remote_cdash)
#####
# end option checks.
#####

#####
# system inspection checks
#####
include_directories(".")
include_directories(${cmake_source_dir}/tests)
set(cmake_required_includes ${cmake_source_dir}/tests)

#
# library include checks
check_include_file("math.h"    have_math_h)
check_include_file("unistd.h"  have_unistd_h)

# taken from netcdf; this list needs updating for oc
check_include_file("alloca.h"  have_alloca_h)
check_include_file("malloc.h"  have_malloc_h)
check_include_file("ctype.h"   have_ctype_h)
check_include_file("dirent.h"  have_dirent_h)
check_include_file("dlfcn.h"   have_dlfcn_h)
check_include_file("errno.h"   have_errno_h)
check_include_file("fcntl.h"   have_fcntl_h)
check_include_file("getopt.h"  have_getopt_h)
check_include_file("stdbool.h"   have_stdbool_h)
check_include_file("locale.h"  have_local_h)
check_include_file("stdint.h"  have_stdint_h)
check_include_file("stdio.h"   have_stdio_h)
check_include_file("stdlib.h"  have_stdlib_h)
check_include_file("stdarg.h"  have_stdarg_h)
check_include_file("strings.h"   have_strings_h)
check_include_file("signal.h"  have_signal_h)
check_include_file("sys/dir.h"   have_sys_dir_h)
check_include_file("sys/ndir.h"  have_sys_ndir_h)
check_include_file("sys/param.h" have_sys_param_h)
check_include_file("sys/stat.h"  have_sys_stat_h)
check_include_file("sys/time.h"  have_sys_time_h)
check_include_file("sys/types.h" have_sys_types_h)
check_include_file("sys/wait.h"  have_sys_wait_h)
check_include_file("sys/resource.h" have_sys_resource_h)
check_include_file("fcntl.h"  have_fcntl_h)
check_include_file("inttypes.h"  have_inttypes_h)
check_include_file("pstdint.h"  have_pstdint_h)
check_include_file("endian.h"   have_endian_h)
check_include_file("basetsd.h"  have_basetsd_h)

# type checks
check_type_size("double"     sizeof_double)
check_type_size("float"      sizeof_float)
check_type_size("int"      sizeof_int)
check_type_size("long"       sizeof_long)
check_type_size("long long"    sizeof_long_long)
check_type_size("off_t"      sizeof_off_t)
check_type_size("off64_t"    sizeof_off64_t)
check_type_size("short"      sizeof_short)
check_type_size("size_t"     sizeof_size_t)
check_type_size("ssize_t"    sizeof_ssize_t)
check_type_size("uchar"      sizeof_uchar)
check_type_size("int64_t"    sizeof_int64_t)
check_type_size("uint64_t"     sizeof_uint64_t)

if(sizeof_ssize_t)
    set(have_ssize_t 1)
else()
    check_type_size("ssize_t"     sizeof_ssize_t)
    if(sizeof_ssize_t)
        set(have_ssize_t 1)
    endif()
endif()

# check for various functions. 
check_function_exists(fsync   have_fsync)
check_function_exists(strlcat   have_strlcat)
check_function_exists(strerror  have_strerror)
check_function_exists(snprintf  have_snprintf)
check_function_exists(strchr  have_strchr)
check_function_exists(strrchr   have_strrchr)
check_function_exists(strcat  have_strcat)
check_function_exists(strcpy  have_strcpy)
check_function_exists(strdup  have_strdup)
check_function_exists(strcasecmp    have_strcasecmp)
check_function_exists(strtod  have_strtod)
check_function_exists(strtoll   have_strtoll)
check_function_exists(strtoull  have_stroull)
check_function_exists(strstr  have_strstr)
check_function_exists(mkstemp   have_mkstemp)
check_function_exists(rand    have_rand)
check_function_exists(gettimeofday    have_gettimeofday)
check_function_exists(fsync   have_fsync)
check_function_exists(mpi_comm_f2c    have_mpi_comm_f2c)
check_function_exists(memmove   have_memmove)
check_function_exists(getpagesize     have_getpagesize)
check_function_exists(sysconf   have_sysconf)
check_function_exists(mremap  have_mremap)
check_function_exists(getrlimit have_getrlimit)

#####
# end system inspection checks.
#####

# check for the math library so it can be explicitely linked.
if(not win32)
     find_library(have_libm names math m libm)
     message(status "found math library: ${have_libm}")
     if(not have_libm)
        message(fatal_error "unable to find the math library.")
     endif()
endif()

# create config.h file
configure_file("${oc_source_dir}/config.h.in.cmake"
               "${oc_binary_dir}/config.h")

include_directories(${oc_binary_dir})
# end autotools-style checs for config.h

#####
# set core names of the libraries.
#####
set(oc_lib_corename    "oc")

#####
# set the true names of all the libraries, if customized by external project
#####
# recurse into other subdirectories.
add_subdirectory("tests")

# for tests and utilities, we are no longer
# exporting symbols but rather importing them.
if(build_dll)
    remove_definitions(-ddll_export)
endif()

#####
# build doxygen documentation, if need be.
#####
add_subdirectory(docs)

configure_file(
  ${cmake_current_source_dir}/fixbundle.cmake.in
  ${cmake_current_binary_dir}/fixbundle.cmake
  @only
)
#install(script ${cmake_current_binary_dir}/fixbundle.cmake)

configure_file (
    ${oc_source_dir}/oc-config.cmake.in
    ${oc_binary_dir}/oc-config.cmake @only)

configure_file (
    ${oc_source_dir}/oc-config-version.cmake.in
    ${oc_binary_dir}/oc-config-version.cmake @only)

# end cmake package configuration files.

#####
# various options for cpack
#####

##
# declare exclusions list used when building a source file.
# note!! this list uses regular expressions, not wildcards!! 
## 
set(cpack_source_ignore_files "${cpack_source_ignore_files}"
    "/tests/"
    "${cmake_binary_dir}/*"
    "/myhtml/*"
    "/.svn/"
    "my.*\\\\.sh"
    "/.deps/"
    "/.libs"
    "/html/"
    ".*\\\\.jar"
    ".*\\\\.jdl"
    ".*\\\\.sed"
    ".*\\\\.proto"
    ".*\\\\.texi"
    ".*\\\\.example"
    "make0"
    ".*~" 
)
configure_file(${cmake_current_source_dir}/copyright
    ${cmake_current_binary_dir}/copyright.txt
    @only
)

set(cpack_resource_file_license "${cmake_current_binary_dir}/copyright.txt")
if(not cpack_pack_version)
    set(cpack_package_version ${version})
endif()


if(apple)
  set(cpack_source_generator "tgz")
  set(cpack_generator "packagemaker" "stgz" "tbz2" "tgz" "zip")
endif()

# create an 'uninstall' target.
configure_file(
    "${cmake_current_source_dir}/cmake_uninstall.cmake.in"
    "${cmake_current_binary_dir}/cmake_uninstall.cmake"
    immediate @only)

add_custom_target(uninstall
    command ${cmake_command} -p ${cmake_current_binary_dir}/cmake_uninstall.cmake)

## customize some of the package component descriptions
set(cpack_component_utilities_display_name "oc utilities")
set(cpack_component_libraries_display_name "oc libraries")
set(cpack_component_headers_display_name "oc headers")
set(cpack_component_dependencies_display_name "oc dependencies")
set(cpack_component_documentation_display_name "oc documentation")

set(cpack_component_utilities_description
    "the ve utilities")
set(cpack_component_libraries_description
    "the ve libraries")
set(cpack_component_headers_description
    "c header files for use with oc")
set(cpack_component_dependencies_description
    "dependencies for this build of oc")
set(cpack_component_documentation_description
    "the ve user documentation.")

# create oc-config
file(make_directory ${oc_binary_dir}/tmp)
configure_file("${oc_source_dir}/oc-config.in.cmake"
  "${oc_binary_dir}/tmp/oc-config" @only)
file(copy "${oc_binary_dir}/tmp/oc-config"
  destination ${oc_binary_dir}/
  file_permissions owner_read owner_write owner_execute group_read group_execute world_read world_execute)
#execute_process(command "chmod 755 ${oc_binary_dir}/oc-config")

# enable makedist files.
add_makedist()
enable_makedist(
    copying
    copyright
    release_notes
    readme
    install
    install.cmake
    dap.y
    oc.spec
    ocprint.1
    cmake
    cmakelists.txt
    compile.cmake.txt
    config.h.in.cmake
    cmake_uninstall.cmake.in
    oc-config-version.cmake.in
    oc-config.cmake.in
    fixbundle.cmake.in
    oc-config.in.cmake
    configure
    configure.ac
    install-sh
    config.h.in
    config.sub)
    


##################################################
##################################################

set(liboc_sources oc.c daplex.c dapparse.c daptab.c ocbytes.c occlientparams.c occompile.c occurlfunctions.c ocdata.c ocdebug.c ocdump.c ochttp.c ocinternal.c oclist.c oclog.c ocnode.c ocrc.c ocread.c ocuri.c ocutil.c xxdr.c)

add_library(oc ${lib_type} ${liboc_sources}) 
if(mod_oc_name)
  set_target_properties(oc properties library_output_name ${oc_lib_name})
  set_target_properties(oc properties archive_output_name ${oc_lib_name})
  set_target_properties(oc properties runtime_output_name ${oc_lib_name})
endif()

file(glob cur_extra_dist relative ${cmake_current_source_dir} ${cmake_current_source_dir}/*.h ${cmake_current_source_dir}/*.c )
set(cur_extra_dist ${cur_extra_dist} dap.y)
set(cur_extra_dist ${cur_extra_dist} cmakelists.txt makefile.am)
add_extra_dist("${cur_extra_dist}")

if(mod_oc_name)
    set_target_properties(oc properties library_output_name ${oc_lib_name})
    set_target_properties(oc properties archive_output_name ${oc_lib_name})
    set_target_properties(oc properties runtime_output_name ${oc_lib_name})
endif()

set_target_properties(oc properties 
	version ${oc_version}
	soversion ${oc_so_version})

#####
# build doxygen documentation, if need be.
#####
if(build_docs)
    find_package(doxygen)
    if(doxygen_found)
        file(glob copy_files ${cmake_current_source_dir}/*.html ${cmake_current_source_dir}/images ${cmake_current_source_dir}/*.doc ${cmake_current_source_dir}/*.xml ${cmake_current_source_dir}/*.m4 ${cmake_current_source_dir}/*.texi)
        file(copy ${copy_files} destination ${cmake_current_binary_dir}/)
        # copy the image files used by markdown documentation 
        # manually.
        file(glob img_files ${cmake_current_binary_dir}/images/*.jpg)
        configure_file(${cmake_current_source_dir}/doxyfile.in.cmake
        ${cmake_current_binary_dir}/doxyfile @only)
        add_custom_target(doc all
            ${doxygen_executable} ${cmake_current_binary_dir}/doxyfile
            working_directory ${cmake_current_binary_dir}
            comment "generating api documentation with doxygen" verbatim
        )
        file(copy ${img_files} destination ${cmake_current_binary_dir}/html/)
    endif()
endif()

set(ocprint_sources ocprint.c)

if(use_x_getopt)
	set(ocprint_sources ${ocprint_sources} xgetopt.c)
endif()

add_executable(ocprint ${ocprint_sources} ${liboc_sources})

target_link_libraries(ocprint ${tll_libs})
target_link_libraries(oc ${tll_libs})

print_conf_summary()

################################
# install packages.
################################

# install oc-config
install(programs ${oc_binary_dir}/oc-config destination bin component utilities)

# install liboc
install(
        targets oc
        export oc-targets
        runtime
        destination bin
        component libraries 
        library destination ${cmake_install_libdir} component libraries 
        archive destination ${cmake_install_libdir} component libraries 
    )

# install ocprint
install(programs ${oc_binary_dir}/ocprint.exe destination bin component utilities)

# create cmake package configuration files. with these, other packages using
# cmake should be able to find ve using find_package and find_library.
install (export oc-targets
         destination share/cmake
         component documentation)

install(files ${oc_source_dir}/oc.h
    destination include component headers)

# install the dependencies.
if(msvc)
    install(directory ${cmake_prefix_path} destination "deps" component dependencies)
endif()

if(enable_doxygen)    
    install(directory docs destination "." component documentation)
endif()

install (files ${oc_binary_dir}/oc-config.cmake
        ${oc_binary_dir}/oc-config-version.cmake
        destination share/cmake)

################################
# set ctest properties
################################

enable_testing()
include(ctest)

# copy the ctest customization file into binary directory, as required.
file(copy ${cmake_source_dir}/ctestcustom.cmake destination ${cmake_binary_dir})

# set memory test program for non-msvc based builds.
# assume valgrind for now.
if(not msvc)
  set(ctest_memorycheck_command valgrind cache string "")
endif()

# enable tests
if(enable_tests)
    add_subdirectory(tests)
endif()

# set variable to define the build type.
include(generateexportheader)

################################
# end ctest properties
################################

##################################################
##################################################

# cpack inclusion must come last.
include(cpack)
