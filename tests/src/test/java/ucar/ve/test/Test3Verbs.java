/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.test;

import ucar.ve.*;

import java.util.*;

import static ucar.ve.Types.*;

abstract public class Test3Verbs
{
    static final String LPAREN = "(";
    static final String RPAREN = ")";

    static enum VerbTag
    {
        Startofline,
        Endofline,
        Find,
        Then,
        Maybe,
        Anything,
        Anythingbut,
        Anythingbutnot,
        Something,
        Somethingbut,
        Linebreak,
        Br,
        Tab,
        Word,
        Anyof,
        Any,
        Oneof,
        Begincapture,
        Endcapture,
        Begin,
        End,
        Stop;

        static public VerbTag
        tagFor(String s)
            throws VEException
        {
            for(VerbTag vt : values())
                if(s.equalsIgnoreCase(vt.toString()))
                    return vt;
            throw new VEException("VerbTag: so such verb: " + s);
        }

    }

    static class VerbCommon extends Verb
    {
        protected VerbTag tag;

        public VerbCommon(String name, ArgType... argtypes)
            throws VEException
        {
            super(name, argtypes);
            tag = VerbTag.tagFor(name);
        }

        public void evaluate(ArgList args, Object state)
            throws VEException
        {
            Test3State t1state = (Test3State) state;
            t1state.evaluate(this.tag, args);
        }
    }

    static public class Test3State
    {
        StringBuilder buf = new StringBuilder();
        VE ve = null;

        public Test3State(VE ve)
        {
            this.ve = ve;
        }

        public String toString()
        {
            return buf.toString();
        }

        public void evaluate(VerbTag tag, ArgList args)
            throws VEException
        {
            switch (tag) {
            case Startofline:
                buf.append("^");
                break;
            case Endofline:
                buf.append("$");
                break;
            case Find:
                buf.append(".*" + args.getString(0));
                break;
            case Then:
                buf.append(args.getString(0));
                break;
            case Maybe:
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
                break;
            case Anything:
                buf.append(".*");
                break;
            case Anythingbut:
                buf.append(String.format("[^%s]*", args.getString(0)));
                break;
            case Anythingbutnot:
                buf.append(String.format("[^%s]*", args.getString(0)));
                break;
            case Something:
                buf.append(".+");
                break;
            case Somethingbut:
                buf.append(String.format("[^%s]+", args.getString(0)));
                break;
            case Linebreak:
                buf.append("[\r]?[\n]");
                break;
            case Br:
                buf.append("[\r]?[\n]");
                break;
            case Tab:
                buf.append("[\t]");
                break;
            case Word:
                buf.append("\\w+");
                break;
            case Anyof:
                buf.append(String.format("[%s]", args.getString(0)));
                break;
            case Any:
                buf.append(String.format("[%s]", args.getString(0)));
                break;
            case Begincapture:
                buf.append(LPAREN);
                break;
            case Endcapture:
                buf.append(RPAREN);
                break;
            case Begin:
                buf.append(LPAREN);
                break;
            case End:
                buf.append(RPAREN);
                break;
            case Oneof:
                // generate code for each branch separated by '|'
                buf.append(LPAREN);
                args.getBlock(0).evaluate(this,ve);
                buf.append(RPAREN);
                buf.append('|');
                buf.append(LPAREN);
                args.getBlock(1).evaluate(this,ve);
                buf.append(RPAREN);
                break;
            case Stop:
                break;
            }
        }
    }

    //////////////////////////////////////////////////

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
        verbs.add(new VerbDef("begincapture", Begincapture.class));
        verbs.add(new VerbDef("endcapture", Endcapture.class));
        verbs.add(new VerbDef("begin", Begin.class));
        verbs.add(new VerbDef("end", End.class));
        verbs.add(new VerbDef("oneof", Oneof.class));
        verbs.add(new VerbDef("stop", Stop.class));
    }

    //////////////////////////////////////////////////
    // Initial set of Verbs; 
    // The verb names are intended to be case insensitive.
    // These are all static, but if the Verbs class state
    // is needed then they can be defined as non-static.

    static public class Startofline extends VerbCommon
    {
        public Startofline()
            throws VEException
        {
            super("startofline");
        }
    }

    static public class Endofline extends VerbCommon
    {
        public Endofline()
            throws VEException
        {
            super("endofline");
        }
    }

    static public class Find extends VerbCommon
    {
        public Find()
            throws VEException
        {
            super("find", ArgType.STRING);
        }
    }

    static public class Then extends VerbCommon
    {
        public Then()
            throws VEException
        {
            super("then", ArgType.STRING);
        }
    }

    static public class Maybe extends VerbCommon
    {
        public Maybe()
            throws VEException
        {
            super("maybe", ArgType.STRING);
        }
    }

    static public class Anything extends VerbCommon
    {
        public Anything()
            throws VEException
        {
            super("anything");
        }
    }

    static public class Anythingbut extends VerbCommon
    {
        public Anythingbut()
            throws VEException
        {
            super("anythingbut", ArgType.STRING);
        }
    }

    static public class Anythingbutnot extends VerbCommon
    {
        public Anythingbutnot()
            throws VEException
        {
            super("anythingbutnot", ArgType.STRING);
        }
    }

    static public class Something extends VerbCommon
    {
        public Something()
            throws VEException
        {
            super("something");
        }
    }

    static public class Somethingbut extends VerbCommon
    {
        public Somethingbut()
            throws VEException
        {
            super("somethingbut", ArgType.STRING);
        }
    }

    static public class Linebreak extends VerbCommon
    {
        public Linebreak()
            throws VEException
        {
            super("linebreak");
        }
    }

    static public class BR extends VerbCommon
    {
        public BR()
            throws VEException
        {
            super("br"); // same as linebreak
        }
    }

    static public class Tab extends VerbCommon
    {
        public Tab()
            throws VEException
        {
            super("tab");
        }
    }

    static public class Word extends VerbCommon
    {
        public Word()
            throws VEException
        {
            super("word");
        }
    }

    static public class Anyof extends VerbCommon
    {
        public Anyof()
            throws VEException
        {
            super("anyof", ArgType.STRING);
        }
    }

    static public class Any extends VerbCommon
    {
        public Any()
            throws VEException
        {
            super("any", ArgType.STRING); //same as anyof
        }
    }

    static public class Begincapture extends VerbCommon
    {
        public Begincapture()
            throws VEException
        {
            super("begincapture");
        }
    }

    static public class Endcapture extends VerbCommon
    {
        public Endcapture()
            throws VEException
        {
            super("endcapture");
        }
    }

    static public class Begin extends VerbCommon
    {
        public Begin()
            throws VEException
        {
            super("begin");
        }
    }

    static public class End extends VerbCommon
    {
        public End()
            throws VEException
        {
            super("end");
        }
    }

    //////////////////////////////////////////////////
    // Extended verb set

    static public class Oneof extends VerbCommon
    {
        public Oneof()
            throws VEException
        {
            super("oneof", ArgType.BLOCK, ArgType.BLOCK);
        }
    }

    static public class Stop extends VerbCommon
    {
        public Stop()
            throws VEException
        {
            super("stop");
        }
    }

}
