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
//  Runtime property keys
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/04/23  Martin D. Flynn
//      Modified/Cleaned-up keys
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.awt.*;
import java.awt.color.*;

/**
 * Holds the key names for run time properties.
 * 
 * @author Martin D. Flynn
 * @author Nam Nguyen
 */
public class RTKey
{

    // ------------------------------------------------------------------------
    /**
     * Represents null.
     */
    public  static final String NULL_VALUE                  = "<null>";
    
    // ------------------------------------------------------------------------
    
    /**
     * Uses to avoid having to hardcode the fully qualified class name.
     * 
     * @return The class name.
     */
    private static String SENDMAIL_CLASS()
    {
        // This is done this way to avoid having to hardcode the fully qualified class name.
        return RTKey.class.getPackage().getName() + ".Send" + "Mail";
    }

    // ------------------------------------------------------------------------
    // property keys
    /**
     * Holds "rtquiet".
     */
    public static final String RT_QUIET                     = "rtquiet"; // command-line use only
    /**
     * Holds "$main.class".
     */
    public static final String MAIN_CLASS                   = "$main.class"; // set by 'RTConfig.setCommandLineArgs'
    /**
     * Holds "isWebApp".
     */
    public static final String IS_WEBAPP                    = "isWebApp";
    /**
     * Holds "configFileDir".
     */
    public static final String CONFIG_FILE_DIR              = "configFileDir";
    /**
     * Holds "configFile".
     */
    public static final String CONFIG_FILE                  = "configFile";
    /**
     * Holds "configFile".
     */
    public static final String COMMAND_LINE_CONF            = "conf"; // alias for CONFIG_FILE for cmdLine use
    /**
     * Holds "testMode".
     */
    public static final String TEST_MODE                    = "testMode";
    /**
     * Holds "debugMode".
     */
    public static final String DEBUG_MODE                   = "debugMode";
    /**
     * Holds "adminMode".
     */
    public static final String ADMIN_MODE                   = "adminMode";
    /**
     * Holds "http.proxy.host".
     */
    public static final String HTTP_PROXY_HOST              = "http.proxy.host";
    /**
     * Holds "http.proxy.port".
     */
    public static final String HTTP_PROXY_PORT              = "http.proxy.port";
    /**
     * Holds "url.connect.timeout".
     */
    public static final String URL_CONNECT_TIMEOUT          = "url.connect.timeout";
    /**
     * Holds "url.read.timeout".
     */
    public static final String URL_READ_TIMEOUT             = "url.read.timeout";
    /**
     * Holds "smtp.host".
     */
    public static final String SMTP_SERVER_HOST             = "smtp.host";
    /**
     * Holds "smtp.threadModel".
     */
    public static final String SMTP_THREAD_MODEL            = "smtp.threadModel";
    /**
     * Holds "smtp.threadModel.show".
     */
    public static final String SMTP_THREAD_MODEL_SHOW       = "smtp.threadModel.show";
    /**
     * Holds "smtp.debug".
     */
    public static final String SMTP_DEBUG                   = "smtp.debug";
    /**
     * Holds "log.level".
     */
    public static final String LOG_LEVEL                    = "log.level";
    /**
     * Holds "log.level.header".
     */
    public static final String LOG_LEVEL_HEADER             = "log.level.header";
    /**
     * Holds "log.file".
     */
    public static final String LOG_FILE                     = "log.file";
    /**
     * Holds "log.file.maxSize".
     */
    public static final String LOG_FILE_MAX_SIZE            = "log.file.maxSize";
    /**
     * Holds "log.include.date".
     */
    public static final String LOG_INCL_DATE                = "log.include.date";
    /**
     * Holds "log.include.frame".
     */
    public static final String LOG_INCL_STACKFRAME          = "log.include.frame";
    /**
     * Holds "log.email.sendExceptions".
     */
    public static final String LOG_EMAIL_EXCEPTIONS         = "log.email.sendExceptions";
    /**
     * Holds "log.email.fromAddr".
     */
    public static final String LOG_EMAIL_FROM               = "log.email.fromAddr";
    /**
     * Holds "log.email.toAddr".
     */
    public static final String LOG_EMAIL_TO                 = "log.email.toAddr";
    /**
     * Holds "log.email.sendmailClass".
     */
    public static final String LOG_SENDMAIL_CLASS           = "log.email.sendmailClass";
    /**
     * Holds "db.sql.dbname".
     */    
    public static final String DB_NAME                      = "db.sql.dbname";
    /**
     * Holds "db.sql.host".
     */
    public static final String DB_HOST                      = "db.sql.host";
    /**
     * Holds "db.sql.port".
     */
    public static final String DB_PORT                      = "db.sql.port";
    /**
     * Holds "db.sql.user".
     */
    public static final String DB_USER                      = "db.sql.user";
    /**
     * Holds "db.sql.pass".
     */
    public static final String DB_PASS                      = "db.sql.pass";
    /**
     * Holds "db.notify.port".
     */
    public static final String DB_NOTIFY_PORT               = "db.notify.port";
    /**
     * Holds "db.tableLocking".
     */
    public static final String DB_TABLE_LOCKING             = "db.tableLocking";
    /**
     * Holds "db.showSQL".
     */
    public static final String DB_SHOW_SQL                  = "db.showSQL";
    /**
     * Holds "dmtp.port".
     */
    public static final String DMTP_PORT                    = "dmtp.port";
    /**
     * Holds "webapp.contextName".
     */
    public static final String WEBAPP_CONTEXT_NAME          = "webapp.contextName";
    /**
     * Holds "webapp.contextPath".
     */
    public static final String WEBAPP_CONTEXT_PATH          = "webapp.contextPath";

