/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import java.util.HashMap;
import java.util.Map;

import static ucar.ve.compiler.Types.*;
import static ucar.ve.compiler.Verbs.*;

/**
 * Define the set of known
 * actions and their signatures.
 */

abstract public class Actions
{

    /**
     * The default set defined at
     * https://github.com/VerbalExpressions/implementation
     */

    /*static public Map<String, Verb> verbs;
    static {
        verbs = new HashMap<String, Verb>();

        verbs.put("startofline", new Startofline());
        verbs.put("endofline", new Endofline());
        verbs.put("find", new Find()); //find all occurrences => backtrack
        verbs.put("then", new Then()); //same as find
        verbs.put("maybe", new Maybe());
        verbs.put("anything", new Anything());
        verbs.put("anythingbut", new Anythingbut());
        verbs.put("something", new Something());
        verbs.put("somethingbut", new Somethingbut());
        verbs.put("linebreak", new Linebreak());
        verbs.put("br", new BR()); // same as linebreak
        verbs.put("tab", new Tab());
        verbs.put("word", new Word());
        verbs.put("anyof", new Anyof());
        verbs.put("any", new Any()); //same as anyof
        verbs.put("or", new Or()); // alternative
        verbs.put("begincapture", new Begincapture());
        verbs.put("endcapture", new Endcapture());
        verbs.put("stop", new Stop());

    // Currently unimplemented as such
    //verbs.put("add",new Add());
    //verbs.put("replace",new Replace());
    //verbs.put("range",new Range());
    //verbs.put("withanycase",new Withanycase());
    //verbs.put("stopatfirst",new Stopatfirst());
    //verbs.put("searchoneline",new Searchoneline());
    //verbs.put("multiple",new Multiple());

    //Extended set of actions
    //verbs.put("either", new Either()); // backtract point for 'or()'
    //verbs.put("stop", new Stop()); // terminate
    }
    */

    //////////////////////////////////////////////////

    protected final ArgType[] signature;
    protected final boolean backtrack;
    protected final String re;

    Actions(ArgType signature, boolean backtrack, String re)
    {
        if(signature == null)
            this.signature = new ArgType[0];
        else {
            this.signature = new ArgType[1];
            this.signature[0] = signature;
        }
        this.backtrack = backtrack;
        this.re = re;
    }

    Actions(ArgType[] signature, boolean backtrack, String re)
    {
        this.signature = signature;
        this.backtrack = backtrack;
        this.re = re;
    }

    Actions(String re)
    {
        this((ArgType[]) null, false, re);
    }

    Actions(ArgType signature, String re)
    {
        this(signature, false, re);
    }

    Actions()
    {
        this((ArgType[]) null, false, null);
    }


    public ArgType[] getSgnature()
    {
        return this.signature;
    }

    public boolean isBacktracking()
    {
        return this.backtrack;
    }

    public String getRE()
    {
        return this.re;
    }

}


