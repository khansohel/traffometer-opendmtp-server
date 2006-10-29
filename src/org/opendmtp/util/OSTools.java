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
//  General OS specific tools
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.io.File;

/**
 * This class provides utility methods for dealing with operating system related issues.
 * @author Martin D. Flynn
 * @author Mark Stillwell
 */
public class OSTools {

  // ------------------------------------------------------------------------
  // OS and JVM specific tools
  // ------------------------------------------------------------------------

  private static int OS_INITIALIZE = -1;
  /** Integer value representing unknown operating system. */
  public static int OS_UNKNOWN = 0x1000;
  /** Integer value representing unix operating systems. */
  public static int OS_UNIX = 0x0010;
  /** Integer value representing macintosh operating systems. */
  public static int OS_MAC = 0x0020;
  /** Integer value representing generic Microsoft Windows operating systems. */
  public static int OS_WINDOWS = 0x0040;
  /** Integer value representing Microsoft Windows XP. */
  public static int OS_WINDOWS_XP = OS_WINDOWS | 0x0001;
  /** Integer value representing Microsoft Windows 9x. */
  public static int OS_WINDOWS_9X = OS_WINDOWS | 0x0002;

  private static int OSType = OS_INITIALIZE;

  /**
   * Returns an integer corresponding to the operating system type.
   * @return operating system type
   */
  public static int getOSType() {
    if (OSType == OS_INITIALIZE) {
      String osName = System.getProperty("os.name").toLowerCase();
      Print.logInfo("OS: " + osName);
      if (osName.startsWith("windows")) {
        if (osName.startsWith("windows xp")) {
          OSType = OS_WINDOWS_XP;
        }
        else if (osName.startsWith("windows 9") || osName.startsWith("windows m")) {
          OSType = OS_WINDOWS_9X;
        }
        else {
          OSType = OS_WINDOWS;
        }
      }
      else if (File.separatorChar == '/') {
        OSType = OS_UNIX;
      }
      else {
        OSType = OS_UNKNOWN;
      }
    }
    return OSType;
  }

  // ------------------------------------------------------------------------

  /**
   * Returns true if operating system type is unknown.
   * @return true if operating system type is unknown
   */
  public static boolean isUnknown() {
    return (getOSType() == OS_UNKNOWN);
  }

  /**
   * Returns true if operating system type is Microsoft Windows.
   * @return true if operating system type is windows
   */
  public static boolean isWindows() {
    return ((getOSType() & OS_WINDOWS) != 0);
  }

  /**
   * Returns true if operating system type is Microsoft Windows XP.
   * @return true if operating system type is windows xp
   */  
  public static boolean isWindowsXP() {
    return (getOSType() == OS_WINDOWS_XP);
  }

  /**
   * Returns true if operating system type is Microsoft Windows 9x or Millennium.
   * @return true if operating system type is windows 9x
   */  
  public static boolean isWindows9X() {
    return (getOSType() == OS_WINDOWS_9X);
  }

  /**
   * Returns true if operating system type is unix.
   * Note: also returns true if operating system is Linux
   * @return true if operating system type is unix
   */ 
  public static boolean isUnix() {
    return ((getOSType() & OS_UNIX) != 0);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns true if operating system type is Microsoft Windows.
   * @return true if operating system type is windows
   */  
  public static boolean isBrokenToFront() {
    return isWindows();
  }

  // ------------------------------------------------------------------------

  /**
   * Prints a list of calling classes to the log.
   */
  public static void printCallerClasses() {
    for (int i = 0;; i++) {
      Class clz = sun.reflect.Reflection.getCallerClass(i);
      Print.logInfo("" + i + "] class " + StringTools.className(clz));
      if (clz == null) {
        break;
      }
    }
  }

  /**
   * Provides a shortcut to sun.reflect.Reflection.getCallerClass.
   * @param frame level of class above OSTools on call stack
   * @return a class on the calling stack
   */
  public static Class getCallerClass(int frame) {
    // sun.reflect.Reflection.getCallerClass(0) == sun.reflect.Reflection
    // sun.reflect.Reflection.getCallerClass(1) == OSTools
    Class clz = sun.reflect.Reflection.getCallerClass(frame + 1);
    // Print._println("" + (frame + 1) + "] class " + StringTools.className(clz));
    return clz;
  }

  // ------------------------------------------------------------------------

  /**
   * Runs operating system tests from the commandline and prints results.
   * @param argv command line argument vector
   */
  public static void main(String argv[]) {
    RTConfig.setCommandLineArgs(argv);
    Print.logInfo("Is Windows  : " + isWindows());
    Print.logInfo("Is Windows9X: " + isWindows9X());
    Print.logInfo("Is WindowsXP: " + isWindowsXP());
    Print.logInfo("Is Unix     : " + isUnix());
  }

}

