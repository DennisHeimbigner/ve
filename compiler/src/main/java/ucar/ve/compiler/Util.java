/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve.compiler;

import net.sf.corn.cps.CPScanner;
import net.sf.corn.cps.ClassFilter;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

abstract public class Util
{

    public enum EscapeMode
    {
        EMODE_C, EMODE_JAVA
    }

    ;

    static int uid = 0; // for generating unique ids.

    static int nextuid()
    {
        return ++uid;
    }

    static public String
    locatefile(String suffix, List<String> includepaths)
    {
        suffix = suffix.trim();
        if(suffix.charAt(0) == '/') return suffix;
        if(includepaths != null)
            for(int i = 0;i < includepaths.size();i++) {
                String path = includepaths.get(i) + "/" + suffix;
                File f = new File(path);
                if(f.canRead()) return path;
            }
        // Try raw suffix as last resort
        File f = new File(suffix);
        if(f.canRead()) return suffix;
        return null;
    }

    // Replace '.' characters with '_'
    static public String
    escapedname(String name)
    {
        return name.replace('.', '_');
    }

    static List<String>
    parsepath(String name)
    {
        String[] segments = name.split("[.]");
        List<String> slist = new ArrayList<String>();
        for(String s : segments) slist.add(s);
        return slist;
    }

    static String
    mergepath(List<String> pieces, String separator)
    {
        StringBuilder buf = new StringBuilder();
        for(int i = 0;i < pieces.size();i++) {
            if(i > 0) buf.append(separator);
            buf.append(pieces.get(i));
        }
        return buf.toString();
    }

// Printable chars that must be escaped

    // Add in escapes to a string
    static public String
    escapify(String s, char quotemark, EscapeMode emode)
    {
        StringBuilder es = new StringBuilder();
        for(char c : s.toCharArray()) {
            switch (emode) {
            case EMODE_C:
                if(c == '\n') es.append("\\n");
                else if(c == '\r') es.append("\\r");
                else if(c == '\t') es.append("\\t");
                else if(c < ' ' || c == '\177') {
                    // octal encoding
                    String octal = Integer.toOctalString((int) c);
                    while(octal.length() < 3) octal = '0' + octal;
                    es.append("\\" + octal);
                } else if(c == quotemark) {
                    es.append("\\\"");
                } else
                    es.append(c);
                break;

            case EMODE_JAVA:
                if(c == '\n') es.append("\\n");
                else if(c == '\r') es.append("\\r");
                else if(c == '\t') es.append("\\t");
                else if(c < ' ' || c == '\177') {
                    // unicode encoding
                    String ucode = Integer.toHexString((int) c);
                    while(ucode.length() < 4) ucode = '0' + ucode;
                    es.append("\\u" + ucode);
                } else if(c == quotemark) {
                    es.append("\\\"");
                } else
                    es.append(c);
                break;
            }
        }
        return es.toString();
    }


    static public String
    getBaseName(String base)
    {
        // strip off the path part
        int index = base.lastIndexOf("/");
        if(index >= 0) base = base.substring(index + 1, base.length());
        // strip off any extension
        index = base.lastIndexOf(".");
        if(index > 0) base = base.substring(0, index);
        return base;
    }

    static public String
    getFilePrefix(String path)
    {
        // strip off the path part and return it
        int index = path.lastIndexOf("/");
        if(index < 0)
            path = "";
        else
            path = path.substring(0, index);
        return path;
    }


    static public boolean
    getbooleanvalue(String optionvalue)
    {
        boolean boolvalue = false;
        if(optionvalue == null)
            boolvalue = false;
        else if(optionvalue.equalsIgnoreCase("true"))
            boolvalue = true;
        else if(optionvalue.equalsIgnoreCase("false"))
            boolvalue = false;
        else try {
                int num = Integer.parseInt(optionvalue);
                boolvalue = (num != 0);
            } catch (NumberFormatException nfe) {
            } // ignore
        return boolvalue;
    }


    /**
     * Create an instance of a specified class
     * assuming a null constructor.
     *
     * @param classname   to locate
     * @param packagename in which to search; null => all packages
     * @return instance of the class.
     */

    static public Object
    createClassInstance(String classname, String packagename)
        throws CompileException
    {
        // Split the classname in case it already has a prefix.
        List<String> pieces = parsepath(classname);
        String last = pieces.get(pieces.size() - 1); // name component is assumed last
        if(pieces.size() > 1 && last.equals("java")) {
            // remove the .java suffix and reset last
            pieces.remove(pieces.size() - 1);
            last = pieces.get(pieces.size() - 1);
        }
        // keep the class name and remove from path
        classname = last;
        pieces.remove(pieces.size() - 1);
        if(pieces.size() > 0) {
            // merge the prefix
            String prefix = Util.mergepath(pieces, ".");
            if(packagename == null)
                packagename = prefix;
            else
                packagename = packagename + "." + prefix;
        }
        String fullname = (packagename == null ? "" : packagename + ".") + classname;
        List<Class<?>> classlist;
        if(packagename != null) {
            classlist = CPScanner.scanClasses(new ClassFilter()
                .packageName(packagename + ".*")
                .className(classname));
        } else {
            classlist = CPScanner.scanClasses(new ClassFilter()
                .className(classname));
        }
        if(classlist.size() == 0)
            throw new CompileException("Unknown class: " + fullname);
        else if(classlist.size() > 1)
            throw new CompileException("Ambiguous clas: " + fullname);
        Class match = classlist.get(0);
        return createClassInstance(match);
    }

    static public Object
    createClassInstance(Class cl)
        throws CompileException
    {
        // get the constructor
        Constructor constructor = null;
        try {
            constructor = cl.getConstructor();
        // create instance
        Object o = constructor.newInstance();
        return o;
        } catch (Exception e) {
            Util.runtimeCheck(e);
            throw new CompileException(e);
        }
    }

    static public void
    runtimeCheck(Exception e)
    {
        if(e instanceof RuntimeException)
            throw (RuntimeException) e;
    }

    /**
     * Read the contents of a file.
     *
     * @param filename file to read, '-'|null => stdin.
     * @return The contents of the file as a string
     * @throws IOException
     */

    static String
    readfile(String filename)
        throws IOException
    {
        InputStream input = null;
        if(filename == null)
            input = System.in;
        else
            input = new FileInputStream(filename);
        InputStreamReader rdr = new InputStreamReader(input, "UTF-8");
        int c = 0;
        StringBuilder buf = new StringBuilder();
        while((c = rdr.read()) >= 0) {
            buf.append((char) c);
        }
        input.close();
        return buf.toString();
    }


}
