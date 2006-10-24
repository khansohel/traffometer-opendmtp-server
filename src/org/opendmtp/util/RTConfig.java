// ----------------------------------------------------------------------------
// Copyright 2006, Martin D. Flynn
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  Support for hierarchical runtime properties
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/04/23  Martin D. Flynn
//      Changed support for default properties
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.awt.*;
import java.awt.color.*;
import java.net.*;

public class RTConfig
{

    // ------------------------------------------------------------------------
    
    // Cannot initialize here, otherwise we would be unable to override 'configFile'
    //static { startupInit(); }

    // ------------------------------------------------------------------------

    private static String localhostName = null;

    public static String getHostName()
    {
        /* host name */
        if (RTConfig.localhostName == null) {
            try {
                String hd = InetAddress.getLocalHost().getHostName();
                int p = hd.indexOf(".");
                RTConfig.localhostName = (p >= 0)? hd.substring(0,p) : hd;
            } catch (UnknownHostException uhe) {
                RTConfig.localhostName = "UNKNOWN";
            }
        }
        return RTConfig.localhostName;
    }

    // ------------------------------------------------------------------------
    
    private static final String CMDLINE     = "cmdline";
    private static final String CMDLINE_    = CMDLINE + "_";
    private static final String WEBAPP      = "webapp";
    private static final String WEBAPP_     = WEBAPP + "_";
    private static final String _CONF       = ".conf";

    private static String createCmdLineConf(String name)
    {
        if (name != null) {
            return CMDLINE_ + name + _CONF;
        } else {
            return CMDLINE + _CONF;
        }
    }

    private static String createWebAppConf(String ctx)
    {
        if (ctx != null) {
            return WEBAPP_ + ctx + _CONF;
        } else {
            return WEBAPP + _CONF;
        }
    }
    
    // ------------------------------------------------------------------------

    private static final int    THREAD_LOCAL        = 0;
    private static final int    SERVLET_CONFIG      = 1;
    private static final int    SERVLET_CONTEXT     = 2;
    private static final int    COMMAND_LINE        = 3;
    private static final int    CONFIG_FILE         = 4;
    private static final int    SYSTEM              = 5;

    private static RTProperties CFG_PROPERTIES[] = new RTProperties[] {
        null,                                       // ThreadLocal properties
        null,                                       // ServletConfig properties
        null,                                       // ServletContext properties
        null,                                       // CommandLine properties [on-demand init]
        null,                                       // ConfigFile properties [lazy init]
        new RTProperties(System.getProperties()),   // System properties
    };
    
    public static RTProperties getThreadProperties()
    {
        if (CFG_PROPERTIES[THREAD_LOCAL] == null) {
            synchronized (CFG_PROPERTIES) {
                if (CFG_PROPERTIES[THREAD_LOCAL] == null) { // still null?
                    CFG_PROPERTIES[THREAD_LOCAL] = new RTProperties(new ThreadLocalMap());
                }
            }
        }
        return CFG_PROPERTIES[THREAD_LOCAL];
    }
    
    public static RTProperties getServletContextProperties()
    {
        return CFG_PROPERTIES[SERVLET_CONTEXT]; // will be null until this is fully implemented
    }
    
    public static RTProperties getServletConfigProperties()
    {
        return CFG_PROPERTIES[SERVLET_CONFIG]; // will be null until this is fully implemented
    }
    
    public static RTProperties getCommandLineProperties()
    {
        return CFG_PROPERTIES[COMMAND_LINE]; // may be null if not initialized
    }
    
    public static RTProperties getConfigFileProperties()
    {
        if (CFG_PROPERTIES[CONFIG_FILE] == null) {
            // this should have been initialized before, but force initialization now
            Print.logInfo("Late initialization!!!");
            startupInit(); 
        }
        return CFG_PROPERTIES[CONFIG_FILE];
    }
    
    public static RTProperties getSystemProperties()
    {
        return CFG_PROPERTIES[SYSTEM]; // always defined
    }
    
    public static RTProperties getPropertiesForKey(String key)
    {
        if (key != null) {
            if (!isInitialized()) {
                // 'Print._println...' used here to eliminate possible recursion stack-overflow
                //Print._println(null, "ConfigFile not yet loaded");
                //Thread.dumpStack();
                // continue ...
            }
            // look for key in our property list stack
            for (int i = 0; i < CFG_PROPERTIES.length; i++) {
                RTProperties rtProps = CFG_PROPERTIES[i];
                if ((rtProps != null) && rtProps.hasProperty(key)) { 
                    return rtProps; 
                }
            }
            // still not found, try the default properties
            RTProperties dftProps = RTKey.getDefaultProperties();
            if ((dftProps != null) && dftProps.hasProperty(key)) {
                return dftProps;
            }
        }
        return null;
    }
    
