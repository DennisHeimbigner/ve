/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import java.io.PrintWriter;

import static ucar.ve.compiler.Types.*;

public class JavaREGenerator extends Generator
{
    //////////////////////////////////////////////////
    // Constants

    static protected final String LPAREN = "(";
    static protected final String RPAREN = ")";

    //////////////////////////////////////////////////
    // Type declarations

    static public class State
    {
        public boolean done = false;
    }

    //////////////////////////////////////////////////
    // Constructor(s)
    public JavaREGenerator() {}

    //////////////////////////////////////////////////
    // Generator API

    @Override
    public void
    generate(ActionList program, PrintWriter pw)
        throws CompileException
    {
        State state = new State();
        for(int i = 0;i < program.size();i++) {
            Action action = program.get(i);
            expression(state, action, pw);
        }
    }

    protected void
    expression(State state, Action action, PrintWriter pw)
        throws CompileException
    {
        String verbname = action.verb.verb;
        if(verbname.equals("startofline")) {
            pw.print("^");
        } else if(verbname.equals("endofline")) {
            pw.print("$");
        } else if(verbname.equals("find")) {
            pw.print("%1");
        } else if(verbname.equals("then")) {
            pw.print("%1");
        } else if(verbname.equals("maybe")) {
            pw.print("%1?");
        } else if(verbname.equals("anything")) {
            pw.print(".*");
        } else if(verbname.equals("anythingbut")
                  || verbname.equals("anythingbutnot")) {
            pw.print("[^%1]*");
        } else if(verbname.equals("something")) {
            pw.print(".+");
        } else if(verbname.equals("somethingbut")) {
            pw.print("[^%1]+");
        } else if(verbname.equals("linebreak")
                  || verbname.equals("br")) {
            pw.print("[\r]?[\n]");
        } else if(verbname.equals("tab")) {
            pw.print("[\t]");
        } else if(verbname.equals("word")) {
            pw.print("\\w+");
        } else if(verbname.equals("anyof")) {
            pw.print("[%1]");
        } else if(verbname.equals("any")) {
            pw.print("[%1]"); //same as anyof;
        } else if(verbname.equals("or")) {
            pw.print("|"); // alternative;
        } else if(verbname.equals("begincapture")) {
            pw.print("\\" + LPAREN);
        } else if(verbname.equals("endcapture")) {
            pw.print("\\" + RPAREN);
        } else if(verbname.equals("begin")) {
            pw.print("\\(");
        } else if(verbname.equals("end")) {
            pw.print("\\)");
        } else if(verbname.equals("stop")) {
            pw.print("");
        } else if(verbname.equals("stop")) {
            state.done = true;
        } else {
            throw new CompileException("Unknown verb: " + verbname)
                .setPosition(action.pos);
        }
    }
}

