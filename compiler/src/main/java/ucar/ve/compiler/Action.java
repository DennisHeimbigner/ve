/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import static ucar.ve.compiler.Types.*;

public class Action extends CompileInfo
{
    public Verb verb; // Will be overwritten by semantic processing
    public ArgList args;
    public VELexer.Position pos = null;
    public Action(Verb verb, ArgList args)
	{this.verb = verb; this.args=args;}
    public void setPosition(VELexer.Position pos) {this.pos = pos; }

    public String toString()
    {
	StringBuilder buf = new StringBuilder();
	buf.append(verb.toString());
	buf.append(")");
	for(int i=0;i<args.size();i++) {
	    Arg arg = args.get(i);
	    if(i > 0) buf.append(",");
	    if(arg.type == ArgType.STRING) {
		buf.append('"');
		buf.append(arg.value);
		buf.append('"');
	    } else
		buf.append(arg.value);
	}
	buf.append(")");
	return buf.toString();
    }


}
