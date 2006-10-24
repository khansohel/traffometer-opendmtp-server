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
//  2006/04/09  Martin D. Flynn
//      Initial release
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.server_mysql;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opendmtp.util.*;
import org.opendmtp.codes.*;
import org.opendmtp.dbtools.*;

import org.opendmtp.server.db.*;
import org.opendmtp.server_mysql.db.*;
import org.opendmtp.server_mysql.dbtypes.*;

public class EventUtil
{

    // ------------------------------------------------------------------------

    public  static final long   DFT_LIMIT   = 30L;
    public  static final long   MAX_LIMIT   = 200L;

    // ------------------------------------------------------------------------

    public static final int FORMAT_UNKNOWN  = 0;
    public static final int FORMAT_CSV      = 1;
    public static final int FORMAT_KML      = 2;
    
    public static int parseOutputFormat(String fmt, int dftFmt)
    {
        if (fmt == null) {
            return dftFmt;
        } else
        if (fmt.equalsIgnoreCase("csv")) {
            return FORMAT_CSV;
        } else
        if (fmt.equalsIgnoreCase("kml")) {
            return FORMAT_KML;
        } else {
            return dftFmt;
        }
    }

    // ------------------------------------------------------------------------

    private Device device = null;
    
    public EventUtil(Device dev) 
    {
        this.device = dev;
    }
     
    // ------------------------------------------------------------------------

    private boolean writeEvents_CSV(PrintWriter out, EventData evdata[])
        throws IOException
    {
        // we assume that the specified events belong to this device

        /* fields to place in CSV format */
        String evFields[] = new String[] {
            EventData.FLD_timestamp,
            EventData.FLD_statusCode,
            EventData.FLD_latitude,
            EventData.FLD_longitude,
            EventData.FLD_speedKPH,
            EventData.FLD_heading,
            EventData.FLD_altitude,
        };
                        
        /* print header */
        out.println("Date,Time,Code,Latitude,Longitude,Speed,Heading,Altitude");

        /* print events */
        for (int i = 0; i < evdata.length; i++) {
            String rcd = evdata[i].formatAsCSVRecord(evFields);
            out.println(rcd);
        }
            
        return true;
    }

    // ------------------------------------------------------------------------

    private static final String STYLE_DEFAULT           = "DefaultStyle";
    private static final String STYLE_START_MOTION      = "StartMotionStyle";
    private static final String STYLE_START_MOTION_LAST = "StartMotionLastStyle";
    private static final String STYLE_IN_MOTION         = "InMotionStyle";
    private static final String STYLE_IN_MOTION_LAST    = "InMotionLastStyle";
    private static final String STYLE_STOP_MOTION       = "StopMotionStyle";
    private static final String STYLE_STOP_MOTION_LAST  = "StopMotionLastStyle";

    private static final int ICON_RED_CAR[]             = new int[] { 224, 192 };
    private static final int ICON_RED_CAR_CIRCLE[]      = new int[] { 224, 224 };
    private static final int ICON_GREEN_CAR[]           = new int[] { 192,   0 };
    private static final int ICON_GREEN_CAR_CIRCLE[]    = new int[] { 192,  32 };
    private static final int ICON_YELLOW_CAR[]          = new int[] { 224, 128 };
    private static final int ICON_YELLOW_CAR_CIRCLE[]   = new int[] { 224, 160 };
    
    private String createStyle(String name, int icon[])
    {
        StringBuffer st = new StringBuffer();
        st.append("    <Style id=\"" + name + "\">\n");
        st.append("      <BalloonStyle id=\"DefaultBalloonStyle\">\n");
        st.append("        <text><![CDATA[<b>$[name]</b><br/><br/>$[description]]]></text>\n");
        st.append("      </BalloonStyle>\n");
        st.append("      <IconStyle id=\"DefaultIconStyle\">\n");
        st.append("        <color>ffffffff</color>\n");
        st.append("        <scale>0.70</scale>\n");
        st.append("        <Icon>\n");
        st.append("          <href>root://icons/palette-4.png</href>\n");
        st.append("          <x>" + icon[0] + "</x>\n");
        st.append("          <y>" + icon[1] + "</y>\n");
        st.append("          <w>32</w>\n");
        st.append("          <h>32</h>\n");
        st.append("        </Icon>\n");
        st.append("      </IconStyle>\n");
        st.append("      <LabelStyle id=\"DefaultLabelStyle\">\n");
        st.append("        <color>ffffffff</color>\n");
        st.append("        <scale>0.70</scale>\n");
        st.append("      </LabelStyle>\n");
        st.append("      <LineStyle id=\"DefaultLineStyle\">\n");
        st.append("        <color>ff0000ff</color>\n");
        st.append("        <width>15</width>\n");
        st.append("      </LineStyle>\n");
        st.append("      <PolyStyle id=\"DefaultPolyStyle\">\n");
        st.append("        <color>7f7faaaa</color>\n");
        st.append("        <colorMode>normal</colorMode>\n");
        st.append("      </PolyStyle>\n");
        st.append("    </Style>\n");
        return st.toString();
    }