    // ------------------------------------------------------------------------
    
    public static void setCommandLineArgs(String argv[])
    {
        RTConfig.setCommandLineArgs(argv, false);
    }
    
    public static void setCommandLineArgs(String argv[], boolean testMode)
    {
        if (argv != null) {
            RTProperties cmdLineProps = new RTProperties(argv);
            cmdLineProps.setIgnoreKeyCase(true);
            cmdLineProps.setProperty(RTKey.MAIN_CLASS, getMainClass());
            if (CFG_PROPERTIES[COMMAND_LINE] == null) {
                // first initialization
                CFG_PROPERTIES[COMMAND_LINE] = cmdLineProps;     
                startupInit(); // initialize now to allow for overriding 'configFile'
            } else {
                // subsequent re-initialization
                CFG_PROPERTIES[COMMAND_LINE].setProperties(cmdLineProps);
            }
        }
    }
    
    // ------------------------------------------------------------------------

    public static void setServletContextProperties(Map props)
    {
        // Don't do anything right now except initialize RTConfig
        // --------------------------------------------------------------
        // Set via servlet v2.3 ServletContextListener
        // Reference: 
        //  http://livedocs.macromedia.com/jrun/4/Programmers_Guide/servletlifecycleevents3.htm
        CFG_PROPERTIES[SERVLET_CONTEXT] = new RTProperties(props);
        startupInit();
        RTConfig.setWebApp(true); // force isWebapp=true
        Print.logInfo("DebugMode == " + RTConfig.isDebugMode());
    }
    
    public static void clearServletContextProperties(Object servlet)
    {
        CFG_PROPERTIES[SERVLET_CONTEXT] = null;
    }
    
    // ------------------------------------------------------------------------

    public static void setServletConfigProperties(Object servlet, Map props)
    {
        // Don't do anything right not except initialize RTConfig
        // --------------------------------------------------------------
        // Ideally, this will use some kind of map indexed by the servlet
        // class to store the servlet specific properties.  The stack
        // will then be examined to find the appropriate Class type and
        // then the corresponding properties will be used.
        startupInit();
    }
    
    public static void clearServletConfigProperties(Object servlet)
    {
        CFG_PROPERTIES[SERVLET_CONFIG] = null;
    }
    
    // ------------------------------------------------------------------------

    private static boolean _didStartupInit = false;
    
    public static boolean isInitialized()
    {
        return _didStartupInit;
    }
    
    public static synchronized void startupInit()
    {
        
        /* check init */
        if (_didStartupInit) { return; }
        _didStartupInit = true;
                
        /* config file */
        File configFile = getConfigFile();
        if (configFile != null) {
            CFG_PROPERTIES[CONFIG_FILE] = new RTProperties(configFile);
        } else {
            Print.logWarn("No config file was found");
            // The 'CONFIG_FILE' entry must be non-null
            CFG_PROPERTIES[CONFIG_FILE] = new RTProperties();
        }
        
        /* initialize http proxy */
        // http.proxyHost
        // http.proxyPort
        // http.nonProxyHosts
        String proxyHost = RTConfig.getString(RTKey.HTTP_PROXY_HOST);
        int    proxyPort = RTConfig.getInt   (RTKey.HTTP_PROXY_PORT);
        if ((proxyHost != null) && (proxyPort > 1024)) {
            String port = String.valueOf(proxyPort);
            Properties sysProp = System.getProperties();
            sysProp.put("proxySet" , "true");           // <  jdk 1.3
            sysProp.put("proxyHost", proxyHost);        // <  jdk 1.3
            sysProp.put("proxyPort", port);             // <  jdk 1.3
            sysProp.put("http.proxyHost", proxyHost);   // >= jdk 1.3
            sysProp.put("http.proxyPort", port);        // >= jdk 1.3
            sysProp.put("firewallSet", "true");         // MS JVM
            sysProp.put("firewallHost", proxyHost);     // MS JVM
            sysProp.put("firewallPort", port);          // MS JVM
        }
        
        /* URLConnection timeouts */
        // sun.net.client.defaultConnectTimeout
        // sun.net.client.defaultReadTimeout
        long urlConnectTimeout = RTConfig.getLong(RTKey.URL_CONNECT_TIMEOUT);
        if (urlConnectTimeout > 0) {
            String timeout = String.valueOf(urlConnectTimeout);
            System.getProperties().put("sun.net.client.defaultConnectTimeout", timeout);
        }
        long urlReadTimeout = RTConfig.getLong(RTKey.URL_READ_TIMEOUT);
        if (urlReadTimeout > 0) {
            String timeout = String.valueOf(urlReadTimeout);
            System.getProperties().put("sun.net.client.defaultReadTimeout", timeout);
        }

    }
    
