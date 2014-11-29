/*
This software is released uder the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve;

import java.util.List;

import static ucar.ve.Types.*;

public class VEParser extends Parser
{

    //////////////////////////////////////////////////
    // Constants

    /**
     * Escape Character
     */
    static final protected char ESCAPE = '\\';

    /**
     * Begin comment Character
     */
    static final protected char COMMENTCHAR = '#';

    static final char NULCHAR = '\0';

    /**
     * Legal first char of a word
     */
    static final public String WORDCHAR1 =
        "abcdefghijklmnopqrstuvwxyz"
            + "abcdefghijklmnopqrstuvwxyz" .toUpperCase()
            + "0123456789"
            + "_$+-";

    /**
     * Legal non-first char of a word
     */
    static final public String WORDCHARN = WORDCHAR1 + ".";


    /**
     * End-of-string marker
     */
    static final protected char EOS = NULCHAR;

    // Use integers instead of enum
    static final protected int EOF = 0;
    static final protected int NONE = -1;
    static final protected int WORD = -2;
    static final protected int STRING = -3;
    static final protected int NUMBER = -4;

    static protected enum ParenState
    {
        NONE, LEFT, RIGHT;
    }

    /////////////////////////////////////////////////
    // TextStream

    /**
     * Equivalent of StringReader that allows for better
     * access to position info
     */

    static protected class TextStream
    {

        // Don't bother with getters
        String text = null; // source of text to lex
        int mark = 0;
        int next = 0; // next unread character
        int textlen = 0;

        public TextStream(String text)
        {
            this.textlen = text.length();
            this.text = text + '\0'; // Null terminate
            next = 0;
            mark = 0;
        }

        public String around(int where)
        {
            String prefix = text.substring(where - 10, where);
            String suffix = text.substring(where, where + 10);
            return prefix + "|" + suffix;
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
            if(next <= 0)
                next = 0;
            else if(next < textlen)
                next--;
        }

        int
        read()
        {
            char c = text.charAt(next);
            if(c != NULCHAR) next++;
            return c;
        }

        void
        mark()
        {
            this.mark = this.next;
        }

        int
        getMark()
        {
            return this.mark;
        }

        void
        setNext(int next)
        {
            this.next = next;
        }

        Position
        toPosition()
        {
            return toPosition(this.getMark());
        }

        Position
        toPosition(int mark)
        {
            int lineno = 0;
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


    //////////////////////////////////////////////////
    // Lexer

    static protected class Lexer
    {
        //////////////////////////////////////////////////
        // Instance variables
        TextStream text = null;
        Parser parser = null;
        int charno = 0;
        int lineno = 0;
        int tokenmark = -1; // allow pushback of a token

        //////////////////////////////////////////////////
        // Constructor(s)

        public Lexer(String text, Parser parser)
        {
            this.text = new TextStream(text);
            this.parser = parser;
        }

        /**
         * Entry point for the scanner.
         * Returns the token corresponding
         * to the next token and stores the value.
         *
         * @param yytext store the value here
         * @return the token identifier corresponding to the next token.
         */

        protected int
        yylex(StringBuilder yytext, Position yypos)
            throws VEException
        {
            int token = NONE;
            int c = 0;
            yytext.setLength(0);
            yypos.clear();

            if(tokenmark >= 0) {
                text.setNext(tokenmark);
                tokenmark = -1;
            }

            token = NONE;
            while(token == NONE) {
                text.mark();
                c = text.read();
                if(c == EOS) {
                    token = EOF;
                } else if(c == '\n')
                    token = c;
                else if(c == COMMENTCHAR) {
                    // move to end of line and return EOL or EOS
                    for(;;) {
                        c = text.read();
                        if(c == EOS) {
                            token = EOF;
                            break;
                        } else if(c == '\n') {
                            token = c;
                            break;
                        }
                    }
                } else if(c <= ' ' || c == '\177') {
                    /* whitespace: ignore */
                } else if(c == '"' || c == '\'') {
                    int delim = c;
                    boolean more = true;
                    /* We have a string token */
                    while(more && (c = text.read()) > 0) {
                        switch (c) {
                        case EOS:
                            yypos.setPosition(text.toPosition());
                            throw new VEException("Unterminated character or string constant").setPosition(yypos);
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
                    for(;;) {
                        c = text.read();
                        if(WORDCHARN.indexOf(c) < 0) break; // not a word character
                        yytext.append((char) c);
                    }
                    // pushback the delimiter
                    text.backup();
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
            yypos.setPosition(text.toPosition());
            if(parser.getDebugLevel() > 0)
                System.err.println("TOKEN = |" + dumptoken(token, yytext.toString()) + "|");
            return token; // Return the type of the token
        }

        /**
         * Allow a one token pushback
         */
        public void pushback()
        {
            this.tokenmark = text.getMark();
        }


        public Position getPosition()
        {
            return text.toPosition();
        }
    }

//////////////////////////////////////////////////
// Parser

//////////////////////////////////////////////////
// Parser state

    Lexer lexer = null;


//////////////////////////////////////////////////
// Constructor(s)

    public VEParser(VE ve)
        throws VEException
    {
        super(ve);
    }

//////////////////////////////////////////////////
// Abstract Parser API

    @Override
    public void setDebugLevel(int level)
    {
    }

    @Override
    public void
    parse(String text)
        throws VEException
    {
        this.lexer = new Lexer(text, this);
        this.program = new ActionList();
        StringBuilder yytext = new StringBuilder();
        Position yypos = new Position(0, 0);
        Action action = null;
        int token = NONE;
        String errmsg = null;

        // Non-recursive parser read action per loop
        actionloop:
        for(;;) {
            token = lexer.yylex(yytext, yypos);
            if(cfg.parsedebug)
                System.err.println("parser: reading token: " + dumptoken(token, yytext.toString()));
            if(token == EOF)
                break actionloop;
            switch (token) {
            case '.': // => EOL
            case '\n': // => EOL
                continue actionloop;

            case WORD: // Verb
                String name = yytext.toString();
                Verb verb = ve.getVerbs().get(name.toLowerCase());
                if(verb == null) {
                    errmsg = String.format("Unknown verb: %s", name);
                    break actionloop;
                }
                action = new Action(verb);
                action.setPosition(lexer.getPosition());
                collectargs(action, yytext, yypos);
                this.program.add(action);
                break;

            default:
                errmsg = String.format("Expected %s, found: %s",
                    "Verb", yytext.toString());
                break actionloop;
            }
        }
        if(errmsg != null) {
            if(action == null)
                throw new VEException(errmsg);
            else
                throw new VEException(errmsg).setPosition(action.getPosition());
        }
    }

    protected void
    collectargs(Action action, StringBuilder yytext, Position yypos)
        throws VEException
    {
        String errmsg = null;
        ArgList args = new ArgList();
        int token = NONE;
        ParenState parenstate = ParenState.NONE; /* track if we are inside parens */

        // read arg per loop upto end of action
        boolean more = true;
        for(int i = 0;more && errmsg == null;i++) {
            Arg arg = null;
            token = lexer.yylex(yytext, yypos);
            switch (token) {
            case EOF:
            case '.':
            case ';':
            case '\n':
                more = false;
                break;
            case '(':
                if(parenstate != ParenState.NONE)
                    errmsg = "Too many parentheses";
                else
                    parenstate = ParenState.LEFT;
                break;
            case ')':
                switch (parenstate) {
                case NONE:
                    errmsg = "Parentheses mismatch";
                    break;
                case RIGHT:
                    errmsg = "Too many parentheses";
                    break;
                case LEFT:
                    parenstate = ParenState.RIGHT;
                    break;
                }
                break;
            case WORD:
                if(parenstate == ParenState.RIGHT)
                    errmsg = "Arguments after right parenthesis";
                else
                    args.add(new Arg(ArgType.WORD, yytext.toString()));
                break;
            case STRING:
                if(parenstate == ParenState.RIGHT)
                    errmsg = "Arguments after right parenthesis";
                else
                    args.add(new Arg(ArgType.STRING, yytext.toString()));
                break;
            case NUMBER:
                if(parenstate == ParenState.RIGHT)
                    errmsg = "Arguments after right parenthesis";
                else
                    args.add(new Arg(ArgType.NUMBER, yytext.toString()));
                break;
            default:
                errmsg = String.format("Expected %s, found: %s",
                    "Argument", yytext.toString());
                break;
            }
        }
        if(errmsg != null)
            throw new VEException(errmsg).setPosition(yypos);
        // Validate against the signature
        List<ArgType> signature = action.getSignature();
        if(signature.size() != args.size())
            throw new VEException("Mismatch in number of arguments")
                .setPosition(action.getPosition());
        for(int i = 0;i < signature.size();i++) {
            ArgType at = signature.get(i);
            Arg arg = args.get(i);
            if(!Arg.compatible(arg.type, at))
                throw new VEException("Type mismatch for argument " + i)
                    .setPosition(action.getPosition());
        }
        action.setArgs(args);
    }

    /////////////////////////////////////////////////////
    // Utils

    static public String
    dumptoken(int token, String lval)
        throws VEException
    {
        StringBuilder buf = new StringBuilder();
        if(token >= 0 && token < '\177') {
            if(token == '\n')
                buf.append("\\n");
            else if(token == EOF)
                buf.append("EOF");
            else
                buf.append(Character.toString((char) token));
        } else
            switch (token) {
            case STRING:
                buf.append('"');
                buf.append(lval);
                buf.append('"');
                break;
            case NUMBER:
                buf.append(lval);
                break;
            case WORD:
                buf.append(lval);
                break;
            default:
                buf.append("UNDEFINED");
                break;
            }
        return buf.toString();
    }
}

