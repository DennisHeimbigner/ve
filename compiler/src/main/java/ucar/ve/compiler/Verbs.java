
/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import net.sf.corn.cps.CPScanner;
import net.sf.corn.cps.ClassFilter;

import java.util.*;

import static ucar.ve.compiler.Types.*;

abstract public class Verbs
{

    //////////////////////////////////////////////////
    // Constants

    static final boolean DEBUG = true;

    /**
     * The default set defined at
     * https://github.com/VerbalExpressions/implementation
     */

    static public Map<String, Verb> verbs = new HashMap<String, Verb>();

    static protected void defineVerb(Verb verb)
    {
        if(verbs.containsKey(verb.getVerb()))
            System.err.println("Duplicate verb: " + verb.getVerb());
        else
            verbs.put(verb.getVerb(), verb);
    }

    static void
    importVerb(Class verbclass)
        throws CompileException
    {
        if(DEBUG)
            System.err.println("Loading " + verbclass.toString());
        Verb verb;
        try {
            verb = (Verb) Util.createClassInstance(verbclass);
        } catch (Exception e) {
            Util.runtimeCheck(e);
            throw new CompileException("Undefined Verb class: " + verbclass.toString(), e);
        }

        // is this already defined?
        String name = verb.getVerb();
        if(verbs.get(name) != null)
            System.err.println("Duplicate verb definition" + name + "; overriding");

        defineVerb(verb);
    }

    /**
     * Load verbs from a specified package
     */

    static void
    importVerbs(String _package)
        throws CompileException
    {
        // Warning: this code causes internal NullPointerExceptions
        List<Class<?>> verbclasses
            = CPScanner.scanClasses(new ClassFilter()
                .packageName(_package)
                .superClass(Verb.class)
        );
        for(Class cl : verbclasses)
            importVerb(cl);
    }

    /**
     * Load the initial set of verbs as defined in
     * the classpath
     */
    static public void initializeVerbs()
    {
        try {
            importVerbs("ucar.ve.compiler");
        } catch (CompileException ce) {
            System.err.println("Could not load initial verbs");
            System.exit(1);
        }
    }

//////////////////////////////////////////////////
// Initial set of Verbs; The class name must canonically
// be all lower case, except the first character, which is upper
// case. The verb names are intended to be case insensitive.

    static public class Startofline extends Verb
    {
        public Startofline()
        {
            super("startofline");
        }
    }

    static public class Endofline extends Verb
    {
        public Endofline()
        {
            super("endofline");
        }
    }

    static public class Find extends Verb
    {
        public Find()
        {
            super("find", ArgType.STRING);
        } //find all occurrences => backtrack
    }

    static public class Then extends Verb
    {
        public Then()
        {
            super("then", ArgType.STRING);
        } //same as find
    }

    static public class Maybe extends Verb
    {
        public Maybe()
        {
            super("maybe", ArgType.STRING);
        }
    }

    static public class Anything extends Verb
    {
        public Anything()
        {
            super("anything");
        }
    }

    static public class Anythingbut extends Verb
    {
        public Anythingbut()
        {
            super("anythingbut", ArgType.STRING);
        }
    }

    static public class Anythingbutnot extends Verb
    {
        public Anythingbutnot()
        {
            super("anythingbutnot", ArgType.STRING);
        }
    }

    static public class Something extends Verb
    {
        public Something()
        {
            super("something");
        }
    }

    static public class Somethingbut extends Verb
    {
        public Somethingbut()
        {
            super("somethingbut", ArgType.STRING);
        }
    }

    static public class Linebreak extends Verb
    {
        public Linebreak()
        {
            super("linebreak");
        }
    }

    static public class BR extends Verb
    {
        public BR()
        {
            super("br"); // same as linebreak;}
        }
    }

    static public class Tab extends Verb
    {
        public Tab()
        {
            super("tab");
        }
    }

    static public class Word extends Verb
    {
        public Word()
        {
            super("word");
        }
    }

    static public class Anyof extends Verb
    {
        public Anyof()
        {
            super("anyof", ArgType.STRING);
        }
    }

    static public class Any extends Verb
    {
        public Any()
        {
            super("any", ArgType.STRING); //same as anyof;}
        }
    }

    static public class Or extends Verb
    {
        public Or()
        {
            super("or"); // alternative;}
        }
    }

    static public class Begincapture extends Verb
    {
        public Begincapture()
        {
            super("begincapture");
        }
    }

    static public class Endcapture extends Verb
    {
        public Endcapture()
        {
            super("endcapture");
        }
    }

    static public class Begin extends Verb
    {
        public Begin()
        {
            super("begin");
        }
    }

    static public class End extends Verb
    {
        public End()
        {
            super("end");
        }
    }

//////////////////////////////////////////////////
// Extended verb set

    static public class Either extends Verb
    {
        public Either()
        {
            super("either");
        }
    }

    static public class Stop extends Verb
    {
        public Stop() {super("stop");}
    }

    static public class Import extends Verb
    {
        public Import()
        {
            super("import", ArgType.STRING);
        }
    }
}