    protected static File getConfigFile()
    {
        
        /* explicitly defined (command-line, etc) */
        if (hasProperty(RTKey.CONFIG_FILE)) {
            return getFile(RTKey.CONFIG_FILE);
        } //else
        //if (hasProperty(RTKey.CONFIG_FILE_ALT)) {
        //    return getFile(RTKey.CONFIG_FILE_ALT);
        //}
        
        /* special config for servlets */
        Class servletClass = null;
        if (getCommandLineProperties() != null) {
            
            /* check for alternate command line override 'conf' */
            File cmdLineCfgFile = getCommandLineProperties().getFile(RTKey.COMMAND_LINE_CONF, null);
            if (cmdLineCfgFile != null) {
                return cmdLineCfgFile;
            }
            
            // Search Order:
            //   .../<server>/cmdline_<mainclass>.conf
            //   .../<server>/cmdline.conf
            //   .../cmdline_<mainclass>.conf
            //   .../cmdline.conf
            //   .../default.conf
            
            /* check for "cmdline_<mainclass>.conf" */
            String mainClass = RTConfig.getString(RTKey.MAIN_CLASS, null);
            if (mainClass != null) {
                int p = mainClass.lastIndexOf(".");
                String mainName = (p >= 0)? mainClass.substring(p + 1) : mainClass;
                File mainCfgFile = _getConfigFile(createCmdLineConf(mainName));
                if (mainCfgFile != null) {
                    return mainCfgFile;
                }
            }
            
            /* check for "cmdline.conf" */
            File rtCfgFile = _getConfigFile(createCmdLineConf(null));
            if (rtCfgFile != null) {
                return rtCfgFile;
            }
            
        } else
        if (getServletContextProperties() != null) {
            
            // Search Order:
            //   .../<server>/webapp_<context>.conf
            //   .../webapp_<context>.conf
            //   .../<server>/webapp.conf
            //   .../webapp.conf
            //   .../default.conf

            /* check for "webapp_<context>.conf" */
            String ctxPath = getServletContextProperties().getString(RTKey.WEBAPP_CONTEXT_PATH, null);
            if (ctxPath != null) {
                File ctxCfgFile = _getConfigFile(createWebAppConf(ctxPath));
                if (ctxCfgFile != null) {
                    return ctxCfgFile;
                }
            }
                        
            /* check for generic "webapp.conf" */
            File webappCfgFile = _getConfigFile(createWebAppConf(null));
            if (webappCfgFile != null) {
                return webappCfgFile;
            }
            
        } else
        if ((servletClass = getServletClass()) != null) {
            
            // If we are here, then this means that 'ContextListener' was not specified for this 
            // context and we are initializing late (possible too late).
            Print.logInfo("--------------------------------------------------------");
            Print.logInfo("******* WebApp: " + StringTools.className(servletClass));
            Print.logInfo("--------------------------------------------------------");
            
            /* check for generic "webapp.conf" */
            File webappCfgFile = _getConfigFile(createWebAppConf(null));
            if (webappCfgFile != null) {
                return webappCfgFile;
            }
            
        } else {
            
            Print.logStackTrace("CommandLine/ServletContext properties not specified");
            
        }
        
        /* check in config file dir */
        File dftDirCfgFile = _getConfigFile(getString(RTKey.CONFIG_FILE));
        if (dftDirCfgFile != null) {
            return dftDirCfgFile;
        }
        
        /* check for default config file as-is */
        File dftCfgFile = getFile(RTKey.CONFIG_FILE);
        if (dftCfgFile.isFile()) {
            return dftCfgFile;
        }
        
        /* no config file */
        return null;

    }
    
