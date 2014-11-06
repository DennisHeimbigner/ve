/* Copyright 2012, UCAR/Unidata.
   See the LICENSE file for more information.
*/

//////////////////////////////////////////////////

package ucar.ve.compiler;

public class CompileException extends java.io.IOException
{
    int lineno = -1;
    int charno = -1;

    public CompileException()
    {
	super();
    }

    public CompileException(String msg)
    {
        super(msg);
    }

    public CompileException(Throwable e)
    {
        super(e);
    }

    public CompileException(String msg, Throwable e)
    {
        super(msg, e);
    }

    public CompileException setPosition(int lineno, int charno)
    {
        this.lineno = lineno;
        this.charno = charno;
        return this;
    }

    public CompileException setPosition(VELexer.Position pos)
    {
        this.lineno = pos.lineno;
        this.charno = pos.charno;
        return this;
    }

    public int getErrorLine()
    {
        return this.lineno;
    }

    public int getErrorChar()
    {
        return this.charno;
    }
}
