/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.test;

import ucar.ve.*;

import java.util.*;

import static ucar.ve.Types.*;

abstract public class Test1Verbs
{
    static final String LPAREN = "(";
    static final String RPAREN = ")";

    static protected List<VerbDef> verbs;

    static public final List<VerbDef> getVerbs()
    {
        return verbs;
    }

    static {
        verbs = new ArrayList<VerbDef>();
        verbs.add(new VerbDef(Startofline.VerbName, Startofline.class));
        verbs.add(new VerbDef(Endofline.VerbName, Endofline.class));
        verbs.add(new VerbDef(Find.VerbName, Find.class));
        verbs.add(new VerbDef(Then.VerbName, Then.class));
        verbs.add(new VerbDef(Maybe.VerbName, Maybe.class));
        verbs.add(new VerbDef(Anything.VerbName, Anything.class));
        verbs.add(new VerbDef(Anythingbut.VerbName, Anythingbut.class));
        verbs.add(new VerbDef(Anythingbutnot.VerbName, Anythingbutnot.class));
        verbs.add(new VerbDef(Something.VerbName, Something.class));
        verbs.add(new VerbDef(Somethingbut.VerbName, Somethingbut.class));
        verbs.add(new VerbDef(Linebreak.VerbName, Linebreak.class));
        verbs.add(new VerbDef(BR.VerbName, BR.class));
        verbs.add(new VerbDef(Tab.VerbName, Tab.class));
        verbs.add(new VerbDef(Word.VerbName, Word.class));
        verbs.add(new VerbDef(Anyof.VerbName, Anyof.class));
        verbs.add(new VerbDef(Any.VerbName, Any.class));
        verbs.add(new VerbDef(Or.VerbName, Or.class));
        verbs.add(new VerbDef(Begincapture.VerbName, Begincapture.class));
        verbs.add(new VerbDef(Endcapture.VerbName, Endcapture.class));
        verbs.add(new VerbDef(Begin.VerbName, Begin.class));
        verbs.add(new VerbDef(End.VerbName, End.class));
        verbs.add(new VerbDef(Either.VerbName, Either.class));
        verbs.add(new VerbDef(Stop.VerbName, Stop.class));
    }

    //////////////////////////////////////////////////
    // Initial set of Verbs; 
    // The verb names are intended to be case insensitive.
    // These are all static, buf if the Verbs class state
    // is needed then they can be defined as non-static.

    static public class Startofline extends Verb
    {
        static public final String VerbName = "startofline";

        public Startofline()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append("^");
        }
    }

    static public class Endofline extends Verb
    {
        static public final String VerbName = "endofline";

        public Endofline()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append("$");
        }
    }

    static public class Find extends Verb
    {
        static public final String VerbName = "find";

        public Find()
        {
            super(VerbName, ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(".*" + args.getString(0));
        }
    }

    static public class Then extends Verb
    {
        static public final String VerbName = "then";

        public Then()
        {
            super(VerbName, ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(args.getString(0));
        }
    }

    static public class Maybe extends Verb
    {
        static public final String VerbName = "maybe";

        public Maybe()
        {
            super(VerbName, ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            String arg = args.getString(0);
            switch (arg.length()) {
            case 0:
                throw new VEException("Maybe: zero length argument");
            case 1:
                buf.append(arg + "?");
                break;
            default:
                buf.append(String.format("(%s)?", arg));
                break;
            }
        }
    }

    static public class Anything extends Verb
    {
        static public final String VerbName = "anything";

        public Anything()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(".*");
        }
    }

    static public class Anythingbut extends Verb
    {
        static public final String VerbName = "anythingbut";

        public Anythingbut()
        {
            super(VerbName, ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(String.format("[^%s]*", args.getString(0)));
        }
    }

    static public class Anythingbutnot extends Verb
    {
        static public final String VerbName = "anythingbutnot";

        public Anythingbutnot()
        {
            super(VerbName, ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(String.format("[^%s]*", args.getString(0)));
        }
    }

    static public class Something extends Verb
    {
        static public final String VerbName = "something";

        public Something()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(".+");
        }
    }

    static public class Somethingbut extends Verb
    {
        static public final String VerbName = "somethingbut";

        public Somethingbut()
        {
            super(VerbName, ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(String.format("[^%s]+", args.getString(0)));
        }
    }

    static public class Linebreak extends Verb
    {
        static public final String VerbName = "linebreak";

        public Linebreak()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append("[\r]?[\n]");
        }
    }

    static public class BR extends Verb
    {
        static public final String VerbName = "br";

        public BR()
        {
            super(VerbName); // same as linebreak
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append("[\r]?[\n]");
        }
    }

    static public class Tab extends Verb
    {
        static public final String VerbName = "tab";

        public Tab()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append("[\t]");
        }
    }

    static public class Word extends Verb
    {
        static public final String VerbName = "word";

        public Word()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append("\\w+");
        }
    }

    static public class Anyof extends Verb
    {
        static public final String VerbName = "anyof";

        public Anyof()
        {
            super(VerbName, ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(String.format("[%s]", args.getString(0)));
        }
    }

    static public class Any extends Verb
    {
        static public final String VerbName = "any";

        public Any()
        {
            super(VerbName, ArgType.STRING); //same as anyof
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(String.format("[%s]", args.getString(0)));
        }
    }

    static public class Or extends Verb
    {
        static public final String VerbName = "or";

        public Or()
        {
            super(VerbName);
        }


        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append("|");
        }
    }

    static public class Begincapture extends Verb
    {
        static public final String VerbName = "begincapture";

        public Begincapture()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(LPAREN);
        }
    }

    static public class Endcapture extends Verb
    {
        static public final String VerbName = "endcapture";

        public Endcapture()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(RPAREN);
        }
    }

    static public class Begin extends Verb
    {
        static public final String VerbName = "begin";

        public Begin()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(LPAREN);
        }
    }

    static public class End extends Verb
    {
        static public final String VerbName = "end";

        public End()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append(RPAREN);
        }
    }

    //////////////////////////////////////////////////
    // Extended verb set

    static public class Either extends Verb
    {
        static public final String VerbName = "either";

        public Either()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append("");
        }
    }

    static public class Stop extends Verb
    {
        static public final String VerbName = "stop";
        static public final Signature signature = new Signature();

        public Stop()
        {
            super(VerbName);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = (StringBuilder) state;
            buf.append("");
        }
    }

}