    protected static File _getConfigFile(String name)
    {
        
        /* check name */
        if ((name == null) || name.equals("")) {
            return null;
        }
        
        /* check for <name> in config file directory */
        File cfgDir = getFile(RTKey.CONFIG_FILE_DIR);
        if (cfgDir.isDirectory()) {
            
            /* check host directory */
            String host  = RTConfig.getHostName();
            File hostCfgDir = (host != null)? new File(cfgDir, host) : null;
            if ((hostCfgDir != null) && hostCfgDir.isDirectory()) {
                File cfgFile = new File(hostCfgDir, name);
                if (cfgFile.isFile()) {
                    return cfgFile; // .../conf/<host>/<name>
                } else
                if (name.indexOf("_") < 0) { // skip "cmdline_prog.conf" & "webapp_ctx.conf"
                    Print.logWarn("ConfigFile not found: " + cfgFile);
                }
            } else {
                Print.logWarn("Host ConfigDir not found: " + hostCfgDir);
            }
            
            /* check default directory */
            File cfgFile = new File(cfgDir, name);
            if (cfgFile.isFile()) {
                return cfgFile; // .../conf/<name>
            } else
            if (name.indexOf("_") < 0) { // skip "cmdline_prog.conf" & "webapp_ctx.conf"
                Print.logWarn("ConfigFile not found: " + cfgFile);
            }
                
        }
        
        /* still not found */
        return null;
    }
    
    // ------------------------------------------------------------------------

    public static Properties loadResourceProperties(String name)
    {
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            InputStream inpStream = cl.getResourceAsStream(name);
            if (inpStream != null) {
                Properties props = new Properties();
                props.load(inpStream);
                return props;
            } else {
                return null;
            }
        } catch (Throwable t) {
            Print.logException("Loading properties: " + name, t);
            return null;
        }
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    public static boolean hasProperty(String key)
    {
        return (getPropertiesForKey(key) != null);
    }

