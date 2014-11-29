/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve;

/*
The super class for all generators.
*/

import java.io.PrintWriter;

abstract public class Generator
{
    abstract public void execute(Types.ActionList program, PrintWriter pw) throws VEException;
}
