/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import org.apache.commons.cli.*;

import java.util.*;
import java.io.*;

import static ucar.ve.compiler.Types.*;

public class Main
{

    //////////////////////////////////////////////////
    // Constants

    static final boolean DEBUG = true;
    static final boolean PARSEDEBUG = false;

    //////////////////////////////////////////////////
    // Output Language Capabilities

    static protected class Language
    {
        String[] names;
        String classname;

        Language(String[] names, String classname)
        {
            this.names = names;
            this.classname = classname;
        }
    }

    // Define a map of -L language tags to class name tags
    // Names are case insensitive, so always use lower case here.
    static final Language[] languagemap = new Language[]{
        // Note, the language tag is tested as lower case
        // while the class name tag is used as is.
        new Language(new String[]{"javaregexp", "jre"}, "JavaREGenerator"),
        new Language(new String[]{"java", "j"}, "JavaGenerator"),
        new Language(new String[]{"c"}, "CGenerator"),
        new Language(new String[]{"python", "py", "p"}, "PythonGenerator"),
    };
    static final String DFALTLANGUAGE = "javaregexp";

    //////////////////////////////////////////////////
    // Input format Capabilities

    static protected class Format extends Language
    {
        Format(String[] name, String classname)
        {
            super(name, classname);
        }
    }

    // Define a map of -F format tags to class name tags
    // Names are case insensitive, so always use lower case here.
    static final Format[] formatmap = new Format[]{
        // Note, the format tag is tested as lower case
        // while the class name tag is used as is.
        new Format(new String[]{"xml", "x"}, "XmlParser"),
        new Format(new String[]{"ve"}, "VEParser"),
    };
    static final String DFALTFORMAT = "ve";

    static List<String> importPaths = new ArrayList<String>();

    // Getters
    static public boolean getDebug()
    {
        return configuration.debug;
    }

    static public boolean getOptionTrace()
    {
        return configuration.trace;
    }

    //////////////////////////////////////////////////
    // Type declarations

    static public class Configuration
    {
        // Local copies of the command line options
        public String[] other = null; // remaining non-option arguments
        public Map<String, String> defines = new HashMap<String, String>(); // -D cl options
        public String inputfile = null;
        public String outputfile = null;
        public boolean verbose = false;
        public List<String> imports = new ArrayList<String>();
        public String language = DFALTLANGUAGE;
        public String format = DFALTFORMAT;
        // Debug options
        public boolean debug = false; // d
        public boolean parseDebug = false; // p
        public boolean semanticsDebug = false; // s
        public boolean semanticStepsDebug = false; // t
        public boolean duplicate = false; //D
        public boolean trace = false;
    }

    static public Configuration configuration = new Configuration();

    //////////////////////////////////////////////////
    // Static variables

    static String DEFAULTPACKAGE = null;
    static {
	Class cl = Main.class;
	Package pack = cl.getPackage();
	DEFAULTPACKAGE = pack.getName();
    };

    //////////////////////////////////////////////////
    // Static methods 

    static Configuration
    getopts(String[] argv)
    {
        // Get command line options
        Options options = new Options();

        options.addOption("V", false, "verbose");
        options.addOption("f", true, "input file name");
        options.addOption("o", true, "output file name");
        options.addOption("I", true, "specify import file to import in .c output; the file name may optionally be surrounded by <...>");
        options.addOption("L", true, "specify the output language; defaults to Java");
        options.addOption("F", true, "specify the input format; defaults to .ve");
        options.addOption("W", true, "specify various subsidiary debug options:\n"
            + "'d' -- turn on general debugging"
            + "'p' -- turn on parsing debug output"
            + "'t' -- track semantic steps"
            + "'s' -- general semantics debug"
            + "'D' -- check for duplicates");

        CommandLineParser clparser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = clparser.parse(options, argv);
        } catch (ParseException pe) {
            System.err.println("Command line parse failure: " + pe.getMessage());
            usage();
            System.exit(1);
        }

        Configuration cloptions = new Configuration();
        if(cmd.hasOption("v"))
            cloptions.verbose = true;
        if(cmd.hasOption("f"))
            cloptions.inputfile = cmd.getOptionValue("f");
        if(cmd.hasOption("o"))
            cloptions.outputfile = cmd.getOptionValue("o");
        if(cmd.hasOption("I"))
            cloptions.imports.add(cmd.getOptionValue("I"));
        if(cmd.hasOption("L"))
            cloptions.language = cmd.getOptionValue("L");
        if(cmd.hasOption("F"))
            cloptions.format = cmd.getOptionValue("F");
        if(cmd.hasOption("D")) {
            // Depending on how java is invoked, -D command line
            // options may or may not be absorbed by the jvm; specifically
            // they will appear here is 'java -jar' was used.
            // If encountered here, then stash them in the defines map.
            String nv = cmd.getOptionValue("X");
            String[] split = nv.split("[=]");
            cloptions.defines.put(split[0], split[1]);
        }
        if(cmd.hasOption("W")) {
            String value = cmd.getOptionValue("W");
            if(value == null) value = "";
            for(int i = 0;i < value.length();i++) {
                char c = value.charAt(i);
                switch (c) {
                case 'd':
                    cloptions.debug = true;
                    break;
                case 'p':
                    cloptions.parseDebug = true;
                    break;
                case 't':
                    cloptions.semanticStepsDebug = true;
                    break;
                case 's':
                    cloptions.semanticsDebug = true;
                    break;
                case 'D':
                    cloptions.duplicate = true;
                    break;
                default:
                    break;
                }
            }
        }
        cloptions.other = cmd.getArgs();
        if(cloptions.inputfile == null) {
            if(cloptions.other.length > 0)
                cloptions.inputfile = cloptions.other[0];
        }