    public static boolean hasProperty(String key[])
    {
        if (key != null) {
            for (int i = 0; i < key.length; i++) {
                RTProperties rtp = getPropertiesForKey(key[i]);
                if (rtp != null) { return true; }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------

    public static Object getProperty(String key)
    {
        return getProperty(key, null);
    }
    
    public static Object getProperty(String key, Object dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getProperty(key, dft) : dft;
    }

    public static void setProperty(String key, Object value)
    {
        getConfigFileProperties().setProperty(key, value);
        if ((key != null) && (value == null)) {
            getSystemProperties().removeProperty(key);
        }
    }
    
    public static void setProperties(Properties props)
    {
        getConfigFileProperties().setProperties(props);
    }
    
    // ------------------------------------------------------------------------

    // Extract a Map containing a group of key/values from the config file properties
    public static Map extractMap(String keyEnd, String valEnd)
    {
        return getConfigFileProperties().extractMap(keyEnd, valEnd);
    }
    
    // ------------------------------------------------------------------------

    public static String getString(String key)
    {
        return getString(key, null);
    }

    public static String getString(String key, String dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getString(key, dft) : dft;
    }
    
    public static String getString(String key[], String dft)
    {
        if (key != null) {
            for (int i = 0; i < key.length; i++) {
                RTProperties rtp = getPropertiesForKey(key[i]);
                if (rtp != null) { return rtp.getString(key[i], dft); }
            }
        }
        return dft;
    }

    public static void setString(String key, String value)
    {
        getConfigFileProperties().setString(key, value);
    }

    // ------------------------------------------------------------------------
    
    public static String[] getStringArray(String key)
    {
        return getStringArray(key, null);
    }
    
    public static String[] getStringArray(String key, String dft[])
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getStringArray(key, dft) : dft;
    }

    public static String[] getStringArray(String key[], String dft[])
    {
        if (key != null) {
            for (int i = 0; i < key.length; i++) {
                RTProperties rtp = getPropertiesForKey(key[i]);
                if (rtp != null) { 
                    return rtp.getStringArray(key[i], dft); 
                }
            }
        }
        return dft;
    }

    public static void setStringArray(String key, String val[])
    {
        getConfigFileProperties().setStringArray(key, val);
    }

    // ------------------------------------------------------------------------

    public static File getFile(String key)
    {
        return getFile(key, null);
    }

    // do not include this method, otherwise "getFile(file, null)" would be ambiguous
    //public File getFile(String key, String dft)

    public static File getFile(String key, File dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getFile(key, dft) : dft;
    }

    public static File getFile(String key[], File dft)
    {
        if (key != null) {
            for (int i = 0; i < key.length; i++) {
                RTProperties rtp = getPropertiesForKey(key[i]);
                if (rtp != null) { return rtp.getFile(key[i], dft); }
            }
        }
        return dft;
    }

    public static void setFile(String key, File value)
    {
        getConfigFileProperties().setFile(key, value);
    }

    // ------------------------------------------------------------------------
    
    public static double getDouble(String key)
    {
        return getDouble(key, 0.0);
    }
    
    public static double getDouble(String key, double dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getDouble(key, dft) : dft;
    }

    public static double getDouble(String key[], double dft)
    {
        if (key != null) {
            for (int i = 0; i < key.length; i++) {
                RTProperties rtp = getPropertiesForKey(key[i]);
                if (rtp != null) { return rtp.getDouble(key[i], dft); }
            }
        }
        return dft;
    }

    public static void setDouble(String key, double value)
    {
        getConfigFileProperties().setDouble(key, value);
    }

    // ------------------------------------------------------------------------
    
    public static float getFloat(String key)
    {
        return getFloat(key, 0.0F);
    }

    public static float getFloat(String key, float dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getFloat(key, dft) : dft;
    }

    public static float getFloat(String key[], float dft)
    {
        if (key != null) {
            for (int i = 0; i < key.length; i++) {
                RTProperties rtp = getPropertiesForKey(key[i]);
                if (rtp != null) { return rtp.getFloat(key[i], dft); }
            }
        }
        return dft;
    }

    public static void setFloat(String key, float value)
    {
        getConfigFileProperties().setFloat(key, value);
    }

    // ------------------------------------------------------------------------
    
    public static long getLong(String key)
    {
        return getLong(key, 0L);
    }

    public static long getLong(String key, long dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getLong(key, dft) : dft;
    }

    public static long getLong(String key[], long dft)
    {
        if (key != null) {
            for (int i = 0; i < key.length; i++) {
                RTProperties rtp = getPropertiesForKey(key[i]);
                if (rtp != null) { return rtp.getLong(key[i], dft); }
            }
        }
        return dft;
    }

    public static void setLong(String key, long value)
    {
        getConfigFileProperties().setLong(key, value);
    }

    // ------------------------------------------------------------------------
    
    public static int getInt(String key)
    {
        return getInt(key, 0);
    }

    public static int getInt(String key, int dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getInt(key, dft) : dft;
    }

    public static int getInt(String key[], int dft)
    {
        if (key != null) {
            for (int i = 0; i < key.length; i++) {
                RTProperties rtp = getPropertiesForKey(key[i]);
                if (rtp != null) { return rtp.getInt(key[i], dft); }
            }
        }
        return dft;
    }

    public static void setInt(String key, int value)
    {
        getConfigFileProperties().setInt(key, value);
    }

    // ------------------------------------------------------------------------
    
    public static boolean getBoolean(String key)
    {
        return getBoolean(key, hasProperty(key));
    }

    public static boolean getBoolean(String key, boolean dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        if (rtp == null) {
            return dft; // no key, return default
        } else {
            String s = rtp.getString(key, "");
            if ((s != null) && s.equals("")) {
                return rtp.getBoolean(key, true); // key with no argument
            } else {
                return rtp.getBoolean(key, dft);  // key with argument, use dft if not parsable.
            }
        }
        //return (rtp != null)? rtp.getBoolean(key, dft) : dft;
    }

    public static boolean getBoolean(String key[], boolean dft)
    {
        if (key != null) {
            for (int i = 0; i < key.length; i++) {
                RTProperties rtp = getPropertiesForKey(key[i]);
                if (rtp != null) { return rtp.getBoolean(key[i], dft); }
            }
        }
        return dft;
    }
    
    public static void setBoolean(String key, boolean value)
    {
        getConfigFileProperties().setBoolean(key, value);
    }

    // ------------------------------------------------------------------------

    public static void setAdminMode(boolean admin)
    {
        setBoolean(RTKey.ADMIN_MODE, admin);
    }
    
    public static boolean isAdminMode()
    {
        return getBoolean(RTKey.ADMIN_MODE);
    }

    // ------------------------------------------------------------------------

    public static void setDebugMode(boolean debug)
    {
        setBoolean(RTKey.DEBUG_MODE, debug);
    }
    
    //private static int _debug_recursion = 0;
    public static boolean isDebugMode()
    {
        //if (_debug_recursion > 0) { Thread.dumpStack(); System.exit(0); }
        //try { _debug_recursion++;
        return !isInitialized() || getBoolean(RTKey.DEBUG_MODE);
        //} finally { _debug_recursion--; }
    }

    // ------------------------------------------------------------------------

    public static void setTestMode(boolean test)
    {
        setBoolean(RTKey.TEST_MODE, test);
    }
    
    public static boolean isTestMode()
    {
        return getBoolean(RTKey.TEST_MODE);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
        
    private static Boolean isRunningAsWebApp = null;

    public static void setWebApp(boolean webapp)
    {
        setBoolean(RTKey.IS_WEBAPP, webapp);
        isRunningAsWebApp = null; // <== to bypass Boolean check
    }
    
    public static boolean isWebApp()
    {
        
        /* already know where we are running? */
        if (isRunningAsWebApp != null) {
            return isRunningAsWebApp.booleanValue();
        }
        
        /* "isWebApp" explicitly defined? */
        if (hasProperty(RTKey.IS_WEBAPP)) {
            return getBoolean(RTKey.IS_WEBAPP);
        }
        
        /* check invocation stack */
        isRunningAsWebApp = new Boolean(_isWebApp_2());
        return isRunningAsWebApp.booleanValue();

    }

    private static String WebAppClassNames[] = {
        "javax.servlet.http.HttpServlet", // as long as the servlet didn't override 'service'
        "org.apache.catalina.core.ApplicationFilterChain"
    };
    protected static boolean _isWebApp_1()
    {
        // We should also check the invocation stack
        // A typical stack-trace segment for a servlet is as follows:
        //   ...
        //   at com.mdflynn.war.DataMessage.doPost(DataMessage.java:46)
        //   at javax.servlet.http.HttpServlet.service(HttpServlet.java:760)
        //   at javax.servlet.http.HttpServlet.service(HttpServlet.java:853)
        //   at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:247)
        //   at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:193)
        //   at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:256)
        //   ...
        // Possible search Strings would be:
        //  - "javax.servlet.http.HttpServlet" (assuming 'service' was not overridden)
        //  - "org.apache.catalina.core.ApplicationFilterChain" (only valid for Tomcat)
        Throwable t = new Throwable();
        t.fillInStackTrace();
        //t.printStackTrace();
        StackTraceElement stackFrame[] = t.getStackTrace();
        for (int i = 0; i < stackFrame.length; i++) {
            String cn = stackFrame[i].getClassName();
            for (int w = 0; w < WebAppClassNames.length; w++) {
                if (cn.equalsIgnoreCase(WebAppClassNames[w])) {
                    return true;
                }
            }
        }
        return false;
    }
    
    protected static boolean _isWebApp_2()
    {
        return (getServletClass() != null);
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private static Class Main_class = null;
    public static Class getMainClass()
    {
        if (Main_class == null) {
            Class lastClz = null;
            for (int sf = 2; ; sf++) {
                Class clz = OSTools.getCallerClass(sf);
                if (clz == null) { break; }
                lastClz = clz;
            }
            Main_class = lastClz;
        }
        return Main_class;
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private static String SERVLET_CLASS = "javax.servlet.Servlet"; // GenericServlet
    private static boolean Servlet_init = false;
    private static Class Servlet_class = null;
    public static Class getServletClass()
    {
        
        /* init for Servlet class */
        if (!Servlet_init) {
            try {
                Servlet_class = Class.forName(SERVLET_CLASS);
            } catch (Throwable t) {
                // class not found?
                Print.logWarn("Not a servlet - running as application?");
            }
            Servlet_init = true;
        }
        
        /* find Servlet in invocation stack */
        if (Servlet_class != null) {
            for (int sf = 2; ; sf++) {
                Class clz = OSTools.getCallerClass(sf);
                if (clz == null) { break; }
                if (Servlet_class.isAssignableFrom(clz)) {
                    return clz;
                }
            }
        }
        
        /* not found */
        return null;
        
    }
    
    // ------------------------------------------------------------------------

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        Print.logFatal("DebugMode [false] = " + RTConfig.getBoolean(RTKey.DEBUG_MODE,false));
        Print.logError("DebugMode [false] = " + RTConfig.getBoolean(RTKey.DEBUG_MODE,false));
        Print.logWarn ("DebugMode [true ] = " + RTConfig.getBoolean(RTKey.DEBUG_MODE,true));
        Print.logInfo ("DebugMode [undef] = " + RTConfig.getBoolean(RTKey.DEBUG_MODE));
        Print.logDebug("DebugMode [undef] = " + RTConfig.getBoolean(RTKey.DEBUG_MODE));
    }
    
}
