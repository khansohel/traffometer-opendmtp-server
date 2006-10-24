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
//  General Printing/Logging utilities.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/04/23  Martin D. Flynn
//      Updated to support a more granular message logging.  Eventually, this
//      should be modified to support Log4J.
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

public class Print
{

    // ------------------------------------------------------------------------

    public  static final int    LOG_OFF         = 0;
    public  static final int    LOG_FATAL       = 1;
    public  static final int    LOG_ERROR       = 2;
    public  static final int    LOG_WARN        = 3;
    public  static final int    LOG_INFO        = 4;
    public  static final int    LOG_DEBUG       = 5;
    public  static final int    LOG_ALL         = 6;

    // ------------------------------------------------------------------------

    private static final String _JAVA = ".java";
    
    // ------------------------------------------------------------------------

    private static PrintStream stdout    = null;
    private static PrintStream stderr    = null;
    private static PrintStream sysStdout = null;

    // ------------------------------------------------------------------------

    private static String localhostName = null;

    public static String getHostName()
    {
        /* host name */
        if (Print.localhostName == null) {
            try {
                String hd = InetAddress.getLocalHost().getHostName();
                int p = hd.indexOf(".");
                Print.localhostName = (p >= 0)? hd.substring(0,p) : hd;
            } catch (UnknownHostException uhe) {
                Print.localhostName = "UNKNOWN";
            }
        }
        return Print.localhostName;
    }

    // ------------------------------------------------------------------------

    public static void setStdout(PrintStream out)
    {
        Print.stdout = out;
    }

    public static void setStderr(PrintStream err)
    {
        Print.stderr = err;
    }
    
    public static PrintStream getStdout()
    {
        return (Print.stdout != null)? Print.stdout : System.out;
    }
    
    public static PrintStream getStderr()
    {
        return (Print.stdout != null)? Print.stderr : System.err;
    }
    
    // ------------------------------------------------------------------------

    protected static String _getStackFrame(int frame)
    {
        
        /* extract stack frame */
        Throwable t = new Throwable();
        t.fillInStackTrace();
        StackTraceElement st[] = t.getStackTrace();
        StackTraceElement sf = (st != null)? st[frame + 1] : null;
        
        /* no stack frame? */
        if (sf == null) {
            return "?";
        }
        
        /* get file */
        String clazz = sf.getClassName();
        String file  = sf.getFileName();
        if (file == null) {
            // Java code was compiled with 'debug=false'
            int p = 0;
            for (; (p < clazz.length()) && !Character.isUpperCase(clazz.charAt(p)); p++);
            if (p < clazz.length()) { clazz = clazz.substring(p); }
        } else
        if (file.toLowerCase().endsWith(_JAVA)) { 
            file = file.substring(0, file.length() - _JAVA.length()); 
            int p = clazz.indexOf(file);
            if (p >= 0) { clazz = clazz.substring(p); }
        }
        
        /* format frame description */
        StringBuffer sb = new StringBuffer();
        sb.append(clazz);
        sb.append(".").append(sf.getMethodName());
        sb.append(":").append(sf.getLineNumber());
        
        return sb.toString();
    }
    
    protected static boolean _includeStackFrame()
    {
        return RTConfig.getBoolean(RTKey.LOG_INCL_STACKFRAME,false) ||
            (Print.getLogLevel() >= Print.LOG_DEBUG);
    }
    
    protected static boolean _includeDate()
    {
        return RTConfig.getBoolean(RTKey.LOG_INCL_DATE,false);
    }

    // ------------------------------------------------------------------------

    public static void _println(PrintStream ps, String msg)
    {
        // Does not use RTConfig
        Print._print(ps, 1, true, msg + "\n");
    }
    
    protected static void _println(PrintStream ps, int frame, boolean printFrame, String msg)
    {
        Print._print(ps, frame + 1, printFrame, msg + "\n");
    }
    
    protected static void _println(PrintStream ps, int frame, String msg)
    {
        Print._print(ps, frame + 1, _includeStackFrame(), msg + "\n");
    }

    protected static void _print(PrintStream ps, int frame, String msg)
    {
        Print._print(ps, frame + 1, _includeStackFrame(), msg);
    }
    
    protected static void _print(PrintStream ps, int frame, boolean printFrame, String msg)
    {
        // - use of RTConfig is NOT allowed in this method!
        // - if not writing to 'Print.stdout', then we really want to open/close this file
        
        /* Print stream */
        PrintStream out = (ps != null)? ps : Print.getStdout();
        
        /* write */
        if (out != null) {
            StringBuffer sb = new StringBuffer();
            if (printFrame) {
                sb.append("[");
                sb.append(_getStackFrame(frame + 1));
                sb.append("] ");
            }
            sb.append(msg);
            out.print(sb.toString());
            out.flush();
        }
        
    }
    
