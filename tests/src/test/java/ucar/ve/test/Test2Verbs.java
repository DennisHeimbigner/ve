/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.test;

import ucar.ve.*;

import java.util.*;

import static ucar.ve.Types.*;

abstract public class Test2Verbs
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
        verbs.add(new VerbDef("startofline", Startofline.class));
        verbs.add(new VerbDef("endofline", Endofline.class));
        verbs.add(new VerbDef("find", Find.class));
        verbs.add(new VerbDef("then", Then.class));
        verbs.add(new VerbDef("maybe", Maybe.class));
        verbs.add(new VerbDef("anything", Anything.class));
        verbs.add(new VerbDef("anythingbut", Anythingbut.class));
        verbs.add(new VerbDef("anythingbutnot", Anythingbutnot.class));
        verbs.add(new VerbDef("something", Something.class));
        verbs.add(new VerbDef("somethingbut", Somethingbut.class));
        verbs.add(new VerbDef("linebreak", Linebreak.class));
        verbs.add(new VerbDef("br", BR.class));
        verbs.add(new VerbDef("tab", Tab.class));
        verbs.add(new VerbDef("word", Word.class));
        verbs.add(new VerbDef("anyof", Anyof.class));
        verbs.add(new VerbDef("any", Any.class));
        verbs.add(new VerbDef("or", Or.class));
        verbs.add(new VerbDef("begincapture", Begincapture.class));
        verbs.add(new VerbDef("endcapture", Endcapture.class));
        verbs.add(new VerbDef("begin", Begin.class));
        verbs.add(new VerbDef("end", End.class));
        verbs.add(new VerbDef("either", Either.class));
        verbs.add(new VerbDef("stop", Stop.class));
    }

    //////////////////////////////////////////////////

    static public class Test2State
    {
        StringBuilder buf = new StringBuilder();

        public Test2State()
        {
        }

        public String toString()
        {
            return buf.toString();
        }

	public StringBuilder getBuf() {return this.buf;}
    }

    //////////////////////////////////////////////////
    // Initial set of Verbs; 
    // The verb names are intended to be case insensitive.
    // These are all static, buf if the Verbs class state
    // is needed then they can be defined as non-static.

    static public class Startofline extends Verb
    {
        public Startofline()
	    throws VEException
        {
            super("startofline");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append("^");
        }
    }

    static public class Endofline extends Verb
    {
        public Endofline()
	    throws VEException
        {
            super("endofline");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append("$");
        }
    }

    static public class Find extends Verb
    {
        public Find()
	    throws VEException
        {
            super("find", ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(".*" + args.getString(0));
        }
    }

    static public class Then extends Verb
    {
        public Then()
	    throws VEException
        {
            super("then", ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(args.getString(0));
        }
    }

    static public class Maybe extends Verb
    {
        public Maybe()
	    throws VEException
        {
            super("maybe", ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
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
        public Anything()
	    throws VEException
        {
            super("anything");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(".*");
        }
    }

    static public class Anythingbut extends Verb
    {
        public Anythingbut()
	    throws VEException
        {
            super("anythingbut", ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(String.format("[^%s]*", args.getString(0)));
        }
    }

    static public class Anythingbutnot extends Verb
    {
        public Anythingbutnot()
	    throws VEException
        {
            super("anythingbutnot", ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(String.format("[^%s]*", args.getString(0)));
        }
    }

    static public class Something extends Verb
    {
        public Something()
	    throws VEException
        {
            super("something");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(".+");
        }
    }

    static public class Somethingbut extends Verb
    {
        public Somethingbut()
	    throws VEException
        {
            super("somethingbut", ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(String.format("[^%s]+", args.getString(0)));
        }
    }

    static public class Linebreak extends Verb
    {
        public Linebreak()
	    throws VEException
        {
            super("linebreak");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append("[\r]?[\n]");
        }
    }

    static public class BR extends Verb
    {
        public BR()
	    throws VEException
        {
            super("br"); // same as linebreak
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append("[\r]?[\n]");
        }
    }

    static public class Tab extends Verb
    {
        public Tab()
	    throws VEException
        {
            super("tab");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append("[\t]");
        }
    }

    static public class Word extends Verb
    {
        public Word()
	    throws VEException
        {
            super("word");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append("\\w+");
        }
    }

    static public class Anyof extends Verb
    {
        public Anyof()
	    throws VEException
        {
            super("anyof", ArgType.STRING);
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(String.format("[%s]", args.getString(0)));
        }
    }

    static public class Any extends Verb
    {
        public Any()
	    throws VEException
        {
            super("any", ArgType.STRING); //same as anyof
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(String.format("[%s]", args.getString(0)));
        }
    }

    static public class Or extends Verb
    {
        public Or()
	    throws VEException
        {
            super("or");
        }


        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append("|");
        }
    }

    static public class Begincapture extends Verb
    {
        public Begincapture()
	    throws VEException
        {
            super("begincapture");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(LPAREN);
        }
    }

    static public class Endcapture extends Verb
    {
        public Endcapture()
	    throws VEException
        {
            super("endcapture");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(RPAREN);
        }
    }

    static public class Begin extends Verb
    {
        public Begin()
	    throws VEException
        {
            super("begin");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(LPAREN);
        }
    }

    static public class End extends Verb
    {
        public End()
	    throws VEException
        {
            super("end");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append(RPAREN);
        }
    }

    //////////////////////////////////////////////////
    // Extended verb set

    static public class Either extends Verb
    {
        public Either()
	    throws VEException
        {
            super("either");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append("");
        }
    }

    static public class Stop extends Verb
    {
        public Stop()
	    throws VEException
        {
            super("stop");
        }

        public void evaluate(ArgList args, Object state) throws VEException
        {
            StringBuilder buf = ((Test2State)state).getBuf();
            buf.append("");
        }
    }

}
