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
// ----------------------------------------------------------------------------
package org.opendmtp.server_mysql.db;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opendmtp.util.*;

import org.opendmtp.dbtools.*;
import org.opendmtp.server_mysql.dbtypes.*;
import org.opendmtp.server_mysql.*;

public class Device
    extends DBRecord
{
    
    // ------------------------------------------------------------------------
    
    public static final int     LIMIT_TYPE_FIRST        = EventData.LIMIT_TYPE_FIRST;
    public static final int     LIMIT_TYPE_LAST         = EventData.LIMIT_TYPE_LAST;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    public static final String  TABLE_NAME               = "Device";

    /* field definition */
    public static final String FLD_accountID             = "accountID";
    public static final String FLD_deviceID              = "deviceID";
    public static final String FLD_uniqueID              = "uniqueID";
    public static final String FLD_description           = "description";
    public static final String FLD_serialNumber          = "serialNumber";
    public static final String FLD_isActive              = "isActive";
    public static final String FLD_notifyEmail           = "notifyEmail";
    public static final String FLD_supportedEncodings    = "supportedEncodings";
    public static final String FLD_unitLimitInterval     = "unitLimitInterval";
    public static final String FLD_maxAllowedEvents      = "maxAllowedEvents";
    public static final String FLD_lastTotalConnectTime  = "lastTotalConnectTime";
    public static final String FLD_totalProfileMask      = "totalProfileMask";
    public static final String FLD_totalMaxConn          = "totalMaxConn";
    public static final String FLD_totalMaxConnPerMin    = "totalMaxConnPerMin";
    public static final String FLD_lastDuplexConnectTime = "lastDuplexConnectTime";
    public static final String FLD_duplexProfileMask     = "duplexProfileMask";
    public static final String FLD_duplexMaxConn         = "duplexMaxConn";
    public static final String FLD_duplexMaxConnPerMin   = "duplexMaxConnPerMin";
    private static DBField FieldInfo[] = {
        new DBField(FLD_accountID            , String.class        , DBField.TYPE_STRING(32)  , "title=Account_ID key=true"),
        new DBField(FLD_deviceID             , String.class        , DBField.TYPE_STRING(32)  , "title=Device_ID key=true"),
        new DBField(FLD_uniqueID             , DTUniqueID.class    , DBField.TYPE_UINT64      , "title=Unique_ID altkey=true"),
        new DBField(FLD_description          , String.class        , DBField.TYPE_STRING(128) , "title=Description edit=2"),
        new DBField(FLD_serialNumber         , String.class        , DBField.TYPE_STRING(24)  , "title=Serial_Number edit=2"),
        new DBField(FLD_isActive             , Boolean.TYPE        , DBField.TYPE_BOOLEAN     , "title=Is_Active edit=2"),
        new DBField(FLD_notifyEmail          , String.class        , DBField.TYPE_STRING(128) , "title=Notification_EMail_Address edit=2"),
        new DBField(FLD_supportedEncodings   , Integer.TYPE        , DBField.TYPE_UINT8       , "title=Supported_Encodings edit=2"),
        new DBField(FLD_unitLimitInterval    , Integer.TYPE        , DBField.TYPE_UINT16      , "title=Accounting_Time_Interval_Minutes edit=2"),
        new DBField(FLD_maxAllowedEvents     , Integer.TYPE        , DBField.TYPE_UINT16      , "title=Max_Events_per_Interval edit=2"),
        new DBField(FLD_lastTotalConnectTime , Long.TYPE           , DBField.TYPE_UINT32      , "title=Last_Total_Connect_Time edit=2"),
        new DBField(FLD_totalProfileMask     , DTProfileMask.class , DBField.TYPE_BINARY      , "title=Total_Profile_Mask"),
        new DBField(FLD_totalMaxConn         , Integer.TYPE        , DBField.TYPE_UINT16      , "title=Max_Total_Connections_per_Interval edit=2"),
        new DBField(FLD_totalMaxConnPerMin   , Integer.TYPE        , DBField.TYPE_UINT16      , "title=Max_Total_Connections_per_Minute edit=2"),
        new DBField(FLD_lastDuplexConnectTime, Long.TYPE           , DBField.TYPE_UINT32      , "title=Last_Duplex_Connect_Time"),
        new DBField(FLD_duplexProfileMask    , DTProfileMask.class , DBField.TYPE_BINARY      , "title=Duplex_Profile_Mask"),
        new DBField(FLD_duplexMaxConn        , Integer.TYPE        , DBField.TYPE_UINT16      , "title=Max_Duplex_Connections_per_Interval edit=2"),
        new DBField(FLD_duplexMaxConnPerMin  , Integer.TYPE        , DBField.TYPE_UINT16      , "title=Max_Duplex_Connections_per_Minute edit=2"),
        newField_lastUpdateTime(),
    };

    /* key class */
    public static class Key
        extends DBRecordKey
    {
        public Key() {
            super();
        }
        public Key(String acctId, String devId) {
            super.setFieldValue(FLD_accountID, acctId);
            super.setFieldValue(FLD_deviceID , devId);
        }
        public DBFactory getFactory() {
            return Device.getFactory();
        }
    }

    /* factory constructor */
    private static DBFactory factory = null;
    public static DBFactory getFactory()
    {
        if (factory == null) {
            factory = new DBFactory(TABLE_NAME, 
                FieldInfo, 
                KEY_PRIMARY, 
                Device.class, 
                Device.Key.class);
        }
        return factory;
    }

    /* Bean instance */
    public Device()
    {
        super();
    }

    /* database record */
    public Device(Device.Key key)
    {
        super(key);
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below

    public String getAccountID()
    {
        String v = (String)this.getFieldValue(FLD_accountID);
        return (v != null)? v : "";
    }
    
    private void setAccountID(String v)
    {
        this.setFieldValue(FLD_accountID, ((v != null)? v : ""));
    }
    
    // ------------------------------------------------------------------------

    public String getDeviceID()
    {
        String v = (String)this.getFieldValue(FLD_deviceID);
        return (v != null)? v : "";
    }
    
    private void setDeviceID(String v)
    {
        this.setFieldValue(FLD_deviceID, ((v != null)? v : ""));
    }
    
    // ------------------------------------------------------------------------

    public DTUniqueID getUniqueID()
    {
        DTUniqueID v = (DTUniqueID)this.getFieldValue(FLD_uniqueID);
        return v;
    }
    
    private void setUniqueID(DTUniqueID v)
    {
        this.setFieldValue(FLD_uniqueID, v);
    }

    // ------------------------------------------------------------------------

    public String getDescription()
    {
        String v = (String)this.getFieldValue(FLD_description);
        return (v != null)? v : "";
    }

    public void setDescription(String v)
    {
        this.setFieldValue(FLD_description, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    public String getSerialNumber()
    {
        String v = (String)this.getFieldValue(FLD_serialNumber);
        return (v != null)? v : "";
    }

    public void setSerialNumber(String v)
    {
        this.setFieldValue(FLD_serialNumber, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    public boolean getIsActive()
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_isActive);
        return (v != null)? v.booleanValue() : false;
    }

    public void setIsActive(boolean v)
    {
        this.setFieldValue(FLD_isActive, v);
    }

    // ------------------------------------------------------------------------

    public String getNotifyEmail()
    {
        String v = (String)this.getFieldValue(FLD_notifyEmail);
        return (v != null)? v : "";
    }

    public void setNotifyEmail(String v)
    {
        this.setFieldValue(FLD_notifyEmail, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    public int getSupportedEncodings()
    {
        Integer v = (Integer)this.getFieldValue(FLD_supportedEncodings);
        return (v != null)? v.intValue() : 0;
    }
    
    public void setSupportedEncodings(int encoding)
    {
        this.setFieldValue(FLD_supportedEncodings, encoding);
    }
    
    public void addEncoding(int encoding)
    {
        int vi = this.getSupportedEncodings();
        if ((vi & encoding) != encoding) {
            vi |= encoding;
            this.setSupportedEncodings(vi);
        }
    }

    // ------------------------------------------------------------------------

    public long getLastTotalConnectTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastTotalConnectTime);
        return (v != null)? v.longValue() : 0L;
    }
    
    public void setLastTotalConnectTime(long v)
    {
        this.setFieldValue(FLD_lastTotalConnectTime, v);
    }

    // ------------------------------------------------------------------------

    public long getLastDuplexConnectTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastDuplexConnectTime);
        return (v != null)? v.longValue() : 0L;
    }
    
    public void setLastDuplexConnectTime(long v)
    {
        this.setFieldValue(FLD_lastDuplexConnectTime, v);
    }

    // ------------------------------------------------------------------------

    public int getUnitLimitInterval()
    {
        Integer v = (Integer)this.getFieldValue(FLD_unitLimitInterval);
        return (v != null)? v.intValue() : 0;
    }

    public void setUnitLimitInterval(int v)
    {
        this.setFieldValue(FLD_unitLimitInterval, v);
    }

    // ------------------------------------------------------------------------

    public int getMaxAllowedEvents()
    {
        Integer v = (Integer)this.getFieldValue(FLD_maxAllowedEvents);
        return (v != null)? v.intValue() : 1;
    }

    public void setMaxAllowedEvents(int max)
    {
        this.setFieldValue(FLD_maxAllowedEvents, max);
    }

    // ------------------------------------------------------------------------

    public DTProfileMask getTotalProfileMask()
    {
        DTProfileMask v = (DTProfileMask)this.getFieldValue(FLD_totalProfileMask);
        return v;
    }

    public void setTotalProfileMask(DTProfileMask v)
    {
        this.setFieldValue(FLD_totalProfileMask, v);
    }

    // ------------------------------------------------------------------------

    public int getTotalMaxConn()
    {
        Integer v = (Integer)this.getFieldValue(FLD_totalMaxConn);
        return (v != null)? v.intValue() : 0;
    }

    public void setTotalMaxConn(int v)
    {
        this.setFieldValue(FLD_totalMaxConn, v);
    }

    // ------------------------------------------------------------------------

    public int getTotalMaxConnPerMin()
    {
        Integer v = (Integer)this.getFieldValue(FLD_totalMaxConnPerMin);
        return (v != null)? v.intValue() : 0;
    }

    public void setTotalMaxConnPerMin(int v)
    {
        this.setFieldValue(FLD_totalMaxConnPerMin, v);
    }

    // ------------------------------------------------------------------------

    public DTProfileMask getDuplexProfileMask()
    {
        DTProfileMask v = (DTProfileMask)this.getFieldValue(FLD_duplexProfileMask);
        return v;
    }

    public void setDuplexProfileMask(DTProfileMask v)
    {
        this.setFieldValue(FLD_duplexProfileMask, v);
    }

    // ------------------------------------------------------------------------

    public int getDuplexMaxConn()
    {
        Integer v = (Integer)this.getFieldValue(FLD_duplexMaxConn);
        return (v != null)? v.intValue() : 0;
    }

    public void setDuplexMaxConn(int max)
    {
        this.setFieldValue(FLD_duplexMaxConn, max);
    }

    // ------------------------------------------------------------------------

    public int getDuplexMaxConnPerMin()
    {
        Integer v = (Integer)this.getFieldValue(FLD_duplexMaxConnPerMin);
        return (v != null)? v.intValue() : 0;
    }

    public void setDuplexMaxConnPerMin(int max)
    {
        this.setFieldValue(FLD_duplexMaxConnPerMin, max);
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* check new event for specific rule triggers */
    public void checkEventRules(EventData evdb)
    {
        // Any special event rules checking should go here.
        // For instance, email can be sent using "org.opendmtp.util.SendMail"
        // based on trigger which have occur due to the receipt of a particular
        // type of event.
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* return events in specified time range */
    public EventData[] getRangeEvents(
        long timeStart, long timeEnd, 
        int limitType, long limit)
        throws DBException
    {
        return EventData.getRangeEvents(
            this.getAccountID(), this.getDeviceID(),
            timeStart, timeEnd,
            limitType, limit);
    }
    
    /* return the most recent 'limit' events */
    public EventData[] getLatestEvents(long limit)
        throws DBException
    {
        long startTime = -1L;
        long endTime   = -1L;
        return EventData.getRangeEvents(
            this.getAccountID(), this.getDeviceID(),
            startTime, endTime, 
            LIMIT_TYPE_LAST, limit);
    }
    
    // ------------------------------------------------------------------------

    public String toString()
    {
        return this.getAccountID() + "/" + this.getDeviceID();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static boolean exists(String acctID, String devID)
        throws DBException // if error occurs while testing existance
    {
        if ((acctID != null) && (devID != null)) {
            Device.Key devKey = new Device.Key(acctID, devID);
            return devKey.exists();
        }
        return false;
    }
    
    public static Device getDevice(String acctID, String devID)
        throws DBException
    {
        if ((acctID != null) && (devID != null)) {
            Device.Key key = new Device.Key(acctID, devID);
            if (key.exists()) {
                return (Device)key.getDBRecord(true);
            }
        }
        return null;
    }

    public static Device getDevice(long uniqID)
        throws DBException
    {
        
        /* read asset for unique-id */
        Device device = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
        
            /* select */
            StringBuffer sel = new StringBuffer();
            // MySQL: SELECT * FROM <TableName> WHERE (uniqueID='<UniqueID>') LIMIT 1
            sel.append("SELECT * FROM ").append(Device.TABLE_NAME).append(" ");
            sel.append("WHERE (");
            sel.append(  FLD_uniqueID).append("='").append(uniqID).append("'");
            sel.append(") LIMIT 1");
            // Note: The index on the column FLD_uniqueID does not enforce uniqueness
            // (since null/empty values are allowed and needed)
    
            /* get records */
            stmt = DBConnection.getDefaultConnection().execute(sel.toString());
            rs = stmt.getResultSet();
            while (rs.next()) {
                String acctId = rs.getString(FLD_accountID);
                String devId  = rs.getString(FLD_deviceID);
                device = new Device(new Device.Key(acctId,devId));
                device.setFieldValues(rs);
                break; // only one record
            }
            
        } catch (SQLException sqe) {
            throw new DBException("Get Device UniqueID", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
        }

        return device;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static void main(String args[])
    {
        DBConfig.init(args,true);
        Device.Key key = new Device.Key("opendmtp", "mobile");
        DBEdit dbEdit = new DBEdit(key);
        dbEdit.print();
    }
    
}