    // ------------------------------------------------------------------------

    public static void setSysStdout(PrintStream out)
    {
        Print.sysStdout = out;
    }

    public static PrintStream getSysStdout()
    {
        return (Print.sysStdout != null)? Print.sysStdout : Print.getStdout();
    }
    
    public static void sysPrint(String msg)
    {
        PrintStream out = Print.getSysStdout();
        Print._print(out, 1, false, msg);
    }

    public static void sysPrint(StringBuffer msg)
    {
        PrintStream out = Print.getSysStdout();
        Print._print(out, 1, false, msg.toString());
    }

    public static void sysPrintln(String msg)
    {
        PrintStream out = Print.getSysStdout();
        Print._println(out, 1, false, msg);
    }

    public static void sysPrintln(StringBuffer msg)
    {
        PrintStream out = null;
        Print._println(out, 1, false, msg.toString());
    }
    
    // ------------------------------------------------------------------------

    public static void print(String msg)
    {
        Print._print(null, 1, false, msg);
    }

    public static void print(StringBuffer msg)
    {
        Print._print(null, 1, false, msg.toString());
    }

    public static void println(String msg)
    {
        Print._println(null, 1, false, msg);
    }

    public static void println(StringBuffer msg)
    {
        Print._println(null, 1, false, msg.toString());
    }

    // ------------------------------------------------------------------------
    
    protected static void _logStackTrace(int frame, String msg, Throwable t)
    {

        /* log stack trace */
        try {
            PrintStream out = Print.openLogFile();
            _printStackTrace(out, frame + 1, msg, t);
        } catch (Throwable loge) {
            _printStackTrace(null, frame + 1, msg, t);
        } finally {
            Print.closeLogFile();
        }
        
        /* email */
        if (RTConfig.getBoolean(RTKey.LOG_EMAIL_EXCEPTIONS)) {
            Print.sysPrintln("EMailing error...");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(bos);
            String host = Print.getHostName();
            
            /* include hostname */
            out.println("From host: " + host);
            
            /* include stacktrace */
            _printStackTrace(out, frame + 1, msg, t);
            
            /* close and send email */
            out.close(); // may not be necessary
            Print.emailError("[" + host + "] " + msg, bos.toString());
        }
    }
    
    protected static void _printStackTrace(PrintStream out, int frame, String msg, Throwable t)
    {
        
        /* get default print stream */
        if (out == null) {
            /* first try default stdout */
            out = Print.getStdout(); 
            if (out == null) {
                // failing that, set to stderr
                out = System.err;
            }
        }
        
        /* print stack trace */
        Print._println(out, frame + 1, false, msg);
        if (t == null) {
            t = new Throwable();
            t.fillInStackTrace();
            StackTraceElement oldst[] = t.getStackTrace();
            StackTraceElement newst[] = new StackTraceElement[oldst.length - (frame + 1)];
            System.arraycopy(oldst, frame + 1, newst, 0, newst.length);
            t.setStackTrace(newst);
        }
        t.printStackTrace(out);
        if (t instanceof SQLException) {
            SQLException sqe = ((SQLException)t).getNextException();
            for (; (sqe != null); sqe = sqe.getNextException()) { 
                sqe.printStackTrace(out); 
            }
        }
        
    }

    // ------------------------------------------------------------------------
   
    public static void logNotImplemented(String msg)
    {
        Print._logStackTrace(1, "Feature Not Implemented: " + msg, null);
    }
   
    public static void logException(String msg, Throwable t)
    {
        Print._logStackTrace(1, "Exception: " + msg, t);
    }
    
    public static void logStackTrace(String msg, Throwable t)
    {
        Print._logStackTrace(1, "Stacktrace: " + msg, t);
    }

    public static void logStackTrace(Throwable t)
    {
        Print._logStackTrace(1, "Stacktrace: ", t);
    }

    public static void logStackTrace(String msg)
    {
        Print._logStackTrace(1, "Stacktrace: " + msg, null);
    }

    // ------------------------------------------------------------------------
    
    public static void logSQLError(int frame, String msg, SQLException sqe)
    {
        PrintStream ps = null;
        Print._log(LOG_ERROR, frame + 1, "==> SQLException: " + msg);
        while (sqe != null) {
            Print._log(LOG_ERROR, frame + 1, "Message:   " + sqe.getMessage());
            Print._log(LOG_ERROR, frame + 1, "SQLState:  " + sqe.getSQLState());
            Print._log(LOG_ERROR, frame + 1, "ErrorCode: " + sqe.getErrorCode());
            //if (sqe.getErrorCode() != DBFactory.SQLERR_DUPLICATE_KEY) {
            Print._printStackTrace(ps, frame + 1, sqe.toString(), sqe);
            //}
            sqe = sqe.getNextException();
        }
    }

