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
//  2006/04/23  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.war.tools;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.opendmtp.util.StringTools;

/**
 * Provides tools for parsing request in the support for servlet <tt>Events</tt>.
 * 
 * @author Martin D. Flynn
 * @author Guanghong Yang
 * @see org.opendmtp.war.events.Events
 * 
 */
public class ServletTools {

  /**
   * Gets a String parameter associated with the key from the request.
   * 
   * @param request The request to be parsed.
   * @param key The key to be matched.
   * @param dft The default String value to returned if request or key is null or not found.
   * @return The String parameter associated with the key from the request. Returns input parameter
   *         dft if request or key is null or not found.
   */
  public static String getRequestString(HttpServletRequest request, String key, String dft) {
    boolean ignoreCase = true;
    if ((request == null) || (key == null)) {
      return dft;
    }
    else if (ignoreCase) {
      for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
        String n = (String) e.nextElement();
        if (n.equalsIgnoreCase(key)) {
          String val = request.getParameter(n);
          return (val != null) ? val : dft;
        }
      }
      return dft;
    }
    else {
      String val = request.getParameter(key);
      return (val != null) ? val : dft;
    }
  }

  /**
   * Gets a long parameter associated with the key from the request.
   * 
   * @param request The request to be parsed.
   * @param key The key to be matched.
   * @param dft The default long value to returned if not found or the value cannot be parsed as
   *        long.
   * @return The long parameter associated with the key from the request. Returns input parameter
   *         dft if not found or the value cannot be parsed as long.
   * @see org.opendmtp.util.StringTools#parseLong(String, long).
   */
  public static long getRequestLong(HttpServletRequest request, String key, long dft) {
    String val = ServletTools.getRequestString(request, key, null);
    return StringTools.parseLong(val, dft);
  }

  /**
   * Gets a boolean parameter associated with the key from the request.
   * 
   * @param request The request to be parsed.
   * @param key The key to be matched.
   * @param dft The default boolean value to returned if not found or the value cannot be parsed as
   *        boolean.
   * @return The boolean parameter associated with the key from the request. Returns input parameter
   *         dft if not found or the value cannot be parsed as boolean.
   * @see org.opendmtp.util.StringTools#parseBoolean(String, boolean).
   */
  public static boolean getRequestBoolean(HttpServletRequest request, String key, boolean dft) {
    String val = ServletTools.getRequestString(request, key, null);
    return StringTools.parseBoolean(val, dft);
  }

  // ------------------------------------------------------------------------

}
