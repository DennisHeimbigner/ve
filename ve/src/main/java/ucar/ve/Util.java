/*
This software is released under the Licence terms
described in the file LICENSE.txt.
*/

package ucar.ve;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

abstract public class Util
{

    /**
     * Hex digits
     */
    static final String hexdigits = "0123456789abcdefABCDEF";

    static final public Charset UTF8 = Charset.forName("UTF-8");

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
     */

    static public Object
    createClassInstance(String classname, Object... params)
        throws Exception
    {
        // Split the classname in case it already has a prefix.
        List<String> pieces = parsepath(classname);
        String last = pieces.get(pieces.size() - 1); // name component is assumed last
        if(pieces.size() > 1 && last.equals("java")) {
            // remove the .java suffix and reset last
            pieces.remove(pieces.size() - 1);
            last = pieces.get(pieces.size() - 1);
        }
        // recreate the full classname
        classname = Util.mergepath(pieces, ".");
        Class cl = Class.forName(classname);
        return createClassInstance(cl, params);
    }

    static public Object
    createClassInstance(Class cl, Object... params)
        throws Exception
    {
        Constructor constructor;
        Object o;
        if(params == null || params.length == 0) {
            constructor = cl.getConstructor();
            // create instance
            o = constructor.newInstance(params);
        } else { // Figure out the param types
            Class[] sig = new Class[params.length];
            for(int i = 0;i < sig.length;i++)
                sig[i] = params[i].getClass();
            // get the constructor
            constructor = cl.getConstructor(sig);
            // create instance
            o = constructor.newInstance(params);
        }
        return o;
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

    static public String
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

    static int
    tohex(int c)
        throws VEException
    {
        if(c >= 'a' && c <= 'f') return (c - 'a') + 0xa;
        if(c >= 'A' && c <= 'F') return (c - 'A') + 0xa;
        if(c >= '0' && c <= '9') return (c - '0');
        return -1;
    }

/*
 * Source: http://stackoverflow.com/posts/2593771/revisions
 */

/**
 * Useful class for dynamically changing the classpath, adding classes during runtime. 
 */
//static public class ClasspathHacker {
    /**
     * Parameters of the method to add a URL to the System classes. 
     */
    /*
    private static final Class<?>[] parameters = new Class[]{URL.class};
     */

    /**
     * Adds a file to the classpath.
     * @param s a String pointing to the file
     * @throws IOException
     */
    /*
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }
      */

    /**
     * Adds a file to the classpath
     * @param f the file to be added
     * @throws IOException
     */
    /*
    public static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }
    */

    /**
     * Adds the content pointed by the URL to the classpath.
     * @param u the URL pointing to the content to be added
     * @throws IOException
     */
    /*
    public static void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ u }); 
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }        
    }
    */

/*
    public static void main(String args[]) throws IOException, SecurityException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
        addFile("C:\\dynamicloading.jar");
        Constructor<?> cs = ClassLoader.getSystemClassLoader().loadClass("test.DymamicLoadingTest").getConstructor(String.class);
        DymamicLoadingTest instance = (DymamicLoadingTest)cs.newInstance();
        instance.test();
    }
    }
*/

}