    public static void logSQLError(SQLException sqe)
    {
        Print.logSQLError(1, "", sqe);
    }

    public static void logSQLError(String msg, SQLException sqe)
    {
        Print.logSQLError(1, msg, sqe);
    }
    
    // ------------------------------------------------------------------------

    private static Object       logLock         = new Object();
    private static PrintStream  logOutput       = null;
    private static long         logRefCount     = 0L;
    
    protected static PrintStream openLogFile()
    {
        // Do not make calls to "logXXXXXX" from within this method.
        // Calls to 'println' and 'sysPrintln' are ok.
        PrintStream out = null;
        synchronized (Print.logLock) {
            
            /* increment log counter */
            Print.logRefCount++;

            /* get log file */
            if (Print.logOutput != null) {
                
                /* already open */
                out = Print.logOutput;
                
            } else {
            
                /* get/return log file */
                File file = RTConfig.getFile(RTKey.LOG_FILE, null);
                if ((file == null) || file.toString().equals("")) {
                    out = System.err;
                } else
                if (file.isDirectory()) {
                    RTConfig.setFile(RTKey.LOG_FILE,null);
                    Print.println("ERROR: Invalid file specification: " + file);
                    out = System.err;
                } else {
                    if (file.exists()) {
                        long maxLogFileSize = RTConfig.getLong(RTKey.LOG_FILE_MAX_SIZE,0L);
                        if ((maxLogFileSize > 1000L) && (file.length() > maxLogFileSize)) {
                            String bkuName = file.getAbsolutePath() + "." + (new DateTime()).format("yyMMdd",null);
                            File bkuFile = new File(bkuName);
                            for (int i = 1; bkuFile.exists(); i++) { bkuFile = new File(bkuName + "." + i); }
                            file.renameTo(bkuFile);
                        }
                    }
                    try {
                        out = new PrintStream(new FileOutputStream(file,true));
                    } catch (IOException ioe) {
                        RTConfig.setFile(RTKey.LOG_FILE,null);
                        Print.println("ERROR: Unable to open log file: " + file);
                        out = System.err;
                    }
                }
                Print.logOutput = out;
            
            }
        
        }
        return out;
    }
    
    protected static void closeLogFile()
    {
        synchronized (Print.logLock) {
            
            /* decrement log counter */
            Print.logRefCount--;
            if (Print.logRefCount < 0) { Print.logRefCount = 0L; }
            
            /* close */
            if ((Print.logRefCount == 0L) && (Print.logOutput != null)) {
                // don't close if stderr or stdout
                if ((Print.logOutput != System.out) && (Print.logOutput != System.err)) {
                    try {
                        Print.logOutput.close();
                    } catch (Throwable t) {
                        Print.sysPrintln("Unable to close log file: " + t);
                    }
                }
                Print.logOutput = null;
            }
            
        }
    }
    
    public static void setLogLevel(int level)
    {
        RTConfig.setInt(RTKey.LOG_LEVEL, level);
    }

    public static void setLogLevel(int level, boolean inclDate, boolean inclFrame)
    {
        Print.setLogLevel(level);
        RTConfig.setBoolean(RTKey.LOG_INCL_DATE, inclDate);
        RTConfig.setBoolean(RTKey.LOG_INCL_STACKFRAME, inclFrame);
    }

    public static int getLogLevel()
    {
        return Print.parseLogLevel(RTConfig.getString(RTKey.LOG_LEVEL,null));
    }

    public static void setLogHeaderLevel(int level)
    {
        RTConfig.setInt(RTKey.LOG_LEVEL_HEADER, level);
    }

    public static int getLogHeaderLevel()
    {
        return Print.parseLogLevel(RTConfig.getString(RTKey.LOG_LEVEL_HEADER,null));
    }

    public static String getLogLevelString(int level)
    {
        if (level <= 0) { return "OFF"; }
        switch (level) {
            case LOG_FATAL: return "FATAL";
            case LOG_ERROR: return "ERROR";
            case LOG_WARN : return "WARN ";
            case LOG_INFO : return "INFO ";
            case LOG_DEBUG: return "DEBUG";
        }
        return "ALL";
    }
    
    public static int parseLogLevel(String val)
    {
        String v = (val != null)? val.toUpperCase() : null;
        if ((v == null) || v.equals("") || v.startsWith("OFF")) {
            return LOG_OFF;
        } else
        if (v.startsWith("FAT")) {
            return LOG_FATAL;
        } else
        if (v.startsWith("ERR")) {
            return LOG_ERROR;
        } else
        if (v.startsWith("WAR")) {
            return LOG_WARN;
        } else
        if (v.startsWith("INF")) {
            return LOG_INFO;
        } else
        if (v.startsWith("DEB")) {
            return LOG_DEBUG;
        } else
        if (Character.isDigit(v.charAt(0))) {
            int lvl = StringTools.parseInt(v.substring(0,1),LOG_ALL);
            if (lvl < LOG_OFF) {
                return LOG_OFF;
            } else 
            if (lvl > LOG_ALL) {
                return LOG_ALL;
            } else {
                return lvl;
            }
        } else {
            return LOG_ALL;
        }
    }

