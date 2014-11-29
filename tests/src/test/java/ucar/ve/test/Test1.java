/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.test;

import org.apache.commons.cli.*;
import ucar.ve.*;

import java.util.*;
import java.io.*;

import static ucar.ve.Types.*;
import static ucar.ve.VE.*;

public class Test1
{

    //////////////////////////////////////////////////
    // Constants

    static final boolean DEBUG = true;
    static final boolean PARSEDEBUG = true;

    //////////////////////////////////////////////////
    // Input format Capabilities

    static protected class Format
    {
        public String[] names;
        public String classname;

        Format(String[] names, String classname)
        {
            this.names = names;
            this.classname = classname;
        }
    }

    // Define a map of -F format tags to class name tags
    // Names are case insensitive, so always use lower case here.
    static final Format[] formatmap = new Format[]{
        // Note, the format tag is tested as lower case
        // while the class name tag is used as is.
        // The class name must be fully qualified
        //new Format(new String[]{"xml", "x"}, "ucar.ve.XmlParser"),
        new Format(new String[]{"ve"}, "ucar.ve.VEParser"),
    };
    static final String DEFAULTFORMAT = "ve";

    //////////////////////////////////////////////////
    // Static variables

    //////////////////////////////////////////////////
    // Static methods 

    static Configuration
    getopts(String[] argv)
        throws VEException
    {
        // Get command line options
        Options options = new Options();

        options.addOption("V", false, "verbose");
        options.addOption("f", true, "input file name");
        options.addOption("o", true, "output file name");
        options.addOption("I", true, "specify package to search for verbs");
        options.addOption("L", true, "specify the output language; defaults to Java");
        options.addOption("F", true, "specify the input format; defaults to .ve");
        options.addOption("W", true, "specify various subsidiary debug options:\n"
            + "'d' -- turn on general debugging"
            + "'p' -- turn on parsing debug output");
        try {
            CommandLineParser clparser = new PosixParser();
            CommandLine cmd = null;
            String[] other = null;
            try {
                cmd = clparser.parse(options, argv);
                other = cmd.getArgs(); // unused args
            } catch (ParseException pe) {
                System.err.println("Command line parse failure: " + pe.getMessage());
                usage();
                System.exit(1);
            }

            Configuration cfg = new Configuration();

            if(cmd.hasOption("H")) {
                usage();
                System.exit(1);
            }

            if(cmd.hasOption("v"))
                cfg.verbose = true;

            if(cmd.hasOption("f") || other.length > 0) {
                String infile = cmd.getOptionValue("f");
                if(infile == null)
                    infile = other[0];
                // verify readability of input file
                File ifile = new File(infile);
                if(!ifile.canRead())
                    fatal("Input file not readable: " + ifile);
                cfg.input = Util.readfile(infile);
            }

            if(cmd.hasOption("o")) {
                String outfile = cmd.getOptionValue("o");
                // verify writeability of output file
                if(outfile != null) {
                    File ofile = new File(outfile);
                    if(!ofile.canWrite())
                        fatal("Output file not writeable: " + ofile);
                }
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outfile), Util.UTF8);
                cfg.output = new PrintWriter(osw);
            }

            String fflag = DEFAULTFORMAT;
            if(cmd.hasOption("F")) {
                String format = cmd.getOptionValue("F");
                fflag = format.toLowerCase();
            }
            // Map the -F format to the class tag
            String classFormatTag = null;
            floop:
            for(int i = 0;i < formatmap.length;i++) {
                Format fmt = formatmap[i];
                for(String s : fmt.names) {
                    if(fflag.equals(s)) {
                        classFormatTag = fmt.classname;
                        break floop;
                    }
                }
            }
            if(classFormatTag == null) {
                fatal("Unsupported format: " + fflag);
            }
            try {
                cfg.format = AbstractTest.class.getClassLoader().loadClass(classFormatTag);
            } catch (ClassNotFoundException cnfe) {
                fatal("No such class: " + classFormatTag);
            }

            if(cmd.hasOption("W")) {
                String value = cmd.getOptionValue("W");
                if(value == null) value = "";
                for(int i = 0;i < value.length();i++) {
                    char c = value.charAt(i);
                    switch (c) {
                    case 'd':
                        cfg.debug = true;
                        break;
                    case 'p':
                        cfg.parsedebug = true;
                        break;
                    default:
                        break;
                    }
                }
            }

            // Overrides
            if(DEBUG)
                cfg.debug = true;
            if(PARSEDEBUG)
                cfg.parsedebug = true;
            return cfg;

        } catch (Exception e) {
            Util.runtimeCheck(e);
            throw new VEException(e);
        }
    }

    static void fatal(Exception e)
    {
        if(DEBUG)
            e.printStackTrace();
        fatal(e.getMessage());
    }

    static void fatal(String msg)
    {
        System.err.println(msg);
        System.err.println("Use -H flag for help");
        System.err.flush();
        System.exit(1);
    }

    static void usage()
    {
        System.err.print(
            "usage: java -jar ve.jar <options>*%n"
                + "where the options are:%n"
                + "-H              -- display this message and exit.%n"
                + "-V              -- verbose.%n"
                + "-o path         -- name of the generated output%n"
                + "-I packagenames  -- specify import package(s) from which to import verb classes;%n"
                + "                   the package name can be a list that is colon separated%n"
                + "-L language     -- specify the generated output language%n"
                + "-F input-format -- specify the input format%n"
                + "                   (currently only the ve format is suppported)%n"
                + "-W woption      -- specify various debug options:%n"
                + "                   'd' -- turn on general debugging."
                + "                   'p' -- turn on parsing debug output."
        );
        System.err.flush();
    }

    //////////////////////////////////////////////////
    // Main

    static public void
    main(String[] argv)
    {
        int exitcode = 0;
        try {
            Configuration cfg = getopts(argv);
            cfg.verbs = Test1Verbs.getVerbs();
            VE interpreter = new VE(cfg);
            Object state = new StringBuilder();
            interpreter.evaluate(state);
            System.out.printf("Result=!%s!",((StringBuilder)state).toString());
        } catch (VEException ce) {
            System.err.println("VE failed: " + ce.getMessage());
            int lineno = ce.getErrorLine();
            int charno = ce.getErrorChar();
            if(lineno < 0)
                System.err.println("Location: unknown");
            else
                System.err.printf("Location: line=%d char=%d%n", lineno, charno);
            if(DEBUG)
                ce.printStackTrace();
            exitcode = 1;
        } catch (Exception e) {
            System.err.println("VE failed: " + e.getMessage());
            e.printStackTrace();
            exitcode = 1;
        }
        System.exit(exitcode);
    }

}

