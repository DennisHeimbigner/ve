/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve;

import java.util.Map;

import static ucar.ve.Types.*;
import static ucar.ve.VE.*;

/**
Common parent for all parsers
*/

abstract public class Parser
{
    //////////////////////////////////////////////////
    // Instance variables

    protected VE ve = null;
    protected Configuration cfg = null;
    protected ActionList program = null;
    protected int debuglevel = 0;

    //////////////////////////////////////////////////
    // Constructors

    public Parser()
        throws VEException
    {
	this(null);
    }

    public Parser(VE ve)
    {
        this.ve = ve;
        this.cfg = ve.getConfiguration();
    }

    //////////////////////////////////////////////////
    // Accessors

    public ActionList
    getProgram()
    {
        return this.program;
    }

    public void setDebugLevel(int level)
    {
        this.debuglevel = level;
    }

    public int getDebugLevel()
    {
        return this.debuglevel;
    }

/*
    public void setVerbs(Map<String, Verb> verbs)
    {
        this.verbs = verbs;
    }
*/

    //////////////////////////////////////////////////
    // Parser API

    abstract public void parse(String text) throws VEException;

}

    
