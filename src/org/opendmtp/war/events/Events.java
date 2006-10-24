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
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.war.events;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opendmtp.util.*;
import org.opendmtp.server.db.*;
import org.opendmtp.dbtools.*;
import org.opendmtp.war.tools.*;

import org.opendmtp.server_mysql.EventUtil;
import org.opendmtp.server_mysql.db.*;

public class Events 
    extends HttpServlet
{

    // ------------------------------------------------------------------------

    public static final String CONTENT_TYPE_PLAIN   = "text/plain";
    public static final String CONTENT_TYPE_CSV     = "text/comma-separated-values";
    public static final String CONTENT_TYPE_CSV2    = "text/csv";
    public static final String CONTENT_TYPE_KML     = "application/vnd.google-earth.kml+xml kml";

    // ------------------------------------------------------------------------

    private static final String PARM_ACCOUNT    = "a"; // Account
    private static final String PARM_PASSWORD   = "p"; // Password
    private static final String PARM_DEVICE     = "d"; // Device
    private static final String PARM_RANGE      = "r"; // Range
    //private static final String PARM_FORMAT   = "f"; // Format (optional)

    // ------------------------------------------------------------------------

    static {
        RTConfig.setString(RTKey.DB_NAME, "dmtp");
        RTConfig.setString(RTKey.DB_HOST, "localhost");
        RTConfig.setString(RTKey.DB_USER, "dmtp");
        RTConfig.setString(RTKey.DB_PASS, "opendmtp");
        RTConfig.startupInit();
    };
    
    // ------------------------------------------------------------------------

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        this.doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String ipAddr    = request.getRemoteAddr();
        String accountID = ServletTools.getRequestString(request, PARM_ACCOUNT , "");
        String password  = ServletTools.getRequestString(request, PARM_PASSWORD, "");
        String deviceID  = ServletTools.getRequestString(request, PARM_DEVICE  , "");
        String rangeStr  = ServletTools.getRequestString(request, PARM_RANGE   , "");
        String fmtStr    = null; // ServletTools.getRequestString(request, PARM_FORMAT  , "");
        
        /* output format */
        if ((fmtStr == null) || fmtStr.equals("")) {
            String uri = request.getRequestURI();
            fmtStr = FileTools.getExtension(uri);
        }
        int outFmt = EventUtil.parseOutputFormat(fmtStr, EventUtil.FORMAT_CSV);
            
        /* get requested data range */
        String rangeFlds[] = StringTools.parseString(rangeStr, "/");
        long startTime = -1L, endTime = -1L, limit = -1L;
        if (rangeFlds.length == 1) {
            // return the last <limit> events
            startTime = -1L;
            endTime   = -1L;
            limit     = StringTools.parseLong(rangeFlds[0], -1L);
        } else
        if (rangeFlds.length >= 2) {
            // return the first <limit> events in specifed time range
            startTime = StringTools.parseLong(rangeFlds[0], 0L);
            endTime   = StringTools.parseLong(rangeFlds[1], 0L);
            limit     = (rangeFlds.length >= 3)? StringTools.parseLong(rangeFlds[2],-1L) : -1L;
        } else {
            startTime = -1L;
            endTime   = -1L;
            limit     = -1L;
        }
        if (limit <= 0L) {
            limit = EventUtil.DFT_LIMIT;
        } else
        if (limit > EventUtil.MAX_LIMIT) {
            limit = EventUtil.MAX_LIMIT;
        }

        /* validate account/password */
        Account account = null;
        try {
            account = Account.getAccount(accountID);
            if (account != null) {
                // we found the account, check the pasword
                if (!account.checkPassword(password)) {
                    // password is invalid
                    account = null;
                }
            }
            if (account == null) {
                this.errorResponse(response, "Invalid Account");
                return;
            }
        } catch (DBException dbe) {
            this.errorResponse(response, "Error reading Account");
            return;
        }
        
        /* read device */
        Device device = null;
        try {
            device = Device.getDevice(accountID, deviceID);
            if (device == null) {
                this.errorResponse(response, "Invalid Device");
                return;
            }
        } catch (DBException dbe) {
            dbe.printException();
            this.errorResponse(response, "Error reading Device");
            return;
        }

        /* extract records */
        // this assumes that the number of returned records is reasonable and fits in memory
        EventData evdata[] = null;
        try {
            if ((startTime <= 0L) && (endTime <= 0L)) {
                evdata = device.getLatestEvents(limit);
            } else {
                evdata = device.getRangeEvents(startTime, endTime, Device.LIMIT_TYPE_FIRST, limit);
            }
            if (evdata == null) { evdata = new EventData[0]; }
        } catch (DBException dbe) {
            dbe.printException();
            this.errorResponse(response, "Error reading Events");
            return;
        }

        /* mime content type */
        switch (outFmt) {
            case EventUtil.FORMAT_CSV:
                response.setContentType(CONTENT_TYPE_CSV);
                break;
            case EventUtil.FORMAT_KML:
                response.setContentType(CONTENT_TYPE_KML);
                break;
            default:
                response.setContentType(CONTENT_TYPE_PLAIN);
                break;
        }

        /* return events */
        PrintWriter out = response.getWriter();
        EventUtil evUtil = new EventUtil(device);
        try {
            evUtil.writeEvents(out, evdata, outFmt);
        } catch (IOException ioe) {
            Print.logException("Error writing events", ioe);
            this.errorResponse(response, "Error writing Events");
            return;
        }
        
    }

    // ------------------------------------------------------------------------

    protected void errorResponse(HttpServletResponse response, String msg)
        throws ServletException, IOException
    {
        response.setContentType(CONTENT_TYPE_PLAIN);
        PrintWriter out = response.getWriter();
        out.println(msg);
    }
    
}
