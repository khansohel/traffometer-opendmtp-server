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
//  2006/04/09  Martin D. Flynn
//      Integrate DBException
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

public class DeviceDBImpl
    implements DeviceDB
{
    
    // ------------------------------------------------------------------------

    private Device device = null;
    
    public DeviceDBImpl(Device dev) 
    {
        this.device = dev;
    }
    
    // ------------------------------------------------------------------------

    public String getAccountName() 
    {
        return this.device.getAccountID();
    }
        
    public String getDeviceName() 
    {
        return this.device.getDeviceID();
    }
    
    public String getDescription() 
    {
        return this.device.getDescription();
    }
    
    public boolean isActive() 
    {
        return this.device.getIsActive();
    }
    
    // ------------------------------------------------------------------------

    public int getMaxAllowedEvents()
    {
        return this.device.getMaxAllowedEvents();
    }
    
    public long getEventCount(long timeStart, long timeEnd)
    {
        try {
            long count = EventData.getRecordCount(
                this.getAccountName(), this.getDeviceName(),
                timeStart, timeEnd);
            return count;
        } catch (DBException dbe) {
            dbe.printException();
            return -1L;
        }
    }
    
    // ------------------------------------------------------------------------

    public int getLimitTimeInterval()
    {
        return this.device.getUnitLimitInterval();
    }
    
    // ------------------------------------------------------------------------

    public int getMaxTotalConnections() 
    {
        return this.device.getTotalMaxConn();
    }
    
    public int getMaxTotalConnectionsPerMinute() 
    {
        return this.device.getTotalMaxConnPerMin();
    }
    
    public byte[] getTotalConnectionProfile()
    {
        DTProfileMask v = this.device.getTotalProfileMask();
        return (v != null)? v.getByteMask() : new byte[0];
    }
    
    public void setTotalConnectionProfile(byte[] profile) 
    {
        DTProfileMask mask = new DTProfileMask(profile);
        mask.setLimitTimeInterval(this.getLimitTimeInterval());
        this.device.setTotalProfileMask(mask);
    }

    public long getLastTotalConnectionTime()
    {
        return this.device.getLastTotalConnectTime();
    }

    public void setLastTotalConnectionTime(long time)
    {
        this.device.setLastTotalConnectTime(time);
    }

    // ------------------------------------------------------------------------

    public int getMaxDuplexConnections() 
    {
        return this.device.getDuplexMaxConn();
    }
    
    public int getMaxDuplexConnectionsPerMinute()
    {
        return this.device.getDuplexMaxConnPerMin();
    }
    
    public byte[] getDuplexConnectionProfile()
    {
        DTProfileMask v = this.device.getDuplexProfileMask();
        return (v != null)? v.getByteMask() : new byte[0];
    }
    
    public void setDuplexConnectionProfile(byte[] profile)
    {
        DTProfileMask mask = new DTProfileMask(profile);
        mask.setLimitTimeInterval(this.getLimitTimeInterval());
        this.device.setDuplexProfileMask(mask);
    }

    public long getLastDuplexConnectionTime()
    {
        return this.device.getLastDuplexConnectTime();
    }

    public void setLastDuplexConnectionTime(long time)
    {
        this.device.setLastDuplexConnectTime(time);
    }

    // ------------------------------------------------------------------------

    public boolean supportsEncoding(int encoding)
    {
        // 'encoding' is a mask containing one (or more) of the following:
        //    Encoding.SUPPORTED_ENCODING_BINARY
        //    Encoding.SUPPORTED_ENCODING_BASE64
        //    Encoding.SUPPORTED_ENCODING_HEX
        //    Encoding.SUPPORTED_ENCODING_CSV
        int vi = this.device.getSupportedEncodings();
        return ((vi & encoding) != 0);
    }
    
    public void removeEncoding(int encoding) 
    {
        int vi = this.device.getSupportedEncodings();
        if ((vi & encoding) != 0) {
            vi &= ~encoding;
            this.device.setSupportedEncodings(vi);
        }
    }
    
    // ------------------------------------------------------------------------

    public boolean addClientPayloadTemplate(PayloadTemplate template)
    {
        return EventTemplate.SetPayloadTemplate(
            this.getAccountName(),
            this.getDeviceName(),
            template);
    }
    
    public PayloadTemplate getClientPayloadTemplate(int custType) 
    {
        return EventTemplate.GetPayloadTemplate(
            this.getAccountName(),
            this.getDeviceName(),
            custType);
    }
    
    // ------------------------------------------------------------------------

    public int insertEvent(GeoEvent geoEvent) 
    {
        
        /* create key */
        EventData.Key evKey = new EventData.Key(
            this.getAccountName(),
            this.getDeviceName(),
            geoEvent.getTimestamp(),
            geoEvent.getStatusCode());
            
        /* populate record */
        EventData evdb = (EventData)evKey.getDBRecord();
        evdb.setFieldValue(EventData.FLD_dataSource  , geoEvent.getDataSource());
        evdb.setFieldValue(EventData.FLD_rawData     , geoEvent.getRawData());
        evdb.setFieldValue(EventData.FLD_latitude    , geoEvent.getLatitude());
        evdb.setFieldValue(EventData.FLD_longitude   , geoEvent.getLongitude());
        evdb.setFieldValue(EventData.FLD_speedKPH    , geoEvent.getSpeed());
        evdb.setFieldValue(EventData.FLD_heading     , geoEvent.getHeading());
        evdb.setFieldValue(EventData.FLD_altitude    , geoEvent.getAltitude());
        evdb.setFieldValue(EventData.FLD_distanceKM  , geoEvent.getDistance());
        evdb.setFieldValue(EventData.FLD_topSpeedKPH , geoEvent.getTopSpeed());
        evdb.setFieldValue(EventData.FLD_geofenceID1 , geoEvent.getGeofence(0));
        evdb.setFieldValue(EventData.FLD_geofenceID2 , geoEvent.getGeofence(1));
        
        /* save */
        try {
            evdb.save();
        } catch (DBException dbe) {
            return ServerErrors.NAK_EVENT_ERROR;
        }
        
        /* check rules and return */
        this.device.checkEventRules(evdb);
        return ServerErrors.NAK_OK;
        
    }
   
    // ------------------------------------------------------------------------

    public String toString()
    {
        return (this.device != null)? this.device.toString() : "";
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private static final int DEFAULT_ENCODING = Encoding.SUPPORTED_ENCODING_BINARY | Encoding.SUPPORTED_ENCODING_BASE64 | Encoding.SUPPORTED_ENCODING_HEX;
    private static final int DEFAULT_UNIT_LIMIT_INTERVAL = 60;
    private static final int DEFAULT_MAX_ALLOWED_EVENTS = 21;
    private static final int DEFAULT_TOTAL_MAX_CONNECTIONS = 10;
    private static final int DEFAULT_TOTAL_MAX_CONNECTIONS_PER_MIN = 2;
    private static final int DEFAULT_DUPLEX_MAX_CONNECTIONS = 6;
    private static final int DEFAULT_DUPLEX_MAX_CONNECTIONS_PER_MIN = 1;
    
    private static Device getDevice(String acctID, String devID, boolean create)
        throws DBException
    {
        // does not return null
        
        /* account-id specified? */
        if ((acctID == null) || acctID.equals("")) {
            throw new DBException("Account-ID not specified.");
        }
        
        /* device-id specified? */
        if ((devID == null) || devID.equals("")) {
            throw new DBException("Device-ID not specified for account: " + acctID);
        }

        /* get/create */
        Device dev = null;
        Device.Key devKey = new Device.Key(acctID, devID);
        if (!devKey.exists()) {
            if (create) {
                dev = (Device)devKey.getDBRecord();
                dev.setIsActive(true);
                dev.setDescription("New Device");
                dev.setSupportedEncodings(DEFAULT_ENCODING);
                dev.setTotalMaxConn(DEFAULT_TOTAL_MAX_CONNECTIONS);
                dev.setDuplexMaxConn(DEFAULT_DUPLEX_MAX_CONNECTIONS);
                dev.setUnitLimitInterval(DEFAULT_UNIT_LIMIT_INTERVAL);
                dev.setTotalMaxConnPerMin(DEFAULT_TOTAL_MAX_CONNECTIONS_PER_MIN);
                dev.setDuplexMaxConnPerMin(DEFAULT_DUPLEX_MAX_CONNECTIONS_PER_MIN);
                dev.setMaxAllowedEvents(DEFAULT_MAX_ALLOWED_EVENTS);
                return dev;
            } else {
                throw new DBException("Device-ID does not exists: " + devKey);
            }
        } else {
            dev = Device.getDevice(acctID, devID);
            if (dev == null) {
                throw new DBException("Unable to read existing Device-ID: " + devKey);
            }
            return dev;
        }
        
    }
    
    // ------------------------------------------------------------------------

    public static void createNewDevice(String acctID, String devID)
        throws DBException
    {
        Device dev = DeviceDBImpl.getDevice(acctID, devID, true);
        dev.save();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String ARG_ACCOUNT = "account";
    private static final String ARG_DEVICE  = "device";
    private static final String ARG_CREATE  = "create";
    private static final String ARG_EDIT    = "edit";
    private static final String ARG_EVENTS  = "events";
    private static final String ARG_OUTPUT  = "output";
    private static final String ARG_FORMAT  = "format";

    private static void usage()
    {
        Print.logInfo("Usage:");
        Print.logInfo("  java ... " + DeviceDBImpl.class.getName() + " {options}");
        Print.logInfo("Common Options:");
        Print.logInfo("  -account=<id>                  Acount ID which owns Device");
        Print.logInfo("  -device=<id>                   Device ID to create/edit");
        Print.logInfo("  -create                        Create a new Device");
        Print.logInfo("  -edit                          Edit an existing (or newly created) Device");
        System.exit(1);
    }

    public static void main(String argv[])
    {
        DBConfig.init(argv,true);
        // Commands:
        //   { -create | -edit | -events }
        //   -account=<name>
        //   -device=<name>
        String acctID  = RTConfig.getString(new String[] { "acct", ARG_ACCOUNT }, "");
        String devID   = RTConfig.getString(new String[] { "dev" , ARG_DEVICE  }, "");

        /* account-id specified? */
        if ((acctID == null) || acctID.equals("")) {
            Print.logError("Account-ID not specified.");
            usage();
        }

        /* get account */
        Account acct = null;
        try {
            acct = Account.getAccount(acctID); // may return DBException
            if (acct == null) {
                Print.logError("Account-ID does not exist: " + acctID);
                usage();
            }
        } catch (DBException dbe) {
            Print.logException("Error loading Account: " + acctID, dbe);
            //dbe.printException();
            System.exit(99);
        }

        /* device-id specified? */
        if ((devID == null) || devID.equals("")) {
            Print.logError("Device-ID not specified.");
            usage();
        }
        
        /* device exists? */
        boolean deviceExists = false;
        try {
            deviceExists = Device.exists(acctID, devID);
        } catch (DBException dbe) {
            Print.logError("Error determining if DEvice exists: " + acctID + "," + devID);
            System.exit(99);
        }
        
        /* option count */
        int opts = 0;

        /* create */
        if (RTConfig.getBoolean(ARG_CREATE, false)) {
            opts++;
            if (deviceExists) {
                Print.logWarn("Device already exists '" + acctID + ":" + devID + "'");
            } else {
                try {
                    DeviceDBImpl.createNewDevice(acctID, devID);
                    Print.logInfo("Created Device '" + acctID + ":" + devID + "'");
                } catch (DBException dbe) {
                    Print.logError("Error creating Device: " + acctID);
                    dbe.printException();
                    System.exit(99);
                }
            }
        }

        /* edit */
        if (RTConfig.getBoolean(ARG_EDIT, false)) {
            opts++;
            if (!deviceExists) {
                Print.logError("Device does not exist '" + acctID + ":" + devID + "'");
            } else {
                try {
                    Device device = DeviceDBImpl.getDevice(acctID,devID,false); // may throw DBException
                    DBEdit editor = new DBEdit(device);
                    editor.edit(); // may throw IOException
                } catch (IOException ioe) {
                    if (ioe instanceof EOFException) {
                        Print.logError("End of input");
                    } else {
                        Print.logError("IO Error");
                    }
                } catch (DBException dbe) {
                    Print.logError("Error editing Device '" + acctID + "'");
                    dbe.printException();
                }
            }
            System.exit(0);
        }
        
        /* no options specified */
        if (opts == 0) {
            usage();
        }
        
    }

}
