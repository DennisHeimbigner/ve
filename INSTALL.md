#Introduction

The installation is partitioned into two parts. Most of the code is an ant based builder for the compiler, the Java runtime, and Java test cases. The "c" subdirectory contains a Make based implementation of the C runtime and tests. One should use the build.xml to build and install the Java-based compiler and use
the ./configure, make process to build the C-code run-time.

# Java Install
1. Invoke the "ant" command to build the Java-based compiler
   and runtime. This will produce two jar files:
   * compiler/target/ast.jar -- the compiler.
   * runtime/target/runtime.jar -- the Java runtime support.
   You may install there where ever convenient.

2. If desired, invoke "ant test" to run the Java test cases.

# C Install
1. Enter the "c" subdirectory.
2. invoke "./configure"
3. invoke "make"
4. invoke "make check"

The result will be a libast.a file.

# Using the compiler

## For C language output:
1. Invoke the compiler using a command similar to this:
       java -Lc -jar ast.jar XXX.proto
   This compiles XXX.proto and produces XXX.{c,h}.

2. Invoke (e.g.) gcc to compile and load a test program.
	gcc -o test.exe testast.c XXX.c -L${prefix}/lib -last

## For Java language output:
1. Invoke the compiler using a command similar to this:
       java -Ljava -jar ast.jar XXX.proto
   This compiles XXX.proto and produces XXX.java.

2. Invoke (e.g.) javac to compile XXX.java:
	javac -classpath ".:runtime.jar" 

Remember that you will need to include runtime.jar in your
class path when executing a program that is using protobuf.

