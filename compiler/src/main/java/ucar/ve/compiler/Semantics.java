/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import java.util.ArrayList;
import java.util.List;

import static ucar.ve.compiler.Types.*;
import static ucar.ve.compiler.Verbs.*;

/**
 * Process a program and check the semantics of
 * the actions.
 */

public class Semantics
{

    //////////////////////////////////////////////////
    // Constructor(s)

    public Semantics()
    {
    }

    //////////////////////////////////////////////////
    // API

    public void
    process(ActionList program)
        throws CompileException
    {
        verbalize(program);
        processImports(program);
        processActions(program);
    }

    /**
     * 1. Replace the verb in the action with the real verb object.
     * 2. Verify that that verb exists.
     */
    protected void
    verbalize(ActionList program)
        throws CompileException
    {
        for(int i = 0;i < program.size();i++) {
            Action action = program.get(i);
            String realname = action.verb.getVerb();
            // Compute the canoninical class name
            realname = realname.substring(0, 1).toUpperCase()
                + realname.substring(1).toLowerCase();
            action.verb = Verbs.verbs.get(realname);
            if(action.verb == null)
                throw new CompileException("Unknown verb: " + action.verb)
                    .setPosition(action.getPosition());
        }
    }

    /**
     * 1. Verify that all imports are at the beginning; if not,
     * complain.
     * 2. Remove the imports from the program
     * 1. Extract and load the imports
     */
    protected void
    processImports(ActionList program)
        throws CompileException
    {
        boolean leading = true;
        List<Action> imports = new ArrayList<>();
        for(int i = 0;i < program.size();i++) {
            Action action = program.get(i);
            if(action.verb instanceof Import) {
                imports.add(action);
                if(!leading)
                    System.err.println("Non-leading import action encountered; continuing");
            } else
                leading = false;
        }
        // Remove import actions
        for(int i = program.size() - 1;i >= 0;i--) { // walk backword because deleting
            Action action = program.get(i);
            if(action.verb instanceof Import)
                program.remove(i);
        }
        // load the imports
        for(int i = 0;i < imports.size();i++) {
            Action imp = imports.get(i);
            ArgList args = imp.args;
            for(i = 0;i < args.size();i++) {
                Arg arg = args.get(i);
                Verbs.importVerbs(arg.value);
            }
        }
    }

    /**
     * 1. Verify signature vs arity
     */
    protected void
    processActions(ActionList program)
        throws CompileException
    {
        boolean leading = true;
        List<Import> imports = new ArrayList<>();
        for(int i = 0;i < program.size();i++) {
            Action action = program.get(i);
            if(action.args.size() != action.verb.signature.size())
                throw new CompileException("Incorrect number of arguments: "
                    + action).setPosition(action.getPosition());
            for(i = 0;i < action.args.size();i++) {
                Arg arg = action.args.get(i);
                ArgType argtype = action.verb.signature.get(i);
                if(Arg.compatible(arg.type, argtype))
                    throw new CompileException("Argument type mismatch at "
                        + i).setPosition(action.getPosition());
            }
        }
    }


}
