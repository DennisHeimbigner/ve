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

public class Test3 extends TestCommon
{
    Configuration cfg = null;

    public Test3(String[] argv)
        throws Exception
    {
        super(argv);
        int exitcode = 0;
        this.cfg = getOptions(argv);
        this.cfg.verbs = Test3Verbs.getVerbs();
    }

    public void
    test()
        throws Exception
    {
        VE ve = new VE(cfg);
        Test3Verbs.Test3State state = new Test3Verbs.Test3State(ve);
        ve.evaluate(state);
        System.out.printf("Result= %s\n", state.toString());
    }

    static public void main(String[] argv)
    {
        int exitcode = 0;
        try {
            new Test3(argv).test();
        } catch (Exception e) {
            report(e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

}

