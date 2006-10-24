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
//      Added field formatting support for CSV output
// ----------------------------------------------------------------------------
package org.opendmtp.server_mysql.db;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opendmtp.util.*;
import org.opendmtp.codes.*;
import org.opendmtp.dbtools.*;

public class EventData
    extends DBRecord
{
    
    // ------------------------------------------------------------------------

    public static final boolean ASCENDING               = true;
    public static final boolean DESCENDING              = false;
    
    public static final int     LIMIT_TYPE_FIRST        = 0;
    public static final int     LIMIT_TYPE_LAST         = 1;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below
    
    /* table name */
    public static final String TABLE_NAME               = "EventData";

    /* field definition */
    public static final String FLD_accountID            = "accountID";
    public static final String FLD_deviceID             = "deviceID";
    public static final String FLD_timestamp            = "timestamp";
    public static final String FLD_statusCode           = "statusCode";
    public static final String FLD_dataSource           = "dataSource";
    public static final String FLD_rawData              = "rawData";
    public static final String FLD_latitude             = "latitude";
    public static final String FLD_longitude            = "longitude";
    public static final String FLD_speedKPH             = "speedKPH";
    public static final String FLD_heading              = "heading";
    public static final String FLD_altitude             = "altitude";
    public static final String FLD_distanceKM           = "distanceKM";
    public static final String FLD_topSpeedKPH          = "topSpeedKPH";
    public static final String FLD_geofenceID1          = "geofenceID1";
    public static final String FLD_geofenceID2          = "geofenceID2";
    private static DBField FieldInfo[] = {
        new DBField(FLD_accountID    , String.class  , DBField.TYPE_STRING(32)  , "title=Account_ID key=true"),
        new DBField(FLD_deviceID     , String.class  , DBField.TYPE_STRING(32)  , "title=Device_ID key=true"),
        new DBField(FLD_timestamp    , Long.TYPE     , DBField.TYPE_UINT32      , "title=Timestamp key=true"),
        new DBField(FLD_statusCode   , Integer.TYPE  , DBField.TYPE_UINT32      , "title=Status_Code key=true"),
        new DBField(FLD_dataSource   , String.class  , DBField.TYPE_STRING(32)  , "title=Data_Source"),
        new DBField(FLD_rawData      , String.class  , DBField.TYPE_TEXT        , "title=Raw_Data"),
        new DBField(FLD_latitude     , Double.TYPE   , DBField.TYPE_DOUBLE      , "title=Latitude format=#0.00000"),
        new DBField(FLD_longitude    , Double.TYPE   , DBField.TYPE_DOUBLE      , "title=Longitude format=#0.00000"),
        new DBField(FLD_speedKPH     , Double.TYPE   , DBField.TYPE_DOUBLE      , "title=Speed_KPH format=#0.0"),
        new DBField(FLD_heading      , Double.TYPE   , DBField.TYPE_DOUBLE      , "title=Heading format=#0.0"),
        new DBField(FLD_altitude     , Double.TYPE   , DBField.TYPE_DOUBLE      , "title=Altitude format=#0.0"),
        new DBField(FLD_distanceKM   , Double.TYPE   , DBField.TYPE_DOUBLE      , "title=Distance_KM format=#0.0"),
        new DBField(FLD_topSpeedKPH  , Double.TYPE   , DBField.TYPE_DOUBLE      , "title=Top_Speed_KPH format=#0.0"),
        new DBField(FLD_geofenceID1  , Long.TYPE     , DBField.TYPE_UINT32      , "title=Geofence_1"),
        new DBField(FLD_geofenceID2  , Long.TYPE     , DBField.TYPE_UINT32      , "title=Geofence_2"),
    };

    /* key class */
    public static class Key
        extends DBRecordKey
    {
        public Key() {
            super();
        }
        public Key(String acctId, String devId, long timestamp, long statusCode) {
            super.setFieldValue(FLD_accountID , acctId);
            super.setFieldValue(FLD_deviceID  , devId);
            super.setFieldValue(FLD_timestamp , timestamp);
            super.setFieldValue(FLD_statusCode, statusCode);
        }
        public DBFactory getFactory() {
            return EventData.getFactory();
        }
        public DBRecord getDBRecord() {
            EventData rcd = (EventData)super.getDBRecord();
            // init as needed
            return rcd;
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
                EventData.class, 
                EventData.Key.class);
        }
        return factory;
    }

    /* Bean instance */
    public EventData()
    {
        super();
    }

    /* database record */
    public EventData(EventData.Key key)
    {
        super(key);
        // init?
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

    public long getTimestamp()
    {
        Long v = (Long)this.getFieldValue(FLD_timestamp);
        return (v != null)? v.longValue() : 0L;
    }
    
    private void setTimestamp(long v)
    {
        this.setFieldValue(FLD_timestamp, v);
    }
    
    // ------------------------------------------------------------------------

    public int getStatusCode()
    {
        Integer v = (Integer)this.getFieldValue(FLD_statusCode);
        return (v != null)? v.intValue() : 0;
    }
    
    public String getStatusCodeString()
    {
        return StatusCodes.GetCodeDescription(this.getStatusCode());
    }
    
    private void setStatusCode(int v)
    {
        this.setFieldValue(FLD_statusCode, v);
    }

    // ------------------------------------------------------------------------

    public String getDataSource()
    {
        String v = (String)this.getFieldValue(FLD_dataSource);
        return (v != null)? v : "";
    }
    
    public void setDataSource(String v)
    {
        this.setFieldValue(FLD_dataSource, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    public String getRawData()
    {
        String v = (String)this.getFieldValue(FLD_rawData);
        return (v != null)? v : "";
    }
    
    public void setRawData(String v)
    {
        this.setFieldValue(FLD_rawData, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    public double getLatitude()
    {
        Double v = (Double)this.getFieldValue(FLD_latitude);
        return (v != null)? v.doubleValue() : 0.0;
    }
    
    public void setLatitude(double v)
    {
        this.setFieldValue(FLD_latitude, v);
    }
    
    public GeoPoint getGeoPoint()
    {
        return new GeoPoint(this.getLatitude(), this.getLongitude());
    }

    // ------------------------------------------------------------------------

    public double getLongitude()
    {
        Double v = (Double)this.getFieldValue(FLD_longitude);
        return (v != null)? v.doubleValue() : 0.0;
    }
    
    public void setLongitude(double v)
    {
        this.setFieldValue(FLD_longitude, v);
    }

    // ------------------------------------------------------------------------

    public double getSpeedKPH()
    {
        Double v = (Double)this.getFieldValue(FLD_speedKPH);
        return (v != null)? v.doubleValue() : 0.0;
    }
    
    public void setSpeedKPH(double v)
    {
        this.setFieldValue(FLD_speedKPH, v);
    }

    // ------------------------------------------------------------------------

    public double getHeading()
    {
        Double v = (Double)this.getFieldValue(FLD_heading);
        return (v != null)? v.doubleValue() : 0.0;
    }
    
    public void setHeading(double v)
    {
        this.setFieldValue(FLD_heading, v);
    }

    // ------------------------------------------------------------------------

    public double getAltitude()
    {
        Double v = (Double)this.getFieldValue(FLD_altitude);
        return (v != null)? v.doubleValue() : 0.0;
    }
    
    public void setAltitude(double v)
    {
        this.setFieldValue(FLD_altitude, v);
    }

    // ------------------------------------------------------------------------

    public double getDistanceKM()
    {
        Double v = (Double)this.getFieldValue(FLD_distanceKM);
        return (v != null)? v.doubleValue() : 0.0;
    }
    
    public void setDistanceKM(double v)
    {
        this.setFieldValue(FLD_distanceKM, v);
    }

    // ------------------------------------------------------------------------

    public double getTopSpeedKPH()
    {
        Double v = (Double)this.getFieldValue(FLD_topSpeedKPH);
        return (v != null)? v.doubleValue() : 0.0;
    }
    
    public void setTopSpeedKPH(double v)
    {
        this.setFieldValue(FLD_topSpeedKPH, v);
    }

    // ------------------------------------------------------------------------

    public long getGeofenceID1()
    {
        Long v = (Long)this.getFieldValue(FLD_geofenceID1);
        return (v != null)? v.longValue() : 0L;
    }
    
    public void setGeofenceID1(long v)
    {
        this.setFieldValue(FLD_geofenceID1, v);
    }

    // ------------------------------------------------------------------------

    public long getGeofenceID2()
    {
        Long v = (Long)this.getFieldValue(FLD_geofenceID2);
        return (v != null)? v.longValue() : 0L;
    }
    
    public void setGeofenceID2(long v)
    {
        this.setFieldValue(FLD_geofenceID2, v);
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public String formatAsCSVRecord(String fields[])
    {
        StringBuffer sb = new StringBuffer();
        if (fields != null) {
            //DBFactory fact = EventData.getFactory();
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) { sb.append(","); }
                DBField dbFld = this.getRecordKey().getField(fields[i]);
                Object val = (dbFld != null)? this.getFieldValue(fields[i]) : null;
                if (val != null) {
                    Class typeClass = dbFld.getTypeClass();
                    if (fields[i].equals(FLD_timestamp)) {
                        long time = ((Long)val).longValue();
                        DateTime dt = new DateTime(time);
                        sb.append(dt.gmtFormat("yyyy/MM/dd,HH:mm:ss"));
                    } else 
                    if (fields[i].equals(FLD_statusCode)) {
                        int code = ((Integer)val).intValue();
                        StatusCodes.Code c = StatusCodes.GetCode(code);
                        if (c != null) {
                            sb.append(c.getDescription());
                        } else {
                            sb.append("0x" + StringTools.toHexString(code));
                        }
                    } else 
                    if ((typeClass == Double.class) || (typeClass == Double.TYPE)) {
                        double d = ((Double)val).doubleValue();
                        String fmt = dbFld.getFormat();
                        if ((fmt != null) && !fmt.equals("")) {
                            sb.append(StringTools.format(d,fmt));
                        } else {
                            sb.append(String.valueOf(d));
                        }
                    } else {
                        sb.append(val.toString());
                    }
                }
            }
        }
        return sb.toString();
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    // MySQL: where ( <Condition...> )
    public static StringBuffer getWhereClause(StringBuffer wh,
        String acctId, String devId,
        long timeStart, long timeEnd, 
        int statCode[], 
        boolean gpsRequired, 
        String andSelect)
    {
        // see SelectionConstraints
        if (wh == null) { wh = new StringBuffer(); }
        
        /* where clause */
        wh.append(" WHERE (");
        
        /* Account/Device */
        wh.append("(");
        wh.append(FLD_accountID).append("='").append(acctId).append("'");
        wh.append(" AND ");
        wh.append(FLD_deviceID).append("='").append(devId).append("'");
        wh.append(")");

        /* status code(s) */
        if ((statCode != null) && (statCode.length > 0)) {
            wh.append(" AND (");
            for (int i = 0; i < statCode.length; i++) {
                if (i > 0) { wh.append(" OR "); }
                wh.append(FLD_statusCode).append("=").append(statCode[i]);
            }
            wh.append(")");
        }
        
        /* gps required */
        if (gpsRequired) {
            // This section states that if either of the latitude/longitude are '0',
            // then do not include the record in the select.  This may not be valid
            // for all circumstances and may need better fine tuning.
            wh.append(" AND (");
            wh.append(FLD_latitude).append("!=").append("0");
            wh.append(" AND ");
            wh.append(FLD_longitude).append("!=").append("0");
            wh.append(")");
        }
        
        /* event time */
        if (timeStart >= 0L) {
            wh.append(" AND ");
            wh.append(FLD_timestamp).append(">=").append(timeStart);
        }
        if ((timeEnd >= 0L) && (timeEnd >= timeStart)) {
            wh.append(" AND ");
            wh.append(FLD_timestamp).append("<=").append(timeEnd);
        }
        
        /* additional selection */
        if (andSelect != null) {
            wh.append(" AND ").append(andSelect);
        }
        
        /* end of where */
        wh.append(")");
        return wh;
        
    }
    
    // ------------------------------------------------------------------------

    // MySQL: select * from EventData <Where> order by <FLD_timestamp> desc limit <Limit>
    public static EventData[] getRangeEvents(
        String acctId, String devId,
        long timeStart, long timeEnd,
        int limitType, long limit)
        throws DBException
    {

        /* invalid account/device */
        if ((acctId == null) || acctId.equals("")) {
            return new EventData[0];
        } else
        if ((devId == null) || devId.equals("")) {
            return new EventData[0];
        }

        /* invalid time range */
        if ((timeStart > 0L) && (timeEnd > 0L) && (timeStart > timeEnd)) {
            return new EventData[0];
        }
        
        /* where clause */
        StringBuffer wh = new StringBuffer();
        EventData.getWhereClause(wh,
            acctId, devId,
            timeStart, timeEnd,
            null  /*statCode[]*/,
            false /*gpsRequired*/,
            null  /*andSelect*/);
        
        /* sorted */
        wh.append(" ORDER BY ").append(FLD_timestamp);
        
        /* descending */
        if ((limitType == LIMIT_TYPE_LAST) && (limit > 0)) {
            // NOTE: records will be in descending order (will need to reorder)
            wh.append(" DESC");
        }

        /* limit */
        if (limit > 0) {
            wh.append(" LIMIT " + limit); 
        }
        
        /* get events */
        EventData ae[] = null;
        try {
            DBRecord.lockTables(new String[] { TABLE_NAME }, null);
            ae = (EventData[])DBRecord.select(EventData.getFactory(), wh.toString());
        } finally {
            DBRecord.unlockTables();
        }
        if (ae == null) {
            // no records
            return new EventData[0];
        } else
        if (limitType == LIMIT_TYPE_FIRST) {
            // records are in ascending order
            return ae;
        } else {
            // reorder records to ascending order
            int lastNdx = ae.length - 1;
            for (int i = 0; i < ae.length / 2; i++) {
                EventData ed = ae[i];
                ae[i] = ae[lastNdx - i];
                ae[lastNdx - i] = ed;
            }
            return ae;
        }

    }
    
    // ------------------------------------------------------------------------

    public static long getRecordCount(
        String acctId, String devId,
        long timeStart, long timeEnd)
        throws DBException
    {
        StringBuffer wh = new StringBuffer();
        EventData.getWhereClause(wh,
            acctId, devId,
            timeStart, timeEnd,
            null /*statCode[]*/,
            false /*gpsRequired*/,
            null /*andSelect*/);
        try {
            return DBRecord.getRecordCount(EventData.getFactory(), wh);
        } catch (SQLException sqe) {
            throw new DBException("Getting record count", sqe);
        }
    }
 
}