    private boolean writeEvents_KML(PrintWriter out, EventData evdata[])
        throws IOException
    {
        // we assume that the specified events belong to this device

        /* header */
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<kml xmlns=\"http://earth.google.com/kml/2.0\">");
        out.println("  <Document>");
        
        /* styles */
        out.println(createStyle(STYLE_DEFAULT          , ICON_GREEN_CAR));
        out.println(createStyle(STYLE_START_MOTION     , ICON_GREEN_CAR));
        out.println(createStyle(STYLE_START_MOTION_LAST, ICON_GREEN_CAR_CIRCLE));
        out.println(createStyle(STYLE_STOP_MOTION      , ICON_RED_CAR));
        out.println(createStyle(STYLE_STOP_MOTION_LAST , ICON_RED_CAR_CIRCLE));
        out.println(createStyle(STYLE_IN_MOTION        , ICON_GREEN_CAR));
        out.println(createStyle(STYLE_IN_MOTION_LAST   , ICON_GREEN_CAR_CIRCLE));

        /* placemarks */
        StringBuffer pm = new StringBuffer();
        for (int i = 0; i < evdata.length; i++) {
            boolean isLast = (i == (evdata.length - 1));
            DateTime dt    = new DateTime(evdata[i].getTimestamp());
            String datStr  = dt.toString();
            int    code    = evdata[i].getStatusCode();
            String codStr  = evdata[i].getStatusCodeString();
            GeoPoint gp    = evdata[i].getGeoPoint();
            String latStr  = gp.getLatitudeString();
            String lonStr  = gp.getLongitudeString();
            long   altStr  = Math.round(evdata[i].getAltitude());
            String spdStr  = StringTools.format(evdata[i].getSpeedKPH() * GeoPoint.MILES_PER_KILOMETER, "#0.0");
            pm.setLength(0);
            pm.append("    <Placemark>\n");
            pm.append("      <name>").append(codStr).append("</name>\n");
            pm.append("      <description><![CDATA["+datStr+"<br/>Speed: "+spdStr+" mph]]></description>\n");
            String style = STYLE_DEFAULT;
            switch (code) {
                case StatusCodes.STATUS_MOTION_START:
                    style = isLast? STYLE_START_MOTION_LAST : STYLE_START_MOTION;
                    break;
                case StatusCodes.STATUS_MOTION_STOP:
                    style = isLast? STYLE_STOP_MOTION_LAST : STYLE_STOP_MOTION;
                    break;
                case StatusCodes.STATUS_MOTION_IN_MOTION:
                    style = isLast? STYLE_IN_MOTION_LAST : STYLE_IN_MOTION;
                    break;
            }
            pm.append("      <styleUrl>#" + style + "</styleUrl>\n");
            pm.append("      <Point>\n");
            pm.append("        <coordinates>").append(lonStr + "," + latStr + ",0").append("</coordinates>\n");
            pm.append("      </Point>\n");
            pm.append("    </Placemark>\n");
            out.println(pm.toString());
        }

        /* trailer */
        out.println("  </Document>");
        out.println("</kml>");
        
