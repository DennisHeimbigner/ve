/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import java.util.ArrayList;
import static ucar.ve.compiler.VELexer.*;
import static ucar.ve.compiler.Types.*;

public class Types
{

/**
 * Track information about the compilation
 * such as position info (in the input file)
 * about verbs, actions, etc.
 */
static abstract public class CompileInfo
{
    Position position = null; // the position in the input file
    public CompileInfo() {}
    public void setPosition(Position pos) {this.position = pos;}
    public Position getPosition() {return this.position;}
}

static enum ArgType {
    WORD,STRING,NUMBER;
    static public ArgType DEFAULT = STRING;
}

static public class Arg extends CompileInfo
{
    public ArgType type;
    public String value;
    public Arg(ArgType type, String value) {this.type=type; this.value=value;}

    static public boolean compatible(ArgType arg, ArgType sig)
    {
	if(arg == sig)
	    return true;
	if(arg == ArgType.STRING)
	    return false;
	return false;	
    }
}

static public class ActionList extends ArrayList<Action>{}
static public class ArgList extends ArrayList<Arg>
{}


}