        // Collect java -D options of interest
        String nv = System.getProperty("X");
        if(nv != null) {
            String[] split = nv.split("[=]");
            cloptions.defines.put(split[0], split[1]);
        }

        return cloptions;
    }

    static public String pathfix(String path)
    {
        if(path == null)
            path = "";
        path = path.trim();
        path = path.replace('\\', '/');
        if(path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        return path;
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
            "usage: java -jar ast.jar <options>*\n"
                + "where the options are:\n"
                + "-V              -- verbose.\n"
                + "-o path         -- path into which to generated files"
                + "-D option=value -- define global options.\n"
                + "-I import-file -- specify import file to import in .c output;\n"
                + "                   the file name may optionally be surrounded by <...>.\n"
                + "-L language     -- specify the output language;\n"
                + "                   currently only Java is suppported.\n"
                + "-F input-format -- specify the input format;\n"
                + "                   currently only the ve format is suppported.\n"
                + "-W woption      -- specify various subsidiary debug options:\n"
                + "                   'd' -- turn on general debugging."
                + "                   'p' -- turn on parsing debug output."
                + "                   't' -- track semantic steps."
                + "                   's' -- general semantics debug"
                + "                   'D' -- check for duplicates\n"
        );
        System.err.flush();
    }


    //////////////////////////////////////////////////
    // Main

    static public void
    main(String[] argv)
    {
        int exitcode = 0;
        Verbs.initializeVerbs();
        try {

            String classLanguageTag = null;
            String classFormatTag = null;
            Main.configuration = getopts(argv);

            // Map the -L language to the class tag
            String lflag = configuration.language.toLowerCase();
            for(int i = 0;i < languagemap.length;i++) {
                Language lang = languagemap[i];
                for(String s : lang.names) {
                    if(lflag.equals(s)) {
                        classLanguageTag = lang.classname;
                        break;
                    }
                }
            }
            if(classLanguageTag == null) {
                fatal("Unsupported language: " + configuration.language);
            }

            // Map the -F format to the class tag
            String fflag = configuration.format.toLowerCase();
            for(int i = 0;i < formatmap.length;i++) {
                Format format = formatmap[i];
                for(String s : format.names) {
                    if(fflag.equals(s)) {
                        classFormatTag = format.classname;
                        break;
                    }
                }
            }
            if(classFormatTag == null) {
                fatal("Unsupported format: " + configuration.format);
            }

            // verify writeability of output file
            if(configuration.outputfile != null) {
                File ofile = new File(configuration.outputfile);
                if(!ofile.canWrite())
                    fatal("Output file not writeable: " + ofile);
            }

            // verify readability of input file
            if(configuration.inputfile != null) {
                File ifile = new File(configuration.inputfile);
                if(!ifile.canRead())
                    fatal("Input file not readable: " + ifile);
            }

            // Create output writer
            PrintWriter output;
            if(configuration.outputfile == null)
                output = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
            else {
                // Guarantee UTF-8 output
                FileOutputStream fos = new FileOutputStream(configuration.outputfile);
                output = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
            }

            // Read in the whole input file 
            String text = Util.readfile(configuration.inputfile);

            // Create the input parser
            Parser parser = (Parser) Util.createClassInstance(classFormatTag,null);
            if(PARSEDEBUG || configuration.parseDebug)
                parser.setDebugLevel(1);
            boolean pass = parser.parse(text);
            if(!pass) {
                fatal("Parse failed");
            }

            // Do semantic processing
            ActionList program = parser.getProgram();
            Semantics sem = new Semantics();
            sem.process(program);

            // Create the generator
            Generator generator = (Generator)Util.createClassInstance(classLanguageTag,null);
            // generate the language-specific output
            generator.generate(parser.getProgram(), output);

        } catch (CompileException ce) {
            System.err.println("Compiler failed: " + ce.getMessage());
            int lineno = ce.getErrorLine();
            int charno = ce.getErrorChar();
            if(lineno < 0)
                System.err.println("Location: unknown");
            else
                System.err.printf("Location: line=%d char=%d\n", lineno, charno);
            exitcode = 1;
        } catch (Exception e) {
            System.err.println("Compiler failed: " + e.getMessage());
                e.printStackTrace();
            exitcode = 1;
        }
        System.exit(exitcode);
    }

}