        return true;
    }
   
    // ------------------------------------------------------------------------

    public boolean writeEvents(OutputStream out, EventData evdata[], int format)
        throws IOException
    {
        return this.writeEvents(new PrintWriter(out), evdata, format);
    }
    
    public boolean writeEvents(PrintWriter out, EventData evdata[], int format)
        throws IOException
    {
        if ((out != null) && (evdata != null)) {
            switch (format) {
                case FORMAT_CSV:
                    return writeEvents_CSV(out, evdata);
                case FORMAT_KML:
                    return writeEvents_KML(out, evdata);
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------

    public static OutputStream openFileOutputStream(String outFile)
    {
        try {
            if ((outFile == null) || outFile.equals("") || outFile.equalsIgnoreCase("stdout")) {
                return System.out;
            } else
            if (outFile.equalsIgnoreCase("stderr")) {
                return System.err;
            } else {
                return new FileOutputStream(outFile, false/*no-append*/);
            }
        } catch (IOException ioe) {
            Print.logException("Unable to open output file: " + outFile, ioe);
            return null;
        }
    }
    
    public static void closeOutputStream(OutputStream out)
    {
        if ((out != null) && (out != System.out) && (out != System.err)) {
            try { out.close(); } catch (Throwable t) {/*ignore*/}
        }
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String ARG_ACCOUNT = "account";
    private static final String ARG_DEVICE  = "device";
    private static final String ARG_EVENTS  = "events";
    private static final String ARG_OUTPUT  = "output";
    private static final String ARG_FORMAT  = "format";

    private static void usage()
    {
        Print.logInfo("Usage:");
        Print.logInfo("  java ... " + DeviceDBImpl.class.getName() + " {options}");
        Print.logInfo("Options:");
        Print.logInfo("  -account=<id>                  Acount ID which owns Device");
        Print.logInfo("  -device=<id>                   Device ID to create/edit");
        Print.logInfo("  -events=<count>                Write last <count> events to output file");
        Print.logInfo("  -events=<from>/<to>[/<limit>]  Write events in specified range to output file");
        Print.logInfo("  -format=[csv|kml]              Event output format");
        Print.logInfo("  -output=<file>                 Event output file");
        System.exit(1);
    }

    public static void main(String argv[])
    {
        DBConfig.init(argv,true);
        String acctID  = RTConfig.getString(new String[] { "acct", ARG_ACCOUNT }, "");
        String devID   = RTConfig.getString(new String[] { "dev" , ARG_DEVICE  }, "");

        /* get account */
        if ((acctID == null) || acctID.equals("")) {
            Print.logError("Account-ID not specified.");
            usage();
        }
        Account acct = null;
        try {
            acct = Account.getAccount(acctID);
            if (acct == null) {
                Print.logError("Account-ID does not exist: " + acctID);
                usage();
            }
        } catch (DBException dbe) {
            Print.logError("Error loading Account: " + acctID);
            dbe.printException();
            System.exit(99);
        }

        /* get device */
        if ((devID == null) || devID.equals("")) {
            Print.logError("Device-ID not specified.");
            usage();
        }
        Device dev = null;
        try {
            dev = Device.getDevice(acctID, devID);
            if (dev == null) {
                Print.logError("Device-ID does not exist: " + acctID + "," + devID);
                usage();
            }
        } catch (DBException dbe) {
            Print.logError("Error loading Device: " + acctID + "," + devID);
            dbe.printException();
            System.exit(99);
        }

        /* events */
        // -events=40
        // -events=17345678/17364636/40
        if (RTConfig.hasProperty(ARG_EVENTS)) {
            
            /* get requested data range */
            String range = RTConfig.getString(ARG_EVENTS,"");
            String rangeFlds[] = StringTools.parseString(range, "/");
            long startTime = -1L, endTime = -1L, limit = DFT_LIMIT;
            if (rangeFlds.length == 1) {
                // return the last <limit> events
                startTime = -1L;
                endTime   = -1L;
                limit     = StringTools.parseLong(rangeFlds[0], DFT_LIMIT);
            } else
            if (rangeFlds.length >= 2) {
                // return the first <limit> events in specifed time range
                startTime = StringTools.parseLong(rangeFlds[0], 0L);
                endTime   = StringTools.parseLong(rangeFlds[1], 0L);
                limit     = (rangeFlds.length >= 3)? StringTools.parseLong(rangeFlds[2],DFT_LIMIT) : DFT_LIMIT;
            } else {
                startTime = -1L;
                endTime   = -1L;
                limit     = DFT_LIMIT;
            }

            /* open output file */
            String evFile = RTConfig.getString(ARG_OUTPUT, "");
            OutputStream fos = EventUtil.openFileOutputStream(evFile);
            if (fos == null) {
                System.exit(1);
            }

            /* extract records */
            // this assumes that the number of returned records is reasonable and fits in memory
            EventData evdata[] = null;
            try {
                if ((startTime <= 0L) && (endTime <= 0L)) {
                    evdata = dev.getLatestEvents(limit);
                } else {
                    evdata = dev.getRangeEvents(startTime, endTime, Device.LIMIT_TYPE_FIRST, limit);
                }
                if (evdata == null) { evdata = new EventData[0]; }
            } catch (DBException dbe) {
                dbe.printException();
                System.exit(99);
            }

            /* output records */
            int outFmt = EventUtil.parseOutputFormat(RTConfig.getString(ARG_FORMAT,null),FORMAT_CSV);
            EventUtil evUtil = new EventUtil(dev);
            try {
                evUtil.writeEvents(fos, evdata, outFmt);
            } catch (IOException t) {
                Print.logException("Error writing events", t);
                System.exit(1);
            }

            /* close output file */
            EventUtil.closeOutputStream(fos);

            /* done */
            System.exit(0);
            
        }

        /* usage */
        usage();

    }

}
