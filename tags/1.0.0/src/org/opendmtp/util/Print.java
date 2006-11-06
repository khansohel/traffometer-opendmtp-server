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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;

//import java.io.*;
//import java.net.*;
//import java.sql.*;

/**
 * Provides general printing/logging utilities.
 * 
 * @author Martin D. Flynn
 * @author Yoshiaki Iinuma
 */
public class Print {

  // ------------------------------------------------------------------------

  /** Represents the log message level where the log contains no messages. */
  public static final int LOG_OFF = 0;
  /** Represents the log message level where the log contains only fatal error messages. */
  public static final int LOG_FATAL = 1;
  /** Represents the log message level where the log contains only error messages. */
  public static final int LOG_ERROR = 2;
  /** Represents the log message level where the log contains warning and error messages. */
  public static final int LOG_WARN = 3;
  /**
   * Represents the log message level where the log contains information messages as well as
   * warnings and errors.
   */
  public static final int LOG_INFO = 4;
  /** Represents the log message level where the log contains even debug information. */
  public static final int LOG_DEBUG = 5;
  /** Represents the log message level where the log contains every kind of messages. */
  public static final int LOG_ALL = 6;

  // ------------------------------------------------------------------------

  /** File extension for java source code. */
  private static final String _JAVA = ".java";

  // ------------------------------------------------------------------------

  private static PrintStream stdout = null;
  private static PrintStream stderr = null;
  private static PrintStream sysStdout = null;

  // ------------------------------------------------------------------------

  private static String localhostName = null;

  /**
   * Returns the local host name.
   * 
   * @return the local host name.
   */
  public static String getHostName() {
    /* host name */
    if (Print.localhostName == null) {
      try {
        String hd = InetAddress.getLocalHost().getHostName();
        int p = hd.indexOf(".");
        Print.localhostName = (p >= 0) ? hd.substring(0, p) : hd;
      }
      catch (UnknownHostException uhe) {
        Print.localhostName = "UNKNOWN";
      }
    }
    return Print.localhostName;
  }

  // ------------------------------------------------------------------------

  /**
   * Sets the specified print stream to standard output.
   * 
   * @param out the print stream to be set.
   */
  public static void setStdout(PrintStream out) {
    Print.stdout = out;
  }

  /**
   * Sets the specified print stream to standard error.
   * 
   * @param err the print stream to be set.
   */
  public static void setStderr(PrintStream err) {
    Print.stderr = err;
  }

  /**
   * Returns the current print stream assigned to standard output.
   * 
   * @return the current print stream assigned to standard output. If a print stream is not assigned
   *         to standard output, returns the system standard output.
   */
  public static PrintStream getStdout() {
    return (Print.stdout != null) ? Print.stdout : System.out;
  }

