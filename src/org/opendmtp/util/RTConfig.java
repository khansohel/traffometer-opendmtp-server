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

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;

/**
 * Provides support for runtime properties in RTProperties class.
 * 
 * @author Martin D. Flynn
 * @author Nam Nguyen
 */
public class RTConfig
{

    // ------------------------------------------------------------------------
    
    // Cannot initialize here, otherwise we would be unable to override 'configFile'
    //static { startupInit(); }

    // ------------------------------------------------------------------------
    /**
     * Holds the name of the local host.
     */
    private static String localhostName = null;

    /**
     * Returns the name of the local host.
     * 
     * @return The name of the local host.
     */
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
    
    /**
     * Holds the value "cmdline".
     */
    private static final String CMDLINE     = "cmdline";
    /**
     * Holds the value CMDLINE + "_".
     */
    private static final String CMDLINE_    = CMDLINE + "_";
    /**
     * Holds the value "webapp".
     */
    private static final String WEBAPP      = "webapp";
    /**
     * Holds the value WEBAPP + "_";.
     */
    private static final String WEBAPP_     = WEBAPP + "_";
    /**
     * Holds the value ".conf".
     */
    private static final String _CONF       = ".conf";

    /**
     * Creates a command line configuration with the specified name.
     * 
     * @param name The name of the configuration.
     * @return A string that represents the command line configuration.
     */
    private static String createCmdLineConf(String name)
    {
        if (name != null) {
            return CMDLINE_ + name + _CONF;
        } else {
            return CMDLINE + _CONF;
        }
    }