    // ------------------------------------------------------------------------

    public static void log(int level, String msg)
    {
        Print._log(level, 1, msg);
    }

    public static void logFatal(String msg)
    {
        Print._log(LOG_FATAL, 1, msg);
    }

    public static void logError(String msg)
    {
        Print._log(LOG_ERROR, 1, msg);
    }

    public static void logWarn(String msg)
    {
        Print._log(LOG_WARN, 1, msg);
    }

    public static void logInfo(String msg)
    {
        Print._log(LOG_INFO, 1, msg);
    }

    public static void logDebug(String msg)
    {
        Print._log(LOG_DEBUG, 1, msg);
    }

    protected static void _log(int level, int frame, String msg)
    {

        /* pertinent level? */
        if (level > Print.getLogLevel()) {
            return;
        }

        /* message accumulator */
        StringBuffer logMsg = new StringBuffer();

        /* log message */
        if (level <= Print.getLogHeaderLevel()) {
            // Print this 'header' info for logged messages with a level < 'headerLevel'
            // ie. print header for errors/warnings, but not for info/debug
            logMsg.append("[");
            logMsg.append(Print.getLogLevelString(level));
            if (Print._includeDate()) {
                logMsg.append("|");
                logMsg.append((new DateTime()).format("MM/dd HH:mm:ss", null)); // "yyyy/MM/dd HH:mm:ss"
            }
            if (Print._includeStackFrame()) {
                logMsg.append("|");
                logMsg.append(_getStackFrame(frame + 1));
            }
            logMsg.append("] ");
        }
        
        /* message */
        logMsg.append(msg);
        if (!msg.endsWith("\n")) { logMsg.append("\n"); }
        
        /* print message */
        try {
            PrintStream out = Print.openLogFile();
            if (out != null) { 
                out.write(StringTools.getBytes(logMsg.toString())); 
                out.flush();
            } else {
                Print._print(null, frame + 1, false, logMsg.toString());
            }
        } catch (IOException ioe) {
            RTConfig.setFile(RTKey.LOG_FILE, null);
            Print.logError("Unable to open/write log file: " + ioe);
            Print._print(null, frame + 1, false, logMsg.toString());
        } finally {
            Print.closeLogFile();
        }

    }

    // ------------------------------------------------------------------------

    public static class NullOutputStream
        extends OutputStream
    {
        public NullOutputStream() {}
        public void write(int b) throws IOException {}
        public void write(byte[] b) throws IOException {}
        public void write(byte[] b, int off, int len) throws IOException {}
        public void flush() throws IOException {}
        public void close() throws IOException {}
    }
    
    public static class NullPrintStream
        extends PrintStream
    {
        public NullPrintStream() { super(new NullOutputStream()); }
    }
    
    // ------------------------------------------------------------------------
    
    private static boolean      errorMailerInit = false;
    private static MethodAction errorMailerAction = null;
        
    public static void emailError(String subject, String msgBody)
    {
        // We jump through these hoops so that 'Send-Mail' isn't referenced directly in this class
        // When running in an environment where 'Send-Mail' isn't available, this feature is
        // quietly ignored.
        
        /* get error mailer MethodAction */
        if (!Print.errorMailerInit) {
            Print.errorMailerInit = true;
            String mailerClass = RTConfig.getString(RTKey.LOG_SENDMAIL_CLASS,null);
            if ((mailerClass != null) && !mailerClass.equals("")) {
                try {
                    Print.errorMailerAction = new MethodAction(mailerClass, 
                        "sendError", 
                        new Class[] { String.class, String.class });
                } catch (Throwable t) {
                    // ignore
                    //Print.sysPrintln("Error: " + t);
                }
            }
        }
        
        /* send error email */
        //Send-Mail.sendError(subject, msgBody);
        if (Print.errorMailerAction != null) {
            try {
                Print.errorMailerAction.invoke(new Object[] { 
                    RTConfig.getString(RTKey.LOG_EMAIL_FROM,null), // null for default
                    RTConfig.getString(RTKey.LOG_EMAIL_TO  ,null), // null for default
                    subject, 
                    msgBody 
                });
            } catch (Throwable t) {
                // ignore
                //Print.sysPrintln("Error: " + t);
            }
        }
        
    }
    
    // ------------------------------------------------------------------------
    
}
