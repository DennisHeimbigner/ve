/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import static ucar.ve.compiler.VELexer.*;

/**
 * Equivalent of StringReader that allows for better
 * access to position info
 */

public class TextStream
{
    // Don't bother with getters
    String text = null; // source of text to lex
    int mark = 0;
    int next = 0; // next unread character
    int textlen = 0;

    public TextStream()
    {
    }

    public String around(int where)
    {
        String prefix = text.substring(where - 10, where);
        String suffix = text.substring(where, where + 10);
        return prefix + "|" + suffix;
    }

    public void setText(String text)
    {
        this.textlen = text.length();
        this.text = text + '\0'; // Null terminate
        next = 0;
        mark = 0;
    }

    public String getText()
    {
        return text.substring(0, this.textlen);
    }

    public String toString()
    {
        return getText();
    }

    int peek()
    {
        return text.charAt(next);
    }

    void
    backup()
    {
        if(next <= 0) next = 0;
        else
            next--;
    }

    int
    read()
    {
        char c = text.charAt(next);
        if(c != EOS) next++;
        return c;
    }

    void
    mark()
    {
        this.mark = this.next;
    }

    Position
    toPosition()
    {
        return toPosition(next);
    }

    Position
    toPosition(int mark)
    {
        int lineno = 1;
        int linepos = 0;
        for(int i = 0;i < mark;i++) {
            if(text.charAt(i) == '\n') {
                lineno++;
                linepos = i;
            }
        }
        int charno = (mark - linepos);
        return new Position(lineno, charno);
    }
}

