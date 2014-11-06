/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import static ucar.ve.compiler.Types.*;
import static ucar.ve.compiler.VEParserBody.Lexer.*;

abstract public class Parser
{

    //////////////////////////////////////////////////
    // Instance variables

    protected ActionList program = null;

    //////////////////////////////////////////////////
    // Constructors

    public Parser()
        throws CompileException
    {
    }

    //////////////////////////////////////////////////
    // Get/Set

    public ActionList
    getProgram()
    {
        return this.program;
    }

    //////////////////////////////////////////////////
    // Parser API

    abstract public boolean parse(String document) throws CompileException;
    abstract public void setDebugLevel(int level);

}

    
