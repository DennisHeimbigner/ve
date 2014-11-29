/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static ucar.ve.Types.*;

/**
 * Define all the relatively simple
 * types here.
 */

public class Types
{

    /**
     * "Classify" common lists of objects
     */
    static public class ActionList extends ArrayList<Action>
    {
        public ActionList addAction(Action x)
        {
            if(x != null) super.add(x);
            return this;
        }
    }

    static public class ArgList extends ArrayList<Arg>
    {
        public ArgList addArg(Arg x)
        {
            if(x != null) super.add(x);
            return this;
        }

        public String getString(int index)
            throws VEException
        {
            return super.get(0).value;
        }

        public String getWord(int index)
            throws VEException
        {
            Arg arg = super.get(0);
            boolean ok = VEParser.WORDCHAR1.indexOf(arg.value.charAt(0)) >= 0;
            if(ok) {
                for(int i = 1;i < arg.value.length();i++) {
                    if(VEParser.WORDCHARN.indexOf(arg.value.charAt(i)) < 0) {
                        ok = false;
                        break;
                    }
                }
            }
            if(!ok)
                throw new VEException(String.format("Cannot convert %s to word", arg.value));
            return arg.value;
        }

        public long getNumber(int index)
            throws VEException
        {
            Arg arg = super.get(0);
            try {
                long l = Long.parseLong(arg.value);
                return l;
            } catch (NumberFormatException nfe) {
                throw new VEException(String.format("Cannot convert %s to number", arg.value));
            }
        }
    }

    static public class Signature extends ArrayList<ArgType>
    {
        public Signature addType(ArgType x)
        {
            if(x != null) super.add(x);
            return this;
        }
    }

    static public enum ArgType
    {
        WORD, STRING, NUMBER;
        static public ArgType DEFAULT = STRING;
    }

    static public class Arg
    {
        public ArgType type;
        public String value;

        public Arg(ArgType type, String value)
        {
            this.type = type;
            this.value = value;
        }

        static public boolean compatible(ArgType arg, ArgType sig)
        {
            if(arg == sig)
                return true;
            if(sig == ArgType.STRING)
                return true;
            if(arg == ArgType.STRING)
                return false;
            if(sig == ArgType.NUMBER && arg != ArgType.NUMBER)
                return false;
            return true;
        }

        public String toString()
        {
            StringBuilder b = new StringBuilder();
            b.append(type.name());
            b.append("(");
            b.append(value);
            b.append(")");
            return b.toString();
        }
    }

    static public class Position
    {
        public int lineno = 0;
        public int charno = 0;

        public Position()
        {
        }

        public Position(int l, int c)
        {
            this.lineno = l;
            this.charno = c;
        }

        public Position(Position pos)
        {
            setPosition(pos);
        }

        public void setPosition(Position pos)
        {
            this.lineno = pos.lineno;
            this.charno = pos.charno;
        }

        public void clear()
        {
            this.lineno = 0;
            this.charno = 0;
        }

        public String toString()
        {
            return String.format("%d::%d", this.lineno, this.charno);
        }
    }

    /**
     * This defines the information needed about
     * a verb in order for the interpreter to use it.
     * Note that this is separate from the Verb class.
     */

    static public class VerbDef
    {
        protected String name = null;
        protected Class<? extends Verb> verbclass = null;

        public VerbDef(String name, Class<? extends Verb> verbclass)
        {
            this.name = name;
            this.verbclass = verbclass;
        }

        public String getName()
        {
            return this.name;
        }

        public Class getVerbClass()
        {
            return this.verbclass;
        }
    }

    static public class Configuration
    {
        public String input = null;
        public PrintWriter output = null;
        public Class format = null;
        public List<VerbDef> verbs = null;
        // Debug state
        public boolean verbose = false;
        public boolean debug = false;
        public boolean parsedebug = false;
        public boolean trace = false;

        public Configuration()
        {
        }

        public Configuration(Configuration cfg)
        {
            this.input = cfg.input.trim();
            this.output = cfg.output;
            this.format = cfg.format;
            this.verbs = cfg.verbs;
            this.verbose = cfg.verbose;
            this.debug = cfg.debug;
            this.parsedebug = cfg.parsedebug;
            this.trace = cfg.trace;
        }
    }

}