  /**
   * Returns the current print stream assigned to standard error.
   * 
   * @return the current print stream assigned to standard error. If a print stream is not assigned
   *         to standard error, returns the system standard error.
   */
  public static PrintStream getStderr() {
    return (Print.stdout != null) ? Print.stderr : System.err;
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the information about a method invocation contained in the specified stack frame in the
   * execution stack.
   * 
   * @param frame the index to specify a stack frame in the execution stack.
   * @return the information about a method invocation contained in the specified stack frame in the
   *         execution stack.
   */
  protected static String _getStackFrame(int frame) {

    /* extract stack frame */
    Throwable t = new Throwable();
    t.fillInStackTrace();
    StackTraceElement st[] = t.getStackTrace();
    StackTraceElement sf = (st != null) ? st[frame + 1] : null;

    /* no stack frame? */
    if (sf == null) {
      return "?";
    }

    /* get file */
    String clazz = sf.getClassName();
    String file = sf.getFileName();
    if (file == null) {
      // Java code was compiled with 'debug=false'
      int p = 0;
      for (; (p < clazz.length()) && !Character.isUpperCase(clazz.charAt(p)); p++)
        ;
      if (p < clazz.length()) {
        clazz = clazz.substring(p);
      }
    }
    else if (file.toLowerCase().endsWith(_JAVA)) {
      file = file.substring(0, file.length() - _JAVA.length());
      int p = clazz.indexOf(file);
      if (p >= 0) {
        clazz = clazz.substring(p);
      }
    }

    /* format frame description */
    StringBuffer sb = new StringBuffer();
    sb.append(clazz);
    sb.append(".").append(sf.getMethodName());
    sb.append(":").append(sf.getLineNumber());

    return sb.toString();
  }

  /**
   * Tests if the log contains the information of the stack frame level.
   * 
   * @return true if the log contains the information of the stack frame level.
   */
  protected static boolean _includeStackFrame() {
    return RTConfig.getBoolean(RTKey.LOG_INCL_STACKFRAME, false)
        || (Print.getLogLevel() >= Print.LOG_DEBUG);
  }

  /**
   * Tests if the log contains the information about date.
   * 
   * @return true if the log contains the information about date.
   */
  protected static boolean _includeDate() {
    return RTConfig.getBoolean(RTKey.LOG_INCL_DATE, false);
  }

  // ------------------------------------------------------------------------

  /**
   * Prints the specified message to the specified stream with a newline character. The message is
   * appended the information about the second latest stack frame.
   * 
   * @param ps the print stream to which a msg to be printed.
   * @param msg the message to be printed.
   */
  public static void _println(PrintStream ps, String msg) {
    // Does not use RTConfig
    Print._print(ps, 1, true, msg + "\n");
  }

  /**
   * Prints the specified message to the specified stream with a newline character. The message is
   * appended the information about the specified stack frame if specified.
   * 
   * @param ps the print stream to which a msg to be printed.
   * @param frame the index to specify a stack frame in the execution stack.
   * @param printFrame the boolean value to indicate if message should contain the stack frame level
   *        information.
   * @param msg the message to be printed.
   */
  protected static void _println(PrintStream ps, int frame, boolean printFrame, String msg) {
    Print._print(ps, frame + 1, printFrame, msg + "\n");
  }

  /**
   * Prints the specified message to the specified stream with a newline character. The message is
   * appended the information about the specified stack frame if specified.
   * 
   * @param ps the print stream to which a msg to be printed.
   * @param frame the index to specify a stack frame in the execution stack.
   * @param msg the message to be printed.
   */
  protected static void _println(PrintStream ps, int frame, String msg) {
    Print._print(ps, frame + 1, _includeStackFrame(), msg + "\n");
  }

  /**
   * Prints the specified message to the specified stream without a newline character. The message
   * is appended the information about the specified stack frame if specified.
   * 
   * @param ps the print stream to which a msg to be printed.
   * @param frame the index to specify a stack frame in the execution stack.
   * @param msg the message to be printed.
   */
  protected static void _print(PrintStream ps, int frame, String msg) {
    Print._print(ps, frame + 1, _includeStackFrame(), msg);
  }

  /**
   * Prints the specified message to the specified stream without a newline character. The message
   * is appended the information about the specified stack frame if specified.
   * 
   * @param ps the print stream to which a msg to be printed.
   * @param frame the index to specify a stack frame in the execution stack.
   * @param printFrame the boolean value to indicate if message should contain the stack frame level
   *        information.
   * @param msg the message to be printed.
   */
  protected static void _print(PrintStream ps, int frame, boolean printFrame, String msg) {
    // - use of RTConfig is NOT allowed in this method!
    // - if not writing to 'Print.stdout', then we really want to open/close this file

    /* Print stream */
    PrintStream out = (ps != null) ? ps : Print.getStdout();

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

  /**
   * Sets the specified print stream to the system standard output.
   * 
   * @param out the print stream to be set.
   */
  public static void setSysStdout(PrintStream out) {
    Print.sysStdout = out;
  }

  /**
   * Returns the system standard output.
   * 
   * @return the system standard output.
   */
  public static PrintStream getSysStdout() {
    return (Print.sysStdout != null) ? Print.sysStdout : Print.getStdout();
  }

  /**
   * Prints the specified message to the system standard output without a newline character and the
   * stack frame info.
   * 
   * @param msg the message to be printed
   */
  public static void sysPrint(String msg) {
    PrintStream out = Print.getSysStdout();
    Print._print(out, 1, false, msg);
  }

  /**
   * Prints the specified message to the system standard output without a newline character and the
   * stack frame info.
   * 
   * @param msg the StringBuffer object containing the message to be printed
   */
  public static void sysPrint(StringBuffer msg) {
    PrintStream out = Print.getSysStdout();
    Print._print(out, 1, false, msg.toString());
  }

  /**
   * Prints the specified message to the system standard output with a newline character and without
   * the stack frame info.
   * 
   * @param msg the message to be printed
   */
  public static void sysPrintln(String msg) {
    PrintStream out = Print.getSysStdout();
    Print._println(out, 1, false, msg);
  }

  /**
   * Prints the specified message to the system standard output with a newline character and without
   * the stack frame info.
   * 
   * @param msg the StringBuffer object containing the message to be printed
   */
  public static void sysPrintln(StringBuffer msg) {
    PrintStream out = null;
    Print._println(out, 1, false, msg.toString());
  }

  // ------------------------------------------------------------------------

  /**
   * Prints the specified message with the print stream configured in the application without a
   * newline character and the stack frame info.
   * 
   * @param msg the message to be printed
   */
  public static void print(String msg) {
    Print._print(null, 1, false, msg);
  }

  /**
   * Prints the specified message with the print stream configured in the application without a
   * newline character and the stack frame info.
   * 
   * @param msg the StringBuffer object containing the message to be printed
   */
  public static void print(StringBuffer msg) {
    Print._print(null, 1, false, msg.toString());
  }

  /**
   * Prints the specified message with the print stream configured in the application with a newline
   * character and without the stack frame info.
   * 
   * @param msg the message to be printed
   */
  public static void println(String msg) {
    Print._println(null, 1, false, msg);
  }

  /**
   * Prints the specified message with the print stream configured in the application with a newline
   * character and without the stack frame info.
   * 
   * @param msg the StringBuffer object containing the message to be printed
   */
  public static void println(StringBuffer msg) {
    Print._println(null, 1, false, msg.toString());
  }

  // ------------------------------------------------------------------------

  /**
   * Prints the contents of the specified stack frame with the specified message to the log.
   * 
   * @param frame the index to specify the position of a stack frame. (in the chronological order)
   * @param msg the message to be printed.
   * @param t the Throwable object containing a snapshot of the execution stack in the current
   *        thread.
   */
  protected static void _logStackTrace(int frame, String msg, Throwable t) {

    /* log stack trace */
    try {
      PrintStream out = Print.openLogFile();
      _printStackTrace(out, frame + 1, msg, t);
    }
    catch (Throwable loge) {
      _printStackTrace(null, frame + 1, msg, t);
    }
    finally {
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

  /**
   * Prints the contents of the specified stack frame with the specified message to the specified
   * print stream.
   * 
   * @param out the print stream to which a message is printed.
   * @param frame the index to specify the position of a stack frame.
   * @param msg the message to be printed.
   * @param t the Throwable object containing a snapshot of the execution stack in the current
   *        thread.
   */
  protected static void _printStackTrace(PrintStream out, int frame, String msg, Throwable t) {

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
      SQLException sqe = ((SQLException) t).getNextException();
      for (; (sqe != null); sqe = sqe.getNextException()) {
        sqe.printStackTrace(out);
      }
    }

  }

  // ------------------------------------------------------------------------

  /**
   * Prints the contents of the specified stack frame with the specified message to the log.
   * 
   * @param msg the message to be printed.
   */
  public static void logNotImplemented(String msg) {
    Print._logStackTrace(1, "Feature Not Implemented: " + msg, null);
  }

  /**
   * Prints an exception with the specified message to the log.
   * 
   * @param msg the message to be printed.
   * @param t the Throwable object containing the information about an exception of the execution
   *        stack in the current thread.
   */
  public static void logException(String msg, Throwable t) {
    Print._logStackTrace(1, "Exception: " + msg, t);
  }

  /**
   * Prints the stack trace information with the specified message to the log.
   * 
   * @param msg the message to be printed.
   * @param t the Throwable object containing the stack trace information of the execution stack in
   *        the current thread.
   */
  public static void logStackTrace(String msg, Throwable t) {
    Print._logStackTrace(1, "Stacktrace: " + msg, t);
  }

  /**
   * Prints the stack trace information to the log.
   * 
   * @param t the Throwable object containing the stack trace information of the execution stack in
   *        the current thread.
   */
  public static void logStackTrace(Throwable t) {
    Print._logStackTrace(1, "Stacktrace: ", t);
  }

  /**
   * Prints the specified message as a stack trace information to the log.
   * 
   * @param msg the message to be printed.
   */
  public static void logStackTrace(String msg) {
    Print._logStackTrace(1, "Stacktrace: " + msg, null);
  }

  // ------------------------------------------------------------------------

  /**
   * Logs the error level message about the specified SQLException.
   * 
   * @param frame the index to specify the position of the stack frame to be logged.
   * @param msg the message to be appended to the log message.
   * @param sqe the SQLException object containing the error message about the SQL.
   */
  public static void logSQLError(int frame, String msg, SQLException sqe) {
    PrintStream ps = null;
    Print._log(LOG_ERROR, frame + 1, "==> SQLException: " + msg);
    while (sqe != null) {
      Print._log(LOG_ERROR, frame + 1, "Message:   " + sqe.getMessage());
      Print._log(LOG_ERROR, frame + 1, "SQLState:  " + sqe.getSQLState());
      Print._log(LOG_ERROR, frame + 1, "ErrorCode: " + sqe.getErrorCode());
      // if (sqe.getErrorCode() != DBFactory.SQLERR_DUPLICATE_KEY) {
      Print._printStackTrace(ps, frame + 1, sqe.toString(), sqe);
      // }
      sqe = sqe.getNextException();
    }
  }

  /**
   * Logs the error about the specified SQLException.
   * 
   * @param sqe the SQLException object containing the error message about the SQL.
   */
  public static void logSQLError(SQLException sqe) {
    Print.logSQLError(1, "", sqe);
  }

  /**
   * Logs the error about the specified SQLException with the specified message.
   * 
   * @param msg the message to be appended to the log message.
   * @param sqe the SQLException object containing the error message about the SQL.
   */
  public static void logSQLError(String msg, SQLException sqe) {
    Print.logSQLError(1, msg, sqe);
  }

  // ------------------------------------------------------------------------

  private static Object logLock = new Object();
  private static PrintStream logOutput = null;
  private static long logRefCount = 0L;

  /**
   * Opens the file for the log.
   * 
   * @return output stream to which the log messages will be printed. If there is an error, returns
   *         standard error.
   */
  protected static PrintStream openLogFile() {
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

      }
      else {

        /* get/return log file */
        File file = RTConfig.getFile(RTKey.LOG_FILE, null);
        if ((file == null) || file.toString().equals("")) {
          out = System.err;
        }
        else if (file.isDirectory()) {
          RTConfig.setFile(RTKey.LOG_FILE, null);
          Print.println("ERROR: Invalid file specification: " + file);
          out = System.err;
        }
        else {
          if (file.exists()) {
            long maxLogFileSize = RTConfig.getLong(RTKey.LOG_FILE_MAX_SIZE, 0L);
            if ((maxLogFileSize > 1000L) && (file.length() > maxLogFileSize)) {
              String bkuName = file.getAbsolutePath() + "."
                  + (new DateTime()).format("yyMMdd", null);
              File bkuFile = new File(bkuName);
              for (int i = 1; bkuFile.exists(); i++) {
                bkuFile = new File(bkuName + "." + i);
              }
              file.renameTo(bkuFile);
            }
          }
          try {
            out = new PrintStream(new FileOutputStream(file, true));
          }
          catch (IOException ioe) {
            RTConfig.setFile(RTKey.LOG_FILE, null);
            Print.println("ERROR: Unable to open log file: " + file);
            out = System.err;
          }
        }
        Print.logOutput = out;

      }

    }
    return out;
  }

  /**
   * Close the output stream to which the log messages are printed.
   */
  protected static void closeLogFile() {
    synchronized (Print.logLock) {

      /* decrement log counter */
      Print.logRefCount--;
      if (Print.logRefCount < 0) {
        Print.logRefCount = 0L;
      }

      /* close */
      if ((Print.logRefCount == 0L) && (Print.logOutput != null)) {
        // don't close if stderr or stdout
        if ((Print.logOutput != System.out) && (Print.logOutput != System.err)) {
          try {
            Print.logOutput.close();
          }
          catch (Throwable t) {
            Print.sysPrintln("Unable to close log file: " + t);
          }
        }
        Print.logOutput = null;
      }

    }
  }

  /**
   * Sets the log message level.
   * 
   * @param level the log message level to be set.
   */
  public static void setLogLevel(int level) {
    RTConfig.setInt(RTKey.LOG_LEVEL, level);
  }

  /**
   * Sets the log message level and also decides whether the log message contains the information
   * about date and a method invocation.
   * 
   * @param level the log message level to be set.
   * @param inclDate the boolean value to indicate that the log message contains date information.
   * @param inclFrame the boolean value to indicate that the log message contains method invocation
   *        information.
   */
  public static void setLogLevel(int level, boolean inclDate, boolean inclFrame) {
    Print.setLogLevel(level);
    RTConfig.setBoolean(RTKey.LOG_INCL_DATE, inclDate);
    RTConfig.setBoolean(RTKey.LOG_INCL_STACKFRAME, inclFrame);
  }

  /**
   * Returns the current log message level.
   * 
   * @return the current log message level.
   */
  public static int getLogLevel() {
    return Print.parseLogLevel(RTConfig.getString(RTKey.LOG_LEVEL, null));
  }

  /**
   * Sets the log header level.
   * 
   * @param level the log header level to be set.
   */
  public static void setLogHeaderLevel(int level) {
    RTConfig.setInt(RTKey.LOG_LEVEL_HEADER, level);
  }

  /**
   * Returns the current log header level.
   * 
   * @return the current log header level.
   */
  public static int getLogHeaderLevel() {
    return Print.parseLogLevel(RTConfig.getString(RTKey.LOG_LEVEL_HEADER, null));
  }

  /**
   * Returns the string corresponding to the specified log message level.
   * 
   * @param level the log message level.
   * @return the string corresponding to the specified log message level.
   */
  public static String getLogLevelString(int level) {
    if (level <= 0) {
      return "OFF";
    }
    switch (level) {
    case LOG_FATAL:
      return "FATAL";
    case LOG_ERROR:
      return "ERROR";
    case LOG_WARN:
      return "WARN ";
    case LOG_INFO:
      return "INFO ";
    case LOG_DEBUG:
      return "DEBUG";
    }
    return "ALL";
  }

  /**
   * Returns the log level specified by the given string.
   * 
   * @param val the string value to specify the log level.
   * @return the log level specified by the given string.
   */
  public static int parseLogLevel(String val) {
    String v = (val != null) ? val.toUpperCase() : null;
    if ((v == null) || v.equals("") || v.startsWith("OFF")) {
      return LOG_OFF;
    }
    else if (v.startsWith("FAT")) {
      return LOG_FATAL;
    }
    else if (v.startsWith("ERR")) {
      return LOG_ERROR;
    }
    else if (v.startsWith("WAR")) {
      return LOG_WARN;
    }
    else if (v.startsWith("INF")) {
      return LOG_INFO;
    }
    else if (v.startsWith("DEB")) {
      return LOG_DEBUG;
    }
    else if (Character.isDigit(v.charAt(0))) {
      int lvl = StringTools.parseInt(v.substring(0, 1), LOG_ALL);
      if (lvl < LOG_OFF) {
        return LOG_OFF;
      }
      else if (lvl > LOG_ALL) {
        return LOG_ALL;
      }
      else {
        return lvl;
      }
    }
    else {
      return LOG_ALL;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Logs the information of the specified level with the specified message.
   * 
   * @param level the log message level.
   * @param msg the message to be appended to the log information.
   */
  public static void log(int level, String msg) {
    Print._log(level, 1, msg);
  }

  /**
   * Logs the fatal error level information with the specified message.
   * 
   * @param msg the message to be appended to the log information.
   */
  public static void logFatal(String msg) {
    Print._log(LOG_FATAL, 1, msg);
  }

  /**
   * Logs the error level information with the specified message.
   * 
   * @param msg the message to be appended to the log information.
   */
  public static void logError(String msg) {
    Print._log(LOG_ERROR, 1, msg);
  }

  /**
   * Logs the warning level information with the specified message.
   * 
   * @param msg the message to be appended to the log information.
   */
  public static void logWarn(String msg) {
    Print._log(LOG_WARN, 1, msg);
  }

  /**
   * Logs the method invocation level information with the specified message.
   * 
   * @param msg the message to be appended to the log information.
   */
  public static void logInfo(String msg) {
    Print._log(LOG_INFO, 1, msg);
  }

  /**
   * Logs the debug level information with the specified message.
   * 
   * @param msg the message to be appended to the log information.
   */
  public static void logDebug(String msg) {
    Print._log(LOG_DEBUG, 1, msg);
  }

  /**
   * Generates and prints a log message of the specified level.
   * 
   * @param level the log message level.
   * @param frame the index to specify the position of a stack frame in the execution stack.
   * @param msg the message to be included in the final log message.
   */
  protected static void _log(int level, int frame, String msg) {

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
    if (!msg.endsWith("\n")) {
      logMsg.append("\n");
    }

    /* print message */
    try {
      PrintStream out = Print.openLogFile();
      if (out != null) {
        out.write(StringTools.getBytes(logMsg.toString()));
        out.flush();
      }
      else {
        Print._print(null, frame + 1, false, logMsg.toString());
      }
    }
    catch (IOException ioe) {
      RTConfig.setFile(RTKey.LOG_FILE, null);
      Print.logError("Unable to open/write log file: " + ioe);
      Print._print(null, frame + 1, false, logMsg.toString());
    }
    finally {
      Print.closeLogFile();
    }

  }

  // ------------------------------------------------------------------------

  /**
   * Provides a null output stream.
   * 
   * @author Martin D. Flynn
   * @author Yoshiaki Iinuma
   */
  public static class NullOutputStream extends OutputStream {

    /**
     * Default constructor of NullOutputStream.
     * 
     */
    public NullOutputStream() {
    }

    /**
     * Writes the specified integer value to this output stream. Actually, this method does nothing.
     * 
     * @param b the integer value to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void write(int b) throws IOException {
    }

    /**
     * Writes the specified character string to this output stream. Actually, this method does
     * nothing.
     * 
     * @param b the character string to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void write(byte[] b) throws IOException {
    }

    /**
     * Writes a substring of the specified character string to this output stream. Actually, this
     * method does nothing.
     * 
     * @param b the character string containing the substring to be written.
     * @param off the offset with which the substring starts.
     * @param len the length of the substring.
     * @throws IOException if an I/O error occurs.
     */
    public void write(byte[] b, int off, int len) throws IOException {
    }

    /**
     * Flushes this output stream. Actually, this method does nothing.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public void flush() throws IOException {
    }

    /**
     * Closes this output stream. Actually, this method does nothing.
     * 
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
    }
  }

  /**
   * Provides a null print stream.
   * 
   * @author Martin D. Flynn
   * @author Yoshiaki Iinuma
   */
  public static class NullPrintStream extends PrintStream {

    /**
     * Default constructor of NullPrintStream.
     * 
     */
    public NullPrintStream() {
      super(new NullOutputStream());
    }
  }

  // ------------------------------------------------------------------------

  private static boolean errorMailerInit = false;
  private static MethodAction errorMailerAction = null;

  /**
   * Sends an email containing error messages.
   * 
   * @param subject the email subject to be sent.
   * @param msgBody the email message body to be sent.
   */
  public static void emailError(String subject, String msgBody) {
    // We jump through these hoops so that 'Send-Mail' isn't referenced directly in this class
    // When running in an environment where 'Send-Mail' isn't available, this feature is
    // quietly ignored.

    /* get error mailer MethodAction */
    if (!Print.errorMailerInit) {
      Print.errorMailerInit = true;
      String mailerClass = RTConfig.getString(RTKey.LOG_SENDMAIL_CLASS, null);
      if ((mailerClass != null) && !mailerClass.equals("")) {
        try {
          Print.errorMailerAction = new MethodAction(mailerClass, "sendError", new Class[] {
              String.class, String.class });
        }
        catch (Throwable t) {
          // ignore
          // Print.sysPrintln("Error: " + t);
        }
      }
    }

    /* send error email */
    // Send-Mail.sendError(subject, msgBody);
    if (Print.errorMailerAction != null) {
      try {
        Print.errorMailerAction.invoke(new Object[] {
            RTConfig.getString(RTKey.LOG_EMAIL_FROM, null), // null for default
            RTConfig.getString(RTKey.LOG_EMAIL_TO, null), // null for default
            subject, msgBody });
      }
      catch (Throwable t) {
        // ignore
        // Print.sysPrintln("Error: " + t);
      }
    }

  }

  // ------------------------------------------------------------------------

}
