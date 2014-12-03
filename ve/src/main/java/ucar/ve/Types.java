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

        public void evaluate(Object state, VE ve)
            throws VEException
        {
            for(int i = 0;i < super.size();i++) {
                Action action = super.get(i);
                action.execute(state);
            }
        }

        public String
        toString()
        {
            StringBuilder buf = new StringBuilder();
            buf.append('{');
            for(int i=0;i<super.size();i++) {
                if(i > 0) buf.append(';');
                buf.append(super.get(i).toString());
            }
            buf.append('}');
            return buf.toString();
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
            return super.get(index).asString();
        }

        public String getWord(int index)
            throws VEException
        {
            return super.get(index).asWord();

        }

        public Number getNumber(int index)
            throws VEException
        {
            return super.get(index).asNumber();
        }

        public ActionList getBlock(int index)
            throws VEException
        {
            return super.get(index).asBlock();
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
        WORD, STRING, NUMBER, BLOCK;
        static public ArgType DEFAULT = STRING;
    }

    static public class Arg
    {
        public ArgType type;
        public Object value;

        public Arg(ArgType type, Object value)
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
            b.append(value.toString());
            b.append(")");
            return b.toString();
        }

        public String asString()
        {
            return value.toString();
        }

        public Number asNumber()
        {
            if(value instanceof Number)
                return (Number) value;
            try {
                return Long.valueOf(this.value.toString()); // try this first
            } catch (NumberFormatException nfe1) {
                try {
                    return Double.valueOf(this.value.toString()); // try this second
                } catch (NumberFormatException nfe2) {
                    throw new IllegalStateException("Arg cannot be converted to Number");
                }
            }
        }

        public String asWord()
        {
            String sv = value.toString();
            boolean ok = VEParser.WORDCHAR1.indexOf(sv.charAt(0)) >= 0;
            if(ok) {
                for(int i = 1;i < sv.length();i++) {
                    if(VEParser.WORDCHARN.indexOf(sv.charAt(i)) < 0) {
                        ok = false;
                        break;
                    }
                }
            }
            if(!ok)
                throw new IllegalStateException("Cannot convert arg to word");
            return asString();
        }

        public ActionList asBlock()
        {
            if(value instanceof ActionList)
                return (ActionList) value;
            throw new IllegalStateException("Arg cannot be converted to Block");
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
            this.input = (cfg.input != null ? cfg.input.trim() : null);
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
