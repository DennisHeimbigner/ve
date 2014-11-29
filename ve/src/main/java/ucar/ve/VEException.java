/* Copyright 2012, UCAR/Unidata.
   See the LICENSE file for more information.
*/

//////////////////////////////////////////////////

package ucar.ve;

public class VEException extends java.io.IOException
{
    int lineno = -1;
    int charno = -1;

    public VEException()
    {
	super();
    }

    public VEException(String msg)
    {
        super(msg);
    }

    public VEException(Throwable e)
    {
        super(e);
    }

    public VEException(String msg, Throwable e)
    {
        super(msg, e);
    }

    public VEException setPosition(int lineno, int charno)
    {
        this.lineno = lineno;
        this.charno = charno;
        return this;
    }

    public VEException setPosition(Types.Position p)
    {
        setPosition(p.lineno,p.charno);
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
