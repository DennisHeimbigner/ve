/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import java.util.ArrayList;
import java.util.List;

import static ucar.ve.compiler.Types.*;

/**
  * Note that this class is not abstract
  * so that it can be instantiated as a placeholder
  * during parsing. It will be replaced by a real
  * Verb subclass during semantic processing
  */

public class Verb extends CompileInfo
{
    //////////////////////////////////////////////////
    // Instance variables

    protected String verb = null;
    protected List<ArgType> signature = null;
    protected boolean backtrack = false;

    //////////////////////////////////////////////////
    // Constructor(s)

    public Verb(String verb)  // 0-ary
    {
        this(verb, (List<ArgType>) null);
    }

    public Verb(String verb, ArgType signature) // 1-ary
    {
        if(signature == null)
            signature = ArgType.DEFAULT;
        this.signature = new ArrayList<ArgType>();
        this.signature.add(signature);
        this.verb = verb.toLowerCase();
    }

    public Verb(String verb, List<ArgType> signature) // n-ary
    {
        this.signature = signature;
        this.backtrack = backtrack;
        this.verb = verb;
    }

    //////////////////////////////////////////////////

    public String toString()
    {
	return this.verb;
    }

    //////////////////////////////////////////////////
    // Accessors

    public String getVerb()
    {
        return this.verb;
    }

    public List<ArgType> getSignature()
    {
        return this.signature;
    }

    public boolean isBacktracking()
    {
        return this.backtrack;
    }

    public void setBackTracking(boolean tf)
    {
        this.backtrack = tf;
    }
}
