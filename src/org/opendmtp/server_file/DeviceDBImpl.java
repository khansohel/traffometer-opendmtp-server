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
//  2006/04/02  Martin D. Flynn
//      Fixed altitude output value
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.server_file;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opendmtp.util.*;
import org.opendmtp.codes.*;
import org.opendmtp.server.db.*;

public class DeviceDBImpl
    implements DeviceDB
{

    // ------------------------------------------------------------------------

    private static final int MAX_ALLOWED_EVENTS             = -1;  // no limit
    private static final int LIMIT_TIME_INTERVAL            = 0;
    
    private static final int MAX_TOTAL_CONNECTIONS          = -1;  // no limit
    private static final int MAX_TOTAL_CONNECTIONS_PER_MIN  = -1;  // no limit
        
    private static final int MAX_DUPLEX_CONNECTIONS         = -1;  // no limit
    private static final int MAX_DUPLEX_CONNECTIONS_PER_MIN = -1;  // no limit
    
    private static       int SUPPORTED_ENCODING = 
        Encoding.SUPPORTED_ENCODING_BINARY |
        Encoding.SUPPORTED_ENCODING_BASE64 |
        Encoding.SUPPORTED_ENCODING_HEX;

    // ------------------------------------------------------------------------

    private static File dataStoreDirectory = new File(".");
    
    public static void setDataStoreDirectory(File dir)
    {
        if ((dir != null) && dir.isDirectory()) {
            dataStoreDirectory = dir;
        } else {
            dataStoreDirectory = new File("./data");
            if (!dataStoreDirectory.isDirectory()) {
                dataStoreDirectory = new File(".");
            }
        }
    }
    
    public static File getDataStoreDirectory()
    {
        if (dataStoreDirectory == null) {
            dataStoreDirectory = new File(".");
        }
        return dataStoreDirectory;
    }

    // ------------------------------------------------------------------------

    private static HashMap customPayloadTemplates = new HashMap();
    
    // ------------------------------------------------------------------------
    
    /* id */
    private String      accountId                       = null;
    private String      deviceId                        = null;
    private String      description                     = "";

    /* active */
    private boolean     isActive                        = true;

    /* connection validator */
    private int         limitTimeInterval               = LIMIT_TIME_INTERVAL;

    /* max events */
    private int         maxAllowedEvents                = MAX_ALLOWED_EVENTS;

    /* total connections */
    private int         maxTotalConnections             = MAX_TOTAL_CONNECTIONS;
    private int         maxTotalConnectionsPerMinute    = MAX_TOTAL_CONNECTIONS_PER_MIN;
    private byte        totalConnectionProfile[]        = new byte[0];
    private long        lastTotalConnectionTime         = 0L;

    /* duplex connections */
    private int         maxDuplexConnections            = MAX_DUPLEX_CONNECTIONS;
    private int         maxDuplexConnectionsPerMinute   = MAX_DUPLEX_CONNECTIONS_PER_MIN;
    private byte        duplexConnectionProfile[]       = new byte[0];
    private long        lastDuplexConnectionTime        = 0L;

    public DeviceDBImpl(String acctId, String devId) 
    {
        this.accountId      = acctId;
        this.deviceId       = devId;
        this.description    = devId;
    }
    
    // ------------------------------------------------------------------------

    public String getAccountName() 
    {
        return this.accountId;
    }
        
    public String getDeviceName() 
    {
        return this.deviceId;
    }
    
    public String getDescription() 
    {
        return this.description;
    }
    
    public boolean isActive() 
    {
        return this.isActive;
    }
    
    // ------------------------------------------------------------------------

    public int getMaxAllowedEvents()
    {
        return this.maxAllowedEvents;
    }
    
    public long getEventCount(long timeStart, long timeEnd) 
    {
        return 0; // don't count events
    }
    
    // ------------------------------------------------------------------------

    public int getLimitTimeInterval()
    {
        return this.limitTimeInterval;
    }
    
    // ------------------------------------------------------------------------

    public int getMaxTotalConnections() 
    {
        return this.maxTotalConnections;
    }
    
    public int getMaxTotalConnectionsPerMinute() 
    {
        return this.maxTotalConnectionsPerMinute;
    }
    
    public byte[] getTotalConnectionProfile()
    {
        return this.totalConnectionProfile;
    }
    
    public void setTotalConnectionProfile(byte[] profile) 
    {
        this.totalConnectionProfile = profile;
    }

    public long getLastTotalConnectionTime()
    {
        return this.lastTotalConnectionTime;
    }

    public void setLastTotalConnectionTime(long time)
    {
        this.lastTotalConnectionTime = time;
    }
    
    // ------------------------------------------------------------------------

    public int getMaxDuplexConnections() 
    {
        return this.maxDuplexConnections;
    }
    
    public int getMaxDuplexConnectionsPerMinute()
    {
        return this.maxDuplexConnectionsPerMinute;
    }
    
    public byte[] getDuplexConnectionProfile()
    {
        return this.duplexConnectionProfile;
    }
    
    public void setDuplexConnectionProfile(byte[] profile)
    {
        this.duplexConnectionProfile = profile;
    }

    public long getLastDuplexConnectionTime()
    {
        return this.lastDuplexConnectionTime;
    }

    public void setLastDuplexConnectionTime(long time)
    {
        this.lastDuplexConnectionTime = time;
    }

    // ------------------------------------------------------------------------

    public boolean supportsEncoding(int encoding)
    {
        return ((SUPPORTED_ENCODING & encoding) != 0);
    }
    
    public void removeEncoding(int encoding) 
    {
        if ((SUPPORTED_ENCODING & encoding) != 0) {
            SUPPORTED_ENCODING &= ~encoding;
        }
    }

    // ------------------------------------------------------------------------

    public boolean addClientPayloadTemplate(PayloadTemplate template)
    {
        if (template != null) {
            int custType = template.getPacketType();
            customPayloadTemplates.put(new Integer(custType), template);
            return true;
        } else {
            return false;
        }
    }
    
    public PayloadTemplate getClientPayloadTemplate(int custType) 
    {
        return (PayloadTemplate)customPayloadTemplates.get(new Integer(custType));
    }

    // ------------------------------------------------------------------------

    public int insertEvent(GeoEvent geoEvent) 
    {
       
        /* directory */
        File storeDir = DeviceDBImpl.getDataStoreDirectory();

        /* file */
        // "account$device.csv"
        StringBuffer sb = new StringBuffer();
        sb.append(this.getAccountName());
        sb.append("_");
        sb.append(this.getDeviceName());
        sb.append(".csv");
        File dataFile = new File(storeDir, sb.toString());
        
        /* extract */
        DateTime ts         = new DateTime(geoEvent.getTimestamp());
        int      statusCode = geoEvent.getStatusCode();
        String   statusDesc = StatusCodes.GetCodeDescription(statusCode);
        GeoPoint gp         = geoEvent.getGeoPoint();
        double   speed      = geoEvent.getSpeed();
        double   heading    = geoEvent.getHeading();
        double   altitude   = geoEvent.getAltitude();
        
        /* format */
        // YYYY/MM/DD,hh:mm:ss,<status>,<latitude>,<logitude>,<speed>,<heading>,<altitude>
        StringBuffer fmt = new StringBuffer();
        ts.format("yyyy/MM/dd,HH:mm:ss",fmt,null);  // local TimeZone
        //ts.gmtFormat("yyyy/MM/dd,HH:mm:ss",fmt);  // GMT TimeZone
        fmt.append(",");
        fmt.append(statusDesc);
        fmt.append(",");
        fmt.append(gp.getLatitudeString());
        fmt.append(",");
        fmt.append(gp.getLongitudeString());
        fmt.append(",");
        fmt.append(StringTools.format(speed, "0.0"));
        fmt.append(",");
        fmt.append(StringTools.format(heading, "0.0"));
        fmt.append(",");
        fmt.append(StringTools.format(altitude, "0.0"));
        fmt.append("\n");

        /* save */
        try {
            //Print.logDebug("Writing CSV record to file: " + dataFile);
            FileTools.writeFile(fmt.toString().getBytes(), dataFile, true);
            return ServerErrors.NAK_OK;
        } catch (IOException ioe) {
            Print.logException("Unable to save to file: " + dataFile, ioe);
            return ServerErrors.NAK_EVENT_ERROR;
        }
        
    }
    
    // ------------------------------------------------------------------------

}
