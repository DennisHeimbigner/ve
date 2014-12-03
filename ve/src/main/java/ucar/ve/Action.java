/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve;

import java.util.List;

import static ucar.ve.Types.*;

public class Action
{
    public Verb verb;
    public ArgList args = null;
    Position pos = null;

    public Action(Verb verb)
    {
        this.verb = verb;
    }

    public void setArgs(ArgList args)
    {
        this.args = args;
    }

    public int arity()
    {
        return verb.getSignature().size();
    }

    public List<ArgType> getSignature()
    {
        return verb.getSignature();
    }

    public void setPosition(Position pos)
    {
        this.pos = new Position(pos);
    }

    public Position getPosition()
    {
        return this.pos;
    }

    public void
    execute(Object state)
        throws VEException
    {
        verb.evaluate(args, state);
    }

    public String
    toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append(verb.getName());
        buf.append("(");
        if(args != null)
            for(int i = 0;i < args.size();i++) {
                if(i > 0) buf.append(",");
                buf.append(args.get(i).toString());
            }
        buf.append(")");
        return buf.toString();
    }

}