    // ------------------------------------------------------------------------

    /**
     * Represents a null entry.
     */
    protected static Entry NullEntry = new Entry("", null);
    
    /**
     * Holds all the standart entries initialized with values.
     */
    protected static Entry runtimeKeys[] = {
        
        new Entry("General mode attributes"),
        new Entry(IS_WEBAPP                  , false                            , "true, if running as a webapp"),              // WEB
        new Entry(ADMIN_MODE                 , false                            , "Admin mode enabled"),                        // APP
        new Entry(DEBUG_MODE                 , false                            , "Debug mode enabled"),                        // APP|WEB
        new Entry(TEST_MODE                  , false                            , "Test mode enabled"),                         // APP

        new Entry("Runtime config file attributes"),
        new Entry(CONFIG_FILE_DIR            , "/conf"                          , "Runtime config file directory"),             // APP|WEB
        new Entry(CONFIG_FILE                , "./default.conf"                 , "Default runtime config file"),               // APP|WEB

        new Entry("HTTP/URL attributes"),
        new Entry(HTTP_PROXY_HOST            , null                             , "HTTP proxy host"),                           // APP
        new Entry(HTTP_PROXY_PORT            , -1                               , "HTTP proxy port"),                           // APP
        new Entry(URL_CONNECT_TIMEOUT        , 60000L                           , "URL connection timeout (msec)"),             // APP
        new Entry(URL_READ_TIMEOUT           , 60000L                           , "URL read timeout (msec)"),                   // APP

        new Entry("SMTP (mail) attributes"),
        new Entry(SMTP_SERVER_HOST           , "smtp.internet.com"              , "SMTP server host"),                          // APP|WEB
        new Entry(SMTP_THREAD_MODEL          , null                             , "Send-Mail thread model"),
        new Entry(SMTP_THREAD_MODEL_SHOW     , false                            , "Print/show Send-Mail thread model"),
        new Entry(SMTP_DEBUG                 , false                            , "Sendmail debug mode"),

        new Entry("'Print' util attributes"),
        new Entry(LOG_LEVEL                  , Print.LOG_ALL                    , "log level"),                                 // APP|WEB
        new Entry(LOG_LEVEL_HEADER           , Print.LOG_ALL                    , "log header level"),                          // APP|WEB
        new Entry(LOG_FILE                   , null                             , "logfile name"),                              // APP|WEB
        new Entry(LOG_FILE_MAX_SIZE          , 50000L                           , "max logfile size"),                          // APP|WEB
        new Entry(LOG_INCL_DATE              , false                            , "include date in logs"),                      // APP|WEB
        new Entry(LOG_INCL_STACKFRAME        , false                            , "include stackframe in logs"),                // APP|WEB
        new Entry(LOG_EMAIL_EXCEPTIONS       , false                            , "EMail exceptions"),                          // APP|WEB
        new Entry(LOG_EMAIL_FROM             , null                             , "Error email sender"),
        new Entry(LOG_EMAIL_TO               , null                             , "Error email recipient"),
        new Entry(LOG_SENDMAIL_CLASS         , SENDMAIL_CLASS()                 , "Sendmail class name"),                       // APP|WEB

        new Entry("DB attributes"),
        new Entry(DB_NAME                    , "?"                              , "Database name"),                             // APP
        new Entry(DB_HOST                    , "127.0.0.1" /*"localhost"*/      , "MySQL server host"),                         // APP|WEB
        new Entry(DB_PORT                    , 3306                             , "MySQL server port"),                         // APP|WEB
        new Entry(DB_USER                    , "userroot"                       , "MySQL server user"),                         // APP|WEB
        new Entry(DB_PASS                    , "passwd"                         , "MySQL server password"),                     // APP|WEB
        new Entry(DB_NOTIFY_PORT             , 9510                             , "Alarm notification port"),                   // WEB
        new Entry(DB_TABLE_LOCKING           , false                            , "Table locking enabled"),                     // APP|WEB
        new Entry(DB_SHOW_SQL                , false                            , "Show insert/update SQL"),                    // APP|WEB

        new Entry("Server attributes"),
        new Entry(DMTP_PORT                  , 31000                            , "DMTP service port (on this host)"),          // APP|WEB

        new Entry("WebApp context attributes"),
        new Entry(WEBAPP_CONTEXT_NAME        , null                             , "WebApp context name"),                       // WEB
        new Entry(WEBAPP_CONTEXT_PATH        , null                             , "WebApp context path"),                       // WEB

    };
    
