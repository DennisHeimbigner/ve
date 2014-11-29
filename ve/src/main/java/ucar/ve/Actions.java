/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve;

import java.util.HashMap;
import java.util.Map;

import static ucar.ve.Types.*;

/**
 * Define the set of known
 * actions and their signatures.
 * Do not confuse with Action (singular)
 */

abstract public class Actions
{

    /**
     * The default set defined at
     * https://github.com/VerbalExpressions/implementation
     */

    static public Map<String, Verb> verbs = new HashMap<>();;

    //////////////////////////////////////////////////

    private final ArgType[] signature;
    private final boolean backtrack;
    private final String re;

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


