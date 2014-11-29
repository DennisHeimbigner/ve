/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve;

import java.util.ArrayList;
import java.util.List;

import static ucar.ve.Types.*;

abstract public class Verb
{
    //////////////////////////////////////////////////
    // Instance variables

    protected String name = null;
    protected Signature signature = null;

    //////////////////////////////////////////////////
    // Constructor(s)

    public Verb(String name, ArgType... argtypes)
    {
        this.name = name;
        this.signature = new Signature();
        for(int i=0;i<argtypes.length;i++) {
            ArgType at = argtypes[i];
            this.signature.add(at);
        }
    }

    //////////////////////////////////////////////////
    // Subclass Defined Interface

    abstract public void evaluate(ArgList args, Object state) throws VEException;
    //////////////////////////////////////////////////
    // Accessors

    public String getName()
    {
        return this.name;
    }

    public Signature getSignature()
    {
        return this.signature;
    }

    //////////////////////////////////////////////////

    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append(this.name);
        buf.append("(");
        for(int i = 0;i < this.signature.size();i++) {
            ArgType at = this.signature.get(i);
            if(i > 0) buf.append(",");
            buf.append(at.name());
        }
        buf.append(")");
        return buf.toString();
    }

    public String
    trace(ArgList args, Object state)
    {
        StringBuilder buf = new StringBuilder();
        buf.append(this.name);
        buf.append("(");
        for(int i = 0;i < args.size();i++) {
            Arg arg = args.get(i);
            if(i > 0) buf.append(",");
            buf.append(arg.value);
        }
        buf.append(")");
        if(state != null) {
            buf.append("@|");
            buf.append(state.toString());
            buf.append("|");
        }
        return buf.toString();
    }
}