    /**
     * Creates a web application configuration with the specified name.
     * 
     * @param ctx The name of the configuration.
     * @return A string that represents the web application configuration.
     */
    private static String createWebAppConf(String ctx)
    {
        if (ctx != null) {
            return WEBAPP_ + ctx + _CONF;
        } else {
            return WEBAPP + _CONF;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
     * Holds numerical value 0.
     */
    private static final int    THREAD_LOCAL        = 0;
    /**
     * Holds numerical value 1.
     */
    private static final int    SERVLET_CONFIG      = 1;
    /**
     * Holds numerical value 2.
     */
    private static final int    SERVLET_CONTEXT     = 2;
    /**
     * Holds numerical value 3.
     */
    private static final int    COMMAND_LINE        = 3;
    /**
     * Holds numerical value 4.
     */
    private static final int    CONFIG_FILE         = 4;
    /**
     * Holds numerical value 5.
     */
    private static final int    SYSTEM              = 5;
    
    /**
     * Holds instances of RTProperties for different types of configuration.
     */
    private static RTProperties CFG_PROPERTIES[] = new RTProperties[] {
        null,                                       // ThreadLocal properties
        null,                                       // ServletConfig properties
        null,                                       // ServletContext properties
        null,                                       // CommandLine properties [on-demand init]
        null,                                       // ConfigFile properties [lazy init]
        new RTProperties(System.getProperties()),   // System properties
    };
    
    /**
     * Returns the local thread's configuration properties.
     * 
     * @return The local thread's configuration properties.
     */
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
    
    /**
     * Returns servlet context properties.
     * 
     * @return Servlet context properties.
     */
    public static RTProperties getServletContextProperties()
    {
        return CFG_PROPERTIES[SERVLET_CONTEXT]; // will be null until this is fully implemented
    }
    
    /**
     * Returns servlet configuration properties.
     * 
     * @return Servlet configuration properties.
     */
    public static RTProperties getServletConfigProperties()
    {
        return CFG_PROPERTIES[SERVLET_CONFIG]; // will be null until this is fully implemented
    }
    
    /**
     * Returns command line properties.
     * @return Command line properties.
     */
    public static RTProperties getCommandLineProperties()
    {
        return CFG_PROPERTIES[COMMAND_LINE]; // may be null if not initialized
    }
    
    /**
     * Returns configuration file properties.
     * 
     * @return Configuration file properties.
     */
    public static RTProperties getConfigFileProperties()
    {
        if (CFG_PROPERTIES[CONFIG_FILE] == null) {
            // this should have been initialized before, but force initialization now
            Print.logInfo("Late initialization!!!");
            startupInit(); 
        }
        return CFG_PROPERTIES[CONFIG_FILE];
    }
    
    /**
     * Returns system properties.
     * 
     * @return System properties.
     */
    public static RTProperties getSystemProperties()
    {
        return CFG_PROPERTIES[SYSTEM]; // always defined
    }
    
    /**
     * Returns the configurations that has the specific property.
     * 
     * @param key The property to look for.
     * @return Configurations that has the specific property.
     */
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
    /**
     * Sets the command line properties.
     * 
     * @param argv The arguments to be set in command line.
     */
    public static void setCommandLineArgs(String argv[])
    {
        RTConfig.setCommandLineArgs(argv, false);
    }
    
    /**
     * Sets the command line properties.
     * 
     * @param argv The arguments to be set in command line.
     * @param testMode Specifies that this is whether a test or not.
     */
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

    /**
     * Sets the servlet context properties.
     * 
     * @param props A map holding properties to be used.
     */
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
    
    /**
     * Removes all properties of a specific servlet context.
     * 
     * @param servlet The argument is not used in this function.
     */
    public static void clearServletContextProperties(Object servlet)
    {
        CFG_PROPERTIES[SERVLET_CONTEXT] = null;
    }
    
    // ------------------------------------------------------------------------

    /**
     * Does not do anything right now except initialize RTConfig.
     * Ideally, this will use some kind of map indexed by the servlet
     * class to store the servlet specific properties.  The stack
     * will then be examined to find the appropriate Class type and
     * then the corresponding properties will be used.
     * 
     * @param servlet a servlet class.
     * @param props map of properties.
     */
    public static void setServletConfigProperties(Object servlet, Map props)
    {
        // Don't do anything right not except initialize RTConfig
        // --------------------------------------------------------------
         
        startupInit();
    }
    
    /**
     * Removes all properties of a specific servlet configuration.
     * 
     * @param servlet The argument is not used in this function.
     */
    public static void clearServletConfigProperties(Object servlet)
    {
        CFG_PROPERTIES[SERVLET_CONFIG] = null;
    }
    
    // ------------------------------------------------------------------------

    /**
     * Specifies whether the initialization at startup was performed.
     */
    private static boolean _didStartupInit = false;
    
    /**
     * Specifies whether the connection is initialized or not.
     * 
     * @return True if the connection is initialized, False otherwise.
     */
    public static boolean isInitialized()
    {
        return _didStartupInit;
    }
    
    /**
     * Initializes the connection.
     */
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
    
    /**
     * Returns the configuration file properties.
     * 
     * @return The configuration file properties.
     */
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
    
    /**
     * Returns the configuration file properties that matches the specified name.
     * 
     * @param name The specific property to look for.
     * @return The configuration file properties.
     */    
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

    /**
     * Loads the properties from input stream.
     * 
     * @param name The name of the configuration.
     * @return List of properties loaded.
     */
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
    /**
     * Specifies whether a property exists.
     * 
     * @param key The property to look for.
     * @return True if the property exists, False otherwise.
     */
    public static boolean hasProperty(String key)
    {
        return (getPropertiesForKey(key) != null);
    }

    /**
     * Specifies whether some properties exists.
     * 
     * @param key The properties to look for.
     * @return True if the property exists, False otherwise.
     */
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
    /**
     * Returns the value of a specified property.
     * 
     * @param key The property to look for.
     * @return The value of the specified property.
     */
    public static Object getProperty(String key)
    {
        return getProperty(key, null);
    }
    
    /**
     * Returns the value of a specified property.
     * Returns dft if the value is null.
     * 
     * @param key The property to look for.
     * @param dft The value to return if property has value null.
     * @return The value of the specified property.
     */
    public static Object getProperty(String key, Object dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getProperty(key, dft) : dft;
    }

    /**
     * Sets the value of a property.
     * 
     * @param key The property to look for.
     * @param value The new value for the property.
     */
    public static void setProperty(String key, Object value)
    {
        getConfigFileProperties().setProperty(key, value);
        if ((key != null) && (value == null)) {
            getSystemProperties().removeProperty(key);
        }
    }
    
    /**
     * Adds, removes or changes the values of the current properties.
     * 
     * @param props The list of properties to be added into the current list.
     */
    public static void setProperties(Properties props)
    {
        getConfigFileProperties().setProperties(props);
    }
    
    // ------------------------------------------------------------------------

    /**
     * Extracts a Map containing a group of key/values from the config file properties.
     * 
     * @param keyEnd A substring that identifies which properties to extract.
     * @param valEnd The value to be set for the extracted properties.
     * @return The extracted from the runtime configuration Map containing a group of key/values.
     */
    public static Map extractMap(String keyEnd, String valEnd)
    {
        return getConfigFileProperties().extractMap(keyEnd, valEnd);
    }
    
    // ------------------------------------------------------------------------

    /**
     * Returns the value of a specified property.
     * 
     * @param key The property to look for.
     * @return String representation of the value.
     */
    public static String getString(String key)
    {
        return getString(key, null);
    }

    /**
     * Returns the value of a specified property.
     * Returns dft if the value is null.
     * 
     * @param key The property to look for.
     * @param dft The value to return in case the property's value is null.
     * @return String representation of the value.
     */
    public static String getString(String key, String dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getString(key, dft) : dft;
    }
    
    /**
     * Returns the value of specified properties.
     * Returns dft if the value is null.
     * 
     * @param key The properties to look for.
     * @param dft The value to return in case the property's value is null.
     * @return String representation of the value.
     */
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

    /**
     * Sets the value of a property.
     * 
     * @param key The property to look for.
     * @param value The new value.
     */
    public static void setString(String key, String value)
    {
        getConfigFileProperties().setString(key, value);
    }

    // ------------------------------------------------------------------------
    /**
     * Returns the value of the specified property as an array of string.
     * This is for the case when the value is a list of words separated by comma.
     * 
     * @param key The property to look for.
     * @return The value of the specified property as an array of string.
     */
    public static String[] getStringArray(String key)
    {
        return getStringArray(key, null);
    }
    
    /**
     * Returns the value of the specified property as an array of string.
     * This is for the case when the value is a list of words separated by comma.
     * 
     * @param key The property to look for.
     * @param dft An array of string to to be returned if the property's value is null.
     * @return The value of the specified property as an array of string.
     */
    public static String[] getStringArray(String key, String dft[])
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getStringArray(key, dft) : dft;
    }

    /**
     * Returns the value of the specified properties as an array of string.
     * This is for the case when the value is a list of words separated by comma.
     * 
     * @param key The properties to look for.
     * @param dft An array of string to to be returned if the property's value is null.
     * @return The value of the specified property as an array of string.
     */
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

    /**
     * Assigns the specified property to a value.
     * 
     * @param key The property to be changed.
     * @param val An array representation of the value.
     */
    public static void setStringArray(String key, String val[])
    {
        getConfigFileProperties().setStringArray(key, val);
    }

    // ------------------------------------------------------------------------

    /**
     * Returns value of the specified property if its a file.
     * Creates a new file if the value is not of type file
     * Returns null otherwise if value is null.
     * 
     * @param key The property to look for
     * @return The value of the specified property if its a file.
     */
    public static File getFile(String key)
    {
        return getFile(key, null);
    }

    // do not include this method, otherwise "getFile(file, null)" would be ambiguous
    //public File getFile(String key, String dft)
    /**
     * Returns value of the specified property if its a file.
     * Creates a new file if the value is not of type file.
     * Returns dft otherwise if value is null.
     * 
     * @param key The property to look for
     * @param dft The file to return if value is null.
     * @return The value of the specified property if its a file.
     */
    public static File getFile(String key, File dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getFile(key, dft) : dft;
    }

    /**
     * Returns value of the specified properties if its a file.
     * Creates a new file if the value is not of type file.
     * Returns dft otherwise if value is null.
     * 
     * @param key The properties to look for
     * @param dft The file to return if value is null.
     * @return The value of the specified property if its a file.
     */
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

    /**
     * Sets the value of the specified property.
     * 
     * @param key The property to look for.
     * @param value The new value.
     */
    public static void setFile(String key, File value)
    {
        getConfigFileProperties().setFile(key, value);
    }

    // ------------------------------------------------------------------------
    /**
     * Returns value of the specified property if its of type double.
     * Returns null otherwise.
     * 
     * @param key The property to look for
     * @return The value of the specified property if its of type double.
     */
    public static double getDouble(String key)
    {
        return getDouble(key, 0.0);
    }
    
    /**
     * Returns value of the specified property if its of type double.
     * Returns dft otherwise.
     * 
     * @param key The property to look for
     * @param dft The value of type double to return if value is null.
     * @return The value of the specified property if its of type double.
     */
    public static double getDouble(String key, double dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getDouble(key, dft) : dft;
    }
    
    /**
     * Returns value of the specified properties if its of type double.
     * Returns dft otherwise.
     * 
     * @param key The property to look for
     * @param dft The value of type double to return if value is null.
     * @return The value of the specified property if its of type double.
     */    
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

    /**
     * Sets the value of the specified property to a double number.
     * 
     * @param key The property to look for.
     * @param value The new value.
     */
    public static void setDouble(String key, double value)
    {
        getConfigFileProperties().setDouble(key, value);
    }

    // ------------------------------------------------------------------------
    /**
     * Returns value of the specified property if its of type float.
     * Returns null otherwise.
     * 
     * @param key The property to look for
     * @return The value of the specified property if its of type float.
     */   
    public static float getFloat(String key)
    {
        return getFloat(key, 0.0F);
    }

    /**
     * Returns value of the specified property if its of type float.
     * Returns dft otherwise.
     * 
     * @param key The property to look for
     * @param dft The value of type float to return if value is null.
     * @return The value of the specified property if its of type float.
     */
    public static float getFloat(String key, float dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getFloat(key, dft) : dft;
    }

    /**
     * Returns value of the specified properties if its of type float.
     * Returns dft otherwise.
     * 
     * @param key The properties to look for
     * @param dft The value of type float to return if value is null.
     * @return The value of the specified property if its of type float.
     */
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

    /**
     * Sets the value of the specified property to a float number.
     * 
     * @param key The property to look for.
     * @param value The new value.
     */
    public static void setFloat(String key, float value)
    {
        getConfigFileProperties().setFloat(key, value);
    }

    // ------------------------------------------------------------------------
    /**
     * Returns value of the specified property if its of type long.
     * Returns null otherwise.
     * 
     * @param key The property to look for
     * @return The value of the specified property if its of type long.
     */
    public static long getLong(String key)
    {
        return getLong(key, 0L);
    }

    /**
     * Returns value of the specified property if its of type long.
     * Returns dft otherwise.
     * 
     * @param key The property to look for
     * @param dft The value of type long to return if value is null.
     * @return The value of the specified property if its of type long.
     */
    public static long getLong(String key, long dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getLong(key, dft) : dft;
    }

    /**
     * Returns value of the specified properties if its of type long.
     * Returns dft otherwise.
     * 
     * @param key The properties to look for
     * @param dft The value of type long to return if value is null.
     * @return The value of the specified property if its of type long.
     */
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

    /**
     * Sets the value of the specified property to a long number.
     * 
     * @param key The property to look for.
     * @param value The new value.
     */
    public static void setLong(String key, long value)
    {
        getConfigFileProperties().setLong(key, value);
    }

    // ------------------------------------------------------------------------
    /**
     * Returns value of the specified property if its of type int.
     * Returns null otherwise.
     * 
     * @param key The property to look for
     * @return The value of the specified property if its of type int.
     */
    public static int getInt(String key)
    {
        return getInt(key, 0);
    }

    /**
     * Returns value of the specified property if its of type int.
     * Returns dft otherwise.
     * 
     * @param key The property to look for
     * @param dft The value of type int to return if value is null.
     * @return The value of the specified property if its of type int.
     */
    public static int getInt(String key, int dft)
    {
        RTProperties rtp = getPropertiesForKey(key);
        return (rtp != null)? rtp.getInt(key, dft) : dft;
    }

    /**
     * Returns value of the specified properties if its of type int.
     * Returns dft otherwise.
     * 
     * @param key The properties to look for
     * @param dft The value of type int to return if value is null.
     * @return The value of the specified property if its of type int.
     */
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

    /**
     * Sets the value of the specified property to a int number.
     * 
     * @param key The property to look for.
     * @param value The new value.
     */
    public static void setInt(String key, int value)
    {
        getConfigFileProperties().setInt(key, value);
    }

    // ------------------------------------------------------------------------
    /**
     * Returns value of the specified property if its of type boolean.
     * Returns null otherwise.
     * 
     * @param key The property to look for
     * @return The value of the specified property if its of type boolean.
     */
    public static boolean getBoolean(String key)
    {
        return getBoolean(key, hasProperty(key));
    }

    /**
     * Returns value of the specified property if its of type boolean.
     * Returns dft otherwise.
     * 
     * @param key The property to look for
     * @param dft The value of type boolean to return if value is null.
     * @return The value of the specified property if its of type boolean.
     */ 
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

    /**
     * Returns value of the specified properties if its of type boolean.
     * Returns dft otherwise.
     * 
     * @param key The properties to look for
     * @param dft The value of type boolean to return if value is null.
     * @return The value of the specified property if its of type boolean.
     */ 
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
    
    /**
     * Sets the value of the specified property to a boolean number.
     * 
     * @param key The property to look for.
     * @param value The new value.
     */
    public static void setBoolean(String key, boolean value)
    {
        getConfigFileProperties().setBoolean(key, value);
    }

    // ------------------------------------------------------------------------

    /**
     * Sets admin mode property.
     * 
     * @param admin The value for admin mode property.
     */
    public static void setAdminMode(boolean admin)
    {
        setBoolean(RTKey.ADMIN_MODE, admin);
    }
    
    /**
     * Indicates whether admin mode is on or not.
     * 
     * @return True if admin mode is on, False otherwise.
     */
    public static boolean isAdminMode()
    {
        return getBoolean(RTKey.ADMIN_MODE);
    }

    // ------------------------------------------------------------------------
    /**
     * Sets debug mode property.
     * 
     * @param debug The value for debug mode property.
     */
    public static void setDebugMode(boolean debug)
    {
        setBoolean(RTKey.DEBUG_MODE, debug);
    }
    
    //private static int _debug_recursion = 0;
    /**
     * Indicates whether debug mode is on or not.
     * 
     * @return True if debug mode is on, False otherwise.
     */
    public static boolean isDebugMode()
    {
        //if (_debug_recursion > 0) { Thread.dumpStack(); System.exit(0); }
        //try { _debug_recursion++;
        return !isInitialized() || getBoolean(RTKey.DEBUG_MODE);
        //} finally { _debug_recursion--; }
    }

    // ------------------------------------------------------------------------
    /**
     * Sets test mode property.
     * 
     * @param test The value for test mode property.
     */
    public static void setTestMode(boolean test)
    {
        setBoolean(RTKey.TEST_MODE, test);
    }
    
    /**
     * Indicates whether test mode is on or not.
     * 
     * @return True if test mode is on, False otherwise.
     */
    public static boolean isTestMode()
    {
        return getBoolean(RTKey.TEST_MODE);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
        
    /**
     * Indicates whether the server is running as a web application or not.
     */
    private static Boolean isRunningAsWebApp = null;

    /**
     * Sets the value of "isWebApp" property.
     * 
     * @param webapp The value for "isWebApp" property.
     */
    public static void setWebApp(boolean webapp)
    {
        setBoolean(RTKey.IS_WEBAPP, webapp);
        isRunningAsWebApp = null; // <== to bypass Boolean check
    }
    
    /**
     * Indicates whether it is a web application or not.
     * 
     * @return True it is a web application, False otherwise.
     */
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

    /**
     * Holds the list of Web Application names.
     */
    private static String WebAppClassNames[] = {
        "javax.servlet.http.HttpServlet", // as long as the servlet didn't override 'service'
        "org.apache.catalina.core.ApplicationFilterChain"
    };
    
    /**
     * Indicates whether it is a web application or not.
     * 
     * @return True it is a web application, False otherwise.
     */
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
    
    /**
     * Indicates whether it is a web application or not.
     * 
     * @return True it is a web application, False otherwise.
     */
    protected static boolean _isWebApp_2()
    {
        return (getServletClass() != null);
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    /**
     * Defines the main class.
     */
    private static Class Main_class = null;
    
    /**
     * Returns the value of the main class.
     * @return Value of the main class.
     */
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
    /**
     * Holds the name of servlet class.
     */
    private static String SERVLET_CLASS = "javax.servlet.Servlet"; // GenericServlet
    /**
     * Indicates whether servlet is initialized or not.
     */
    private static boolean Servlet_init = false;
    /**
     * Holds the class name of servlet_class.
     */
    private static Class Servlet_class = null;
    /**
     * Returns the class name of servlet_class.
     * 
     * @return The class name of servlet_class.
     */
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

    /**
     * Runs operating system tests from the commandline and prints results.
     * 
     * @param argv Command line arguments.
     */
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
