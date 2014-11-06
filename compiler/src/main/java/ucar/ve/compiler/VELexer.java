/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

public class VELexer implements VEParserBody.Lexer
{

    //////////////////////////////////////////////////
    // Constants

    /**
     * Escape Character
     */
    static final char ESCAPE = '\\';

    /**
     * Legal first char of a word
     */
    static final String WORDCHAR1 =
        "abcdefghijklmnopqrstuvwxyz"
            + "abcdefghijklmnopqrstuvwxyz".toUpperCase()
            + "0123456789"
            + "_$+-";

    /**
     * Legal non-first char of a word
     */
    static final String WORDCHARN = WORDCHAR1 + ".";


    /**
     * End-of-string marker
     */
    static final char EOS = '\0';

    static final int CONTEXTLEN = 20; // yyerror shows last CONTEXTLEN characters of input

    /**
     * Hex digits
     */
    static final String hexdigits = "0123456789abcdefABCDEF";

    /**
     * Key words: none currently
     */
    static final String[] keywords = new String[]{
    };

    /**
     * Key word tokens: none currently
     */
    static final int[] keytokens = new int[]{
    };

    //////////////////////////////////////////////////
    // Type Declarations

    static public class Position
    {
        public int lineno = 0;
        public int charno = 0;

        public Position(int lineno, int charno)
        {
            this.lineno = lineno;
            this.charno = charno;
        }

        public void clear()
        {
            this.lineno = 0;
            this.charno = 0;
        }

        @Override
        public boolean
        equals(Object o)
        {
            if(!(o instanceof Position))
                return false;
            Position po = (Position) o;
            return po.lineno == this.lineno && po.charno == this.charno;
        }

        public String toString()
        {
            return String.format("%d::%d",this.lineno,this.charno);
        }
    }

    //////////////////////////////////////////////////
    // Instance variables

    VEParser parsestate = null; // our parent parser

    Object lval = null;

    TextStream text = null;
    int charno = 0;
    int lineno = 0;
    StringBuilder yytext = null;

    //////////////////////////////////////////////////
    // Constructor(s)

    public VELexer()
    {
        this.text = new TextStream();
        yytext = new StringBuilder();
        lval = null;
    }

    public VELexer(VEParser state)
    {
        this();
        setParser(state);
    }

    //////////////////////////////////////////////////
    // Accessors

    void setParser(VEParser state)
    {
        this.parsestate = state;
    }

    void setText(String text)
    {
        this.text.setText(text);
    }

    public String getInput()
    {
        return text.getText();
    }

    //////////////////////////////////////////////////
    // The Bison Lexer interface methods: yylex, getLval, yyerror.

    /**
     * Entry point for the scanner.
     * Returns the token identifier corresponding
     * to the next token and prepares to return the semantic value
     * of the token.
     *
     * @return the token identifier corresponding to the next token.
     */

    public int
    yylex()
        throws CompileException
    {
        int token;
        int c = 0;
        token = 0;
        yytext.setLength(0);
        text.mark();

        token = -1;
        while(token < 0 && (c = text.read()) != EOS) {
            if(c == '\n')
                token = EOL;
            else if(c <= ' ' || c == '\177') {
                /* whitespace: ignore */
            } else if(c == '"' || c == '\'') {
                int delim = c;
                boolean more = true;
                /* We have a string token */
                while(more && (c = text.read()) > 0) {
                    switch (c) {
                    case EOS:
                        Position pos = text.toPosition();
                        throw new CompileException("Unterminated character or string constant").setPosition(pos);
                    case '"':
                        more = (delim != c);
                        break;
                    case '\'':
                        more = (delim != c);
                        break;
                    case ESCAPE:
                        switch (c) {
                        case 'r':
                            c = '\r';
                            break;
                        case 'n':
                            c = '\n';
                            break;
                        case 'f':
                            c = '\f';
                            break;
                        case 't':
                            c = '\t';
                            break;
                        default:
                            break;
                        }
                        break;
                    default:
                        break;
                    }
                    if(more) yytext.append((char) c);
                }
                token = STRING;
            } else if(WORDCHAR1.indexOf(c) >= 0) {
                yytext.append((char) c);
                while((c = text.read()) != EOS) {
                    if(WORDCHARN.indexOf(c) < 0) break; // not a word character
                    yytext.append((char) c);
                }
                // pushback the delimiter
                if(c != EOS) text.backup();
                try {// See if this looks like an integer
                    long num = Long.parseLong(yytext.toString());
                    token = NUMBER;
                } catch (NumberFormatException nfe) {
                    token = WORD;
                }
            } else {// Treat as a single char delimiter
                token = c;
            }
        }
        if(c == EOS && token < 0) {
            token = 0;
            lval = null;
        } else {
            lval = (yytext.length() == 0 ? (String) null : yytext.toString());
        }
        if(parsestate.getDebugLevel() > 0)
            dumptoken(token, (String) lval);
        return token; // Return the type of the token
    }

    void
    dumptoken(int token, String lval)
        throws CompileException
    {
        String stoken;
        if(token < '\177')
            stoken = Character.toString((char) token);
        else
            switch (token) {
            case VEParser.Lexer.STRING:
                stoken = '"' + lval + '"';
                break;
            case VEParser.Lexer.NUMBER:
                stoken = lval;
                break;
            case VEParser.Lexer.WORD:
                stoken = lval;
                break;
            default:
                stoken = "X" + Integer.toString(token);
            }
        System.err.println("TOKEN = |" + stoken + "|");

    }

    static int
    tohex(int c)
        throws CompileException
    {
        if(c >= 'a' && c <= 'f') return (c - 'a') + 0xa;
        if(c >= 'A' && c <= 'F') return (c - 'A') + 0xa;
        if(c >= '0' && c <= '9') return (c - '0');
        return -1;
    }

    /**
     * Method to retrieve the semantic value of the last scanned token.
     * Part of Lexer interface.
     *
     * @return the semantic value of the last scanned token.
     */
    public Object getLVal()
    {
        return this.lval;
    }

    /**
     * Entry point for error reporting.  Emits an error
     * in a user-defined way.
     * Part of Lexer interface.
     *
     * @param s The string for the error message.
     */
    public void yyerror(VEParserBody.Location loc, String s)
    {
        System.err.println("CEParser.yyerror: " + s + "; parse failed at char: " + charno + "; near: ");
        String context = getInput();
        int show = (context.length() < CONTEXTLEN ? context.length() : CONTEXTLEN);
        System.err.println(context.substring(context.length() - show) + "^");
        new Exception().printStackTrace(System.err);
    }

    public Position getStartPos()
    {
        return text.toPosition(text.mark);
    }

    public Position getEndPos()
    {
        return text.toPosition(text.next);
    }


}
