/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.test;

import org.apache.commons.cli.*;
import ucar.ve.*;

import java.util.*;
import java.io.*;

import static ucar.ve.Types.*;
import static ucar.ve.VE.*;

public class Test2 extends TestCommon
{
    Configuration cfg = null;

    public Test2(String[] argv)
        throws Exception
    {
        super(argv);
        int exitcode = 0;
        Configuration cfg = getOptions(argv);
        cfg.verbs = Test2Verbs.getVerbs();
    }

    public void
    test()
        throws Exception
    {
        Object state = new StringBuilder();
        evaluate(cfg, state);
        System.out.printf("Result=!%s!", ((StringBuilder) state).toString());
    }

    static public void main(String[] argv)
    {
	int exitcode = 0;
	try {
	    new Test2(argv).test();
        } catch (Exception e) {
	    report(e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

}