    // ------------------------------------------------------------------------

    /**
     * Holds the global entry map.
     */
    protected static Map            globalEntryMap = null;
    /**
     * Holds the global properties.
     */
    protected static RTProperties   globalProperties = null;

    /**
     * Returns the map consisting of runtime entries.
     * 
     * @return the map consisting of runtime entries.
     */
    protected static Map getRuntimeEntryMap()
    {
        if (globalEntryMap == null) {
            /* create map */
            globalEntryMap = new OrderedMap();
            
            /* load default key entries */
            for (int i = 0; i < RTKey.runtimeKeys.length; i++) {
                String rtKey = RTKey.runtimeKeys[i].getKey();
                if (rtKey != null) {
                    globalEntryMap.put(rtKey, RTKey.runtimeKeys[i]);
                }
            }
            
        }
        return globalEntryMap;
    }
    
    /**
     * Adds entries to the runtime entries list.
     * 
     * @param dftEntry the entries to be added.
     */
    public void addRuntimeEntries(Entry dftEntry[])
    {
        if (dftEntry != null) {
            for (int i = 0; i < dftEntry.length; i++) {
                addRuntimeEntry(dftEntry[i]);
            }
        }
    }
    
    /**
     * Adds an entry to the runtime entries list.
     * 
     * @param dftEntry the entry to be added.
     */
    public void addRuntimeEntry(Entry dftEntry)
    {
        if (dftEntry != null) {
            String rtKey = dftEntry.getKey();
            if (rtKey != null) {
                RTKey.getRuntimeEntryMap().put(rtKey, dftEntry);
                globalProperties = null;
            }
        }
    }
    
    // ------------------------------------------------------------------------
    /**
     * Returns the runtime entry with the specified key.
     * 
     * @param key Specifies the entry to look for.
     * @return the runtime entry with the specified key.
     */
    protected static Entry getRuntimeEntry(String key)
    {
        return (key != null)? (Entry)RTKey.getRuntimeEntryMap().get(key) : null;
    }
    
    // ------------------------------------------------------------------------

    /**
     * Checks to see whether an entry has default value.
     * 
     * @param key Specifies the entry to look for.
     * @return True if an entry has default value, False otherwise.
     */
    public static boolean hasDefault(String key)
    {
        return (RTKey.getRuntimeEntry(key) != null);
    }
    
    /**
     * Returns a specified entry, returns dft if default value is null.
     * 
     * @param key Specifies the entry to look for.
     * @param dft The value to return in the case when default value is null.
     * @return the default value of an entry, returns dft if default value is null.
     */
    public static Object getDefaultProperty(String key, Object dft)
    {
        Entry rtKey = RTKey.getRuntimeEntry(key);
        return (rtKey != null)? rtKey.getDefault() : dft;
    }
    
