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

import java.lang.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opendmtp.util.*;

public class ServletTools
{
    
    // ------------------------------------------------------------------------
    
    public static String getRequestString(HttpServletRequest request, String key, String dft)
    {
        boolean ignoreCase = true;
        if ((request == null) || (key == null)) {
            return dft;
        } else
        if (ignoreCase) {
            for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
                String n = (String)e.nextElement();
                if (n.equalsIgnoreCase(key)) {
                    String val = request.getParameter(n);
                    return (val != null)? val : dft;
                }
            }
            return dft;
        } else {
            String val = request.getParameter(key);
            return (val != null)? val : dft;
        }
    }
    
    public static long getRequestLong(HttpServletRequest request, String key, long dft)
    {
        String val = ServletTools.getRequestString(request, key, null);
        return StringTools.parseLong(val, dft);
    }
    
    public static boolean getRequestBoolean(HttpServletRequest request, String key, boolean dft)
    {
        String val = ServletTools.getRequestString(request, key, null);
        return StringTools.parseBoolean(val, dft);
    }
    
    // ------------------------------------------------------------------------
    
}
