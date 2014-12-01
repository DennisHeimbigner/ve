/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve;

import java.io.*;
import java.util.*;

import static ucar.ve.Types.*;

public class VE
{

    //////////////////////////////////////////////////
    // Constants

    static final public boolean DEBUG = true;
    static final public boolean PARSEDEBUG = true;

    //////////////////////////////////////////////////
    // Instance variables

    protected Configuration config = null;
    protected ActionList program = null;

    // All verb classes
    protected Map<String, Verb> verbs = new HashMap<>();

    //////////////////////////////////////////////////
    // Constructor(s)
    public VE()
        throws VEException
    {
    }

    public VE(Configuration configuration)
        throws VEException
    {
        this();
        reset(configuration);
        loadVerbs();
    }

    public void
    reset(Configuration configuration)
        throws VEException
    {
        this.config = new Configuration(configuration);
        // Validate and initialize the configuration
        validate(config);
        parse();
    }

    protected void
    validate(Configuration cfg)
        throws VEException
    {
        if(cfg.format == null)
            throw new VEException("Invalid configuration: generate format is null");
        if(cfg.output == null) {
            // default to System.out
            cfg.output = new PrintWriter(new OutputStreamWriter(System.out, Util.UTF8));
        }
        if(DEBUG)
            cfg.debug = true;
        if(PARSEDEBUG)
            cfg.parsedebug = true;
        if(cfg.debug)
            cfg.verbose = true;

        if(cfg.verbs == null)
            cfg.verbs = new ArrayList<VerbDef>();
    }

    protected void
    parse()
        throws VEException
    {
        try {
            if(this.config.input == null || this.config.input.length() == 0)
                throw new VEException("Empty input");
            // Create the input parser
            Parser parser = (Parser) Util.createClassInstance(config.format, this);
            if(config.parsedebug)
                parser.setDebugLevel(1);
            parser.parse(this.config.input);
            this.program = parser.getProgram();
        } catch (Exception e) {
            Util.runtimeCheck(e);
            throw new VEException(e);
        }
    }
    //////////////////////////////////////////////////
    // Execution

    public void
    evaluate(Object state)
        throws VEException
    {
        for(int i = 0;i < program.size();i++) {
            Action action = program.get(i);
            action.execute(state);
        }
    }

    //////////////////////////////////////////////////
    // Accessors

    public Configuration
    getConfiguration()
    {
        return this.config;
    }

    public ActionList
    getProgram()
    {
        return this.program;
    }

    public Map<String, Verb>
    getVerbs()
    {
        return this.verbs;
    }


    //////////////////////////////////////////////////
    // Utilities

    protected void
    loadVerbClass(VerbDef def)
        throws VEException
    {
        if(DEBUG)
            System.err.println("Loading " + def.getClass().toString());
        Verb verb;
        try {
            verb = (Verb) Util.createClassInstance(def.getVerbClass());
        } catch (Exception e) {
            Util.runtimeCheck(e);
            throw new VEException("Undefined Verb class: " + def.getClass().toString(), e);
        }
        if(config.verbose && verbs.containsKey(def.getName()))
            System.err.println(String.format("Duplicate verb: %s; replacing old with new", verb.getName()));
        verbs.put(verb.getName(), verb);
    }

    protected void
    loadVerbs()
        throws VEException
    {
        for(int i = 0;i < config.verbs.size();i++) {
            VerbDef def = config.verbs.get(i);
            loadVerbClass(def);
        }
    }

}