    /**
     * Returns all default entries, returns dft if default value is null.
     * 
     * @return set of all default entries.
     */
    public static RTProperties getDefaultProperties()
    {
        if (globalProperties == null) {
            RTProperties rtp = new RTProperties();
            for (Iterator v = RTKey.getRuntimeEntryMap().values().iterator(); v.hasNext();) {
                Entry rtk = (Entry)v.next();
                if (!rtk.isHelp()) {
                    String key = rtk.getKey();
                    Object val = rtk.getDefault();
                    rtp.setProperty(key, val);
                }
            }
            globalProperties = rtp;
        }
        return globalProperties;
    }

    // ------------------------------------------------------------------------
    
    /**
     * A property entry consisting of property name, value and description.
     */
    public static class Entry
    {
        
        private String key  = null;
        private Object dft  = null;
        private String hlp  = null;
        private int    ref  = 0;    // cyclical reference test
        
        /**
         * Creates a new entry with specified name, value and description.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         * @param help Description of the new entry.
         */
        public Entry(String key, Object dft, String help) {
            this.key = key;
            this.dft = dft;
            this.hlp = help;
        }
        
        /**
         * Creates a new entry with specified name and value.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         */
        public Entry(String key, Object dft) {
            this(key, dft, null);
        }
        
        /**
         * Creates a new entry with just a description.
         * 
         * @param help Description of the new entry.
         */
        public Entry(String help) {
            this(null, null, help);
        }
        
        /**
         * Creates a new entry with specified name, value of type int and description.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         * @param help Description of the new entry.
         */
        public Entry(String key, int dft, String help) {
            this(key, new Integer(dft), help);
        }
        
        /**
         * Creates a new entry with specified name and value of type int.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         */
        public Entry(String key, int dft) {
            this(key, dft, null);
        }
        
        /**
         * Creates a new entry with specified name, value of type long and description.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         * @param help Description of the new entry.
         */        
        public Entry(String key, long dft, String help) {
            this(key, new Long(dft), help);
        }
        
        /**
         * Creates a new entry with specified name and value of type long.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         */
        public Entry(String key, long dft) {
            this(key, dft, null);
        }
        
        /**
         * Creates a new entry with specified name, value of type double and description.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         * @param help Description of the new entry.
         */  
        public Entry(String key, double dft, String help) {
            this(key, new Double(dft), help);
        }
        
        /**
         * Creates a new entry with specified name and value of type double.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         */
        public Entry(String key, double dft) {
            this(key, dft, null);
        }
        
        /**
         * Creates a new entry with specified name, value of type float and description.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         * @param help Description of the new entry.
         */  
        public Entry(String key, float dft, String help) {
            this(key, new Float(dft), help);
        }
        
        /**
         * Creates a new entry with specified name and value of type float.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         */
        public Entry(String key, float dft) {
            this(key, dft, null);
        }
        
        /**
         * Creates a new entry with specified name, value of type boolean and description.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         * @param help Description of the new entry.
         */          
        public Entry(String key, boolean dft, String help) {
            this(key, new Boolean(dft), help);
        }
        
        /**
         * Creates a new entry with specified name and value of type boolean.
         * 
         * @param key Name of the new entry.
         * @param dft Value of the new entry.
         */
        public Entry(String key, boolean dft) {
            this(key, dft, null);
        }
        
        /**
         * Returns the Entry if its a real entry, not a reference.
         * 
         * @return Entry if its a real entry, not a reference.
         */
        public Entry getRealEntry() {
            if (this.dft instanceof EntryReference) {
                Entry entry = null;
                if (this.ref > 0) {
                    Print.logStackTrace("Cyclical EntryReference: " + this.getKey());
                    entry = NullEntry;
                } else {
                    this.ref++;
                    try {
                        EntryReference entryRef = (EntryReference)this.dft;
                        Entry nextEntry = entryRef.getReferencedEntry(); // <-- will display error, if not found
                        entry = (nextEntry != null)? nextEntry.getRealEntry() : NullEntry;
                    } finally {
                        this.ref--;
                    }
                }
                return entry;
            } else {
                return this;
            }
        }
        
        /**
         * Indicates whether this is a reference or a real entry.
         * 
         * @return True if it is a reference, False otherwise.
         */
        public boolean isReference() {
            return (this.dft instanceof EntryReference);
        }
        
        /**
         * Returns the name of the property entry.
         * 
         * @return name of the property entry.
         */
        public String getKey() {
            return this.key;
        }
        
        /**
         * Returns the value of the property entry.
         * 
         * @return value of the property entry.
         */
        public Object getDefault() {
            return this.isReference()? this.getRealEntry().getDefault() : this.dft;
        }
        
