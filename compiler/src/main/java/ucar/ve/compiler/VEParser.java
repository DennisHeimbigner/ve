/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import static ucar.ve.compiler.Types.*;
import static ucar.ve.compiler.VEParserBody.Lexer.*;

public class VEParser extends VEParserBody
{

    //////////////////////////////////////////////////
    // Instance variables

    //////////////////////////////////////////////////
    // Constructors

    public VEParser()
        throws CompileException
    {
        super(null);
        VELexer lexer = new VELexer(this);
        setLexer(lexer);
    }

    //////////////////////////////////////////////////
    // Get/Set

    //////////////////////////////////////////////////
    // Parser API

    @Override
    public boolean
    parse(String document)
        throws CompileException
    {
        ((VELexer) getLexer()).setText(document);
        return super.parse();
    }

    //////////////////////////////////////////////////
    // Abstract Parser action implementations

    @Override
    void
    program(ActionList actionlist)
    {
        this.program = actionlist;
    }

    @Override
    Action
    action(Verb verb, ArgList args)
    {
        Action a = new Action(verb, args);
        a.setPosition(verb.getPosition());
        return a;
    }

    @Override
    Verb
    verb(String verb)
    {
        Verb v = new Verb(verb);
        v.setPosition(getLexer().getStartPos());
        return v;
    }

    @Override
    Arg
    arg(int kind, String value)
    {
        ArgType type = null;
        switch (kind) {
        case WORD:
            type = ArgType.WORD;
            break;
        case STRING:
            type = ArgType.WORD;
            break;
        case NUMBER:
            type = ArgType.WORD;
            break;
        }
        Arg arg = new Arg(type, value);
        arg.setPosition(getLexer().getStartPos());
        return arg;
    }

    @Override
    ActionList
    actionlist(ActionList list, Action action)
    {
        if(list == null)
            list = new ActionList();
        if(action != null)
            list.add(action);
        return list;
    }

    @Override
    ArgList
    arglist(ArgList list, Arg arg)
    {
        if(list == null)
            list = new ArgList();
        if(arg != null)
            list.add(arg);
        return list;
    }
}

    