        /**
         * Indicates whether this entry has description text or not.
         * Bug: should be this.help instead of this.key.
         * 
         * @return True if this entry has description, False otherwise.
         */
        public boolean isHelp() {
            return (this.key == null);
        }
        
        /**
         * Returns the description of the property entry.
         * 
         * @return description of the property entry.
         */
        public String getHelp() {
            return (this.hlp != null)? this.hlp : "";
        }
        
        /**
         * Returns a string consisting of entry's description(if there is) and name with value being v.
         * 
         * @param v The value to be printed out.
         * @return string consisting of entry's description(if there is) and name with value being v.
         */
        public String toString(Object v) {
            StringBuffer sb = new StringBuffer();
            if (this.isHelp()) {
                sb.append("# --- ").append(this.getHelp());
            } else {
                sb.append(this.getKey()).append("=");
                sb.append((v != null)? v : NULL_VALUE);
            }
            return sb.toString();
        }
        
        /**
         * Returns a string representation of the entry.
         * 
         * @return a string representation of the entry
         */
        public String toString() {
            return this.toString(this.getDefault());
        }
        
    }
    
    /**
     * Provides descriptions for a reference entry.
     * 
     * @author Martin D. Flynn
     * @author Nam
     */
    public static class EntryReference
    {
        /**
         * Points to the actual entry.
         */
        private String refKey = null;
        /**
         * Reference this entry to a specified entry.
         * 
         * @param key The entry to reference to.
         */
        public EntryReference(String key) {
            this.refKey = key;
        }
        
        /**
         * Returns the reference key of this entry.
         * 
         * @return the reference key of this entry.
         */
        public String getKey() {
            return this.refKey;
        }
        
        /**
         * Returns the referenced entry.
         * 
         * @return the referenced entry.
         */
        public Entry getReferencedEntry() {
            Entry entry = getRuntimeEntry(this.getKey());
            if (entry == null) { 
                Print.logStackTrace("Entry reference not found: " + this.getKey()); 
            }
            return entry;
        }
        
        /**
         * Converts the entry to string for readability.
         * 
         * @return A string representation of the entry.
         */
        public String toString() {
            String k = this.getKey();
            return (k != null)? k : "";
        }
        
        /**
         * Provides a way to compare with other entries.
         * 
         * @param other The object to be compared to.
         * @return True if the entries are equal, False otherwise.
         */
        public boolean equals(Object other) {
            if (other instanceof EntryReference) {
                return this.toString().equals(other.toString());
            } else {
                return false;
            }
        }
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Prints out the standart runtime entries.
     * 
     * @param out The output stream to be printed to.
     */
    public static void printDefaults(PrintStream out)
    {
        
        /* print standard runtime entries */
        Set keyList = new OrderedSet();
        String keyGrp = null;
        
        for (Iterator v = RTKey.getRuntimeEntryMap().values().iterator(); v.hasNext();) {
            Entry rtk = (Entry)v.next();
            if (rtk.isHelp()) { 
                out.println(""); 
                out.println("# ===== " + rtk.getHelp());
            } else {
                Object dft = rtk.getDefault();
                out.println("# --- " + rtk.getHelp());
                out.println("# " + rtk.toString(dft));
                String key = rtk.getKey();
                keyList.add(key);
                if (!key.equals(CONFIG_FILE) && RTConfig.hasProperty(key)) {
                    String val = RTConfig.getString(key, null);
                    //if ((val != null) && ((dft == null) || !val.equals(dft.toString()))) {
                        out.println(rtk.toString(val));
                    //}
                }
            }
        }
        
        /* orphaned entries */
        RTProperties cmdLineProps = RTConfig.getConfigFileProperties();
        if (cmdLineProps != null) {
            boolean orphanHeader = false;
            for (Iterator i = cmdLineProps.keyIterator(); i.hasNext();) {
                Object k = i.next();
                if (!k.equals(COMMAND_LINE_CONF) && !keyList.contains(k)) {
                    if (!orphanHeader) {
                        out.println(""); 
                        out.println("# ===== Other entries");
                        orphanHeader = true;
                    }
                    Object v = cmdLineProps.getProperty(k, null);
                    out.println(k + "=" + ((v != null)? v : NULL_VALUE)); 
                }
            }
        }
        
        /* final blank line */
        out.println("");
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Runs from the command line.
     * 
     * @param argv Command line arguments.
     */
    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        printDefaults(System.out);
    }
    
    // ------------------------------------------------------------------------

}