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

import java.sql.SQLException;

import org.opendmtp.codes.StatusCodes;
import org.opendmtp.dbtools.DBException;
import org.opendmtp.dbtools.DBFactory;
import org.opendmtp.dbtools.DBField;
import org.opendmtp.dbtools.DBRecord;
import org.opendmtp.dbtools.DBRecordKey;
import org.opendmtp.util.DateTime;
import org.opendmtp.util.GeoPoint;
import org.opendmtp.util.StringTools;

/**
 * Provides a wrapper around the MySQL details of a record in the database recording
 * a GPS event.
 * 
 * @author Martin D. Flynn
 * @author Robert S. Brewer
 */
public class EventData extends DBRecord {

  // ------------------------------------------------------------------------

  /**
   * Apparently used to specify sort direction, but nothing actually references
   * this field.
   */
  public static final boolean ASCENDING = true;
  /**
   * Apparently used to specify sort direction, but nothing actually references
   * this field.
   */
  public static final boolean DESCENDING = false;

  
  /**
   * Used to specify the limit type of SQL queries. Something about descending order?  
   */
  public static final int LIMIT_TYPE_FIRST = 0;
  /**
   * Used to specify the limit type of SQL queries. Something about ascending order?  
   */
  public static final int LIMIT_TYPE_LAST = 1;

  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------
  // SQL table definition below

  /**
   * Name for SQL table.
   */
  public static final String TABLE_NAME = "EventData";

  /* field definition */
  /**
   * Name of database field holding account ID.
   */
  public static final String FLD_accountID = "accountID";
  /**
   * Name of database field holding device ID.
   */
  public static final String FLD_deviceID = "deviceID";
  /**
   * Name of database field holding timestamp.
   */
  public static final String FLD_timestamp = "timestamp";
  /**
   * Name of database field holding status code.
   */
  public static final String FLD_statusCode = "statusCode";
  /**
   * Name of database field holding data source.
   */
  public static final String FLD_dataSource = "dataSource";
  /**
   * Name of database field holding raw data.
   */
  public static final String FLD_rawData = "rawData";
  /**
   * Name of database field holding latitude.
   */
  public static final String FLD_latitude = "latitude";
  /**
   * Name of database field holding longitude.
   */
  public static final String FLD_longitude = "longitude";
  /**
   * Name of database field holding speed in kph.
   */
  public static final String FLD_speedKPH = "speedKPH";
  /**
   * Name of database field holding heading.
   */
  public static final String FLD_heading = "heading";
  /**
   * Name of database field holding altitude.
   */
  public static final String FLD_altitude = "altitude";
  /**
   * Name of database field holding distance in km.
   */
  public static final String FLD_distanceKM = "distanceKM";
  /**
   * Name of database field holding top speed recorded in kph.
   */
  public static final String FLD_topSpeedKPH = "topSpeedKPH";
  /**
   * Name of database field holding GeoFence #1.
   */
  public static final String FLD_geofenceID1 = "geofenceID1";
  /**
   * Name of database field holding GeoFence #2.
   */
  public static final String FLD_geofenceID2 = "geofenceID2";
  
  private static DBField FieldInfo[] = {
      new DBField(FLD_accountID, String.class, DBField.TYPE_STRING(32), "title=Account_ID key=true"),
      new DBField(FLD_deviceID, String.class, DBField.TYPE_STRING(32), "title=Device_ID key=true"),
      new DBField(FLD_timestamp, Long.TYPE, DBField.TYPE_UINT32, "title=Timestamp key=true"),
      new DBField(FLD_statusCode, Integer.TYPE, DBField.TYPE_UINT32, "title=Status_Code key=true"),
      new DBField(FLD_dataSource, String.class, DBField.TYPE_STRING(32), "title=Data_Source"),
      new DBField(FLD_rawData, String.class, DBField.TYPE_TEXT, "title=Raw_Data"),
      new DBField(FLD_latitude, Double.TYPE, DBField.TYPE_DOUBLE, "title=Latitude format=#0.00000"),
      new DBField(FLD_longitude, Double.TYPE, DBField.TYPE_DOUBLE,
          "title=Longitude format=#0.00000"),
      new DBField(FLD_speedKPH, Double.TYPE, DBField.TYPE_DOUBLE, "title=Speed_KPH format=#0.0"),
      new DBField(FLD_heading, Double.TYPE, DBField.TYPE_DOUBLE, "title=Heading format=#0.0"),
      new DBField(FLD_altitude, Double.TYPE, DBField.TYPE_DOUBLE, "title=Altitude format=#0.0"),
      new DBField(FLD_distanceKM, Double.TYPE, DBField.TYPE_DOUBLE, "title=Distance_KM format=#0.0"),
      new DBField(FLD_topSpeedKPH, Double.TYPE, DBField.TYPE_DOUBLE,
          "title=Top_Speed_KPH format=#0.0"),
      new DBField(FLD_geofenceID1, Long.TYPE, DBField.TYPE_UINT32, "title=Geofence_1"),
      new DBField(FLD_geofenceID2, Long.TYPE, DBField.TYPE_UINT32, "title=Geofence_2"), };

  /**
   * Specifies the keys used in the SQL database.
   * 
   * @author Martin D. Flynn
   * @author Robert S. Brewer
   */
  public static class Key extends DBRecordKey {

    /**
     * Basic constructor, just calls superclass constructor.
     */
    public Key() {
      super();
    }

    /**
     * Creates new record keys for GPS device events.
     *  
     * @param acctId account ID to add as key.
     * @param devId device ID to add as key.
     * @param timestamp timestamp value to add as key.
     * @param statusCode status code to add as key.
     */
    public Key(String acctId, String devId, long timestamp, long statusCode) {
      super.setFieldValue(FLD_accountID, acctId);
      super.setFieldValue(FLD_deviceID, devId);
      super.setFieldValue(FLD_timestamp, timestamp);
      super.setFieldValue(FLD_statusCode, statusCode);
    }

    /**
     * Returns the factory associated with database.
     * 
     * @return The factory associated with database.
     * @see org.opendmtp.dbtools.DBRecordKey#getFactory()
     */
    public DBFactory getFactory() {
      return EventData.getFactory();
    }

    /**
     * Returns the associated DBRecord.
     * 
     * @return The associated DBRecord.
     * @see org.opendmtp.dbtools.DBRecordKey#getDBRecord()
     */
    public DBRecord getDBRecord() {
      EventData rcd = (EventData) super.getDBRecord();
      // init as needed
      return rcd;
    }
  }

  /* factory constructor */
  private static DBFactory factory = null;

  /**
   * Gets the DBFactory, creating a new factory if one does not already exist.
   * 
   * @return The DBFactory (possibly new).
   */
  public static DBFactory getFactory() {
    if (factory == null) {
      factory = new DBFactory(TABLE_NAME, FieldInfo, KEY_PRIMARY, EventData.class,
          EventData.Key.class);
    }
    return factory;
  }

  /* Bean instance */
  /**
   * Constructs a new instance.
   */
  public EventData() {
    super();
  }

  /* database record */
  /**
   * Constructs a new instance using the supplied Key.
   * 
   * @param key Key to be used for new object.
   */
  public EventData(EventData.Key key) {
    super(key);
    // init?
  }

  // SQL table definition above
  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------
  // Bean access fields below

  /**
   * Returns account ID from database field.
   * 
   * @return account ID String, or empty String if value is null in database. 
   */
  public String getAccountID() {
    String v = (String) this.getFieldValue(FLD_accountID);
    return (v != null) ? v : "";
  }

  /**
   * Sets account ID in database field.
   * 
   * @param v account ID to store in database, if null then empty String will be stored
   * instead.
   */
  private void setAccountID(String v) {
    this.setFieldValue(FLD_accountID, ((v != null) ? v : ""));
  }

  // ------------------------------------------------------------------------

  /**
   * Returns device ID from database field.
   * 
   * @return device ID String, or empty String if value is null in database. 
   */
 public String getDeviceID() {
    String v = (String) this.getFieldValue(FLD_deviceID);
    return (v != null) ? v : "";
  }

 /**
  * Sets device ID in database field.
  * 
  * @param v device ID to store in database, if null then empty String will be stored
  * instead.
  */
  private void setDeviceID(String v) {
    this.setFieldValue(FLD_deviceID, ((v != null) ? v : ""));
  }

  // ------------------------------------------------------------------------

  /**
   * Returns timestamp from database field.
   * 
   * @return timestamp, or 0 if value is null in database. 
   */
  public long getTimestamp() {
    Long v = (Long) this.getFieldValue(FLD_timestamp);
    return (v != null) ? v.longValue() : 0L;
  }

  /**
   * Sets timestamp in database field.
   * 
   * @param v timestamp to store in database.
   */
  private void setTimestamp(long v) {
    this.setFieldValue(FLD_timestamp, v);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns status code from database field.
   * 
   * @return status code, or 0 if value is null in database.
   */
  public int getStatusCode() {
    Integer v = (Integer) this.getFieldValue(FLD_statusCode);
    return (v != null) ? v.intValue() : 0;
  }

  /**
   * Returns description of status code from database field.
   * 
   * @return Description of status code. 
   */
  public String getStatusCodeString() {
    return StatusCodes.GetCodeDescription(this.getStatusCode());
  }

  /**
   * Sets status code in database field.
   * 
   * @param v status code to store in database.
   */
  private void setStatusCode(int v) {
    this.setFieldValue(FLD_statusCode, v);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns data source from database field.
   * 
   * @return data source String, or empty String if value is null in database. 
   */
  public String getDataSource() {
    String v = (String) this.getFieldValue(FLD_dataSource);
    return (v != null) ? v : "";
  }

  /**
   * Sets data source in database field.
   * 
   * @param v data source to store in database, if null then empty String will be stored
   * instead.
   */
  public void setDataSource(String v) {
    this.setFieldValue(FLD_dataSource, ((v != null) ? v : ""));
  }

  // ------------------------------------------------------------------------

  /**
   * Returns raw data from database field.
   * 
   * @return raw data String, or empty String if value is null in database. 
   */
  public String getRawData() {
    String v = (String) this.getFieldValue(FLD_rawData);
    return (v != null) ? v : "";
  }

  /**
   * Sets data source in database field.
   * 
   * @param v data source to store in database, if null then empty String will be stored
   * instead.
   */
  public void setRawData(String v) {
    this.setFieldValue(FLD_rawData, ((v != null) ? v : ""));
  }

  // ------------------------------------------------------------------------

  /**
   * Returns latitude from database field.
   * 
   * @return Stored latitude, or 0 if value is null in database.
   */
  public double getLatitude() {
    Double v = (Double) this.getFieldValue(FLD_latitude);
    return (v != null) ? v.doubleValue() : 0.0;
  }

  /**
   * Sets the latitude in database field.
   * 
   * @param v latitude to store
   */
  public void setLatitude(double v) {
    this.setFieldValue(FLD_latitude, v);
  }

  /**
   * Retrieves latitude and longitude from database as a GeoPoint.
   * 
   * @return new GeoPoint constructed from latitude and longitude in database.
   */
  public GeoPoint getGeoPoint() {
    return new GeoPoint(this.getLatitude(), this.getLongitude());
  }

  // ------------------------------------------------------------------------

  /**
   * Returns longitude from database field.
   * 
   * @return Stored longitude, or 0 if value is null in database.
   */
  public double getLongitude() {
    Double v = (Double) this.getFieldValue(FLD_longitude);
    return (v != null) ? v.doubleValue() : 0.0;
  }

  /**
   * Sets the longitude in database field.
   * 
   * @param v longitude to store
   */
  public void setLongitude(double v) {
    this.setFieldValue(FLD_longitude, v);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns speed in kph from database field.
   * 
   * @return Stored speed in kph, or 0 if value is null in database.
   */
  public double getSpeedKPH() {
    Double v = (Double) this.getFieldValue(FLD_speedKPH);
    return (v != null) ? v.doubleValue() : 0.0;
  }

  /**
   * Sets the speed in kph in database field.
   * 
   * @param v speed to store
   */
  public void setSpeedKPH(double v) {
    this.setFieldValue(FLD_speedKPH, v);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns heading from database field.
   * 
   * @return Stored heading, or 0 if value is null in database.
   */
  public double getHeading() {
    Double v = (Double) this.getFieldValue(FLD_heading);
    return (v != null) ? v.doubleValue() : 0.0;
  }

  /**
   * Sets the heading in database field.
   * 
   * @param v heading to store
   */
  public void setHeading(double v) {
    this.setFieldValue(FLD_heading, v);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns altitude from database field.
   * 
   * @return Stored altitude, or 0 if value is null in database.
   */
  public double getAltitude() {
    Double v = (Double) this.getFieldValue(FLD_altitude);
    return (v != null) ? v.doubleValue() : 0.0;
  }

  /**
   * Sets the altitude in database field.
   * 
   * @param v altitude to store
   */
  public void setAltitude(double v) {
    this.setFieldValue(FLD_altitude, v);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns distance (traveled?) in km from database field.
   * 
   * @return Stored distance in km, or 0 if value is null in database.
   */
  public double getDistanceKM() {
    Double v = (Double) this.getFieldValue(FLD_distanceKM);
    return (v != null) ? v.doubleValue() : 0.0;
  }

  /**
   * Sets the distance (traveled?) in km in database field.
   * 
   * @param v distance to store
   */
  public void setDistanceKM(double v) {
    this.setFieldValue(FLD_distanceKM, v);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the top speed recorded in kph from database field.
   * 
   * @return Stored top speed in kph, or 0 if value is null in database.
   */
  public double getTopSpeedKPH() {
    Double v = (Double) this.getFieldValue(FLD_topSpeedKPH);
    return (v != null) ? v.doubleValue() : 0.0;
  }

  /**
   * Sets the top speed in kph in database field.
   * 
   * @param v top speed in kph to store
   */
  public void setTopSpeedKPH(double v) {
    this.setFieldValue(FLD_topSpeedKPH, v);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns GeoFence ID #1 from database field.
   * 
   * @return Stored GeoFence ID, or 0 if value is null in database.
   */
  public long getGeofenceID1() {
    Long v = (Long) this.getFieldValue(FLD_geofenceID1);
    return (v != null) ? v.longValue() : 0L;
  }

  /**
   * Sets GeoFence ID #1 in database field.
   * 
   * @param v GeoFence ID to store
   */
  public void setGeofenceID1(long v) {
    this.setFieldValue(FLD_geofenceID1, v);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns GeoFence ID #2 from database field.
   * 
   * @return Stored GeoFence ID, or 0 if value is null in database.
   */
  public long getGeofenceID2() {
    Long v = (Long) this.getFieldValue(FLD_geofenceID2);
    return (v != null) ? v.longValue() : 0L;
  }

  /**
   * Sets GeoFence ID #2 in database field.
   * 
   * @param v GeoFence ID to store
   */
  public void setGeofenceID2(long v) {
    this.setFieldValue(FLD_geofenceID2, v);
  }

  // Bean access fields above
  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

  /**
   * Formats a set of fields from the database as a record of comma separated
   * values (CSV).
   * 
   * @param fields the names of the fields to be formatted.
   * @return CSV formatted record
   */
  public String formatAsCSVRecord(String fields[]) {
    StringBuffer sb = new StringBuffer();
    if (fields != null) {
      // DBFactory fact = EventData.getFactory();
      for (int i = 0; i < fields.length; i++) {
        if (i > 0) {
          sb.append(",");
        }
        DBField dbFld = this.getRecordKey().getField(fields[i]);
        Object val = (dbFld != null) ? this.getFieldValue(fields[i]) : null;
        if (val != null) {
          Class typeClass = dbFld.getTypeClass();
          if (fields[i].equals(FLD_timestamp)) {
            long time = ((Long) val).longValue();
            DateTime dt = new DateTime(time);
            sb.append(dt.gmtFormat("yyyy/MM/dd,HH:mm:ss"));
          }
          else if (fields[i].equals(FLD_statusCode)) {
            int code = ((Integer) val).intValue();
            StatusCodes.Code c = StatusCodes.GetCode(code);
            if (c != null) {
              sb.append(c.getDescription());
            }
            else {
              sb.append("0x" + StringTools.toHexString(code));
            }
          }
          else if ((typeClass == Double.class) || (typeClass == Double.TYPE)) {
            double d = ((Double) val).doubleValue();
            String fmt = dbFld.getFormat();
            if ((fmt != null) && !fmt.equals("")) {
              sb.append(StringTools.format(d, fmt));
            }
            else {
              sb.append(String.valueOf(d));
            }
          }
          else {
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
  /**
   * Assembles an SQL query string (?) in the supplied buffer, based on the other
   * parameters.
   * 
   * @param wh buffer where SQL query string is placed.
   * @param acctId account ID to be matched.
   * @param devId device ID to be matched.
   * @param timeStart start of time interval of interest
   * @param timeEnd end of time interval of interest
   * @param statCode Array of acceptable status codes. 
   * @param gpsRequired true to only include records that have valid location data,
   * false to allow records with invalid location data.
   * @param andSelect additional required criterion for query. 
   * @return the buffer containing the query string as a convenience.
   */
  public static StringBuffer getWhereClause(StringBuffer wh, String acctId, String devId,
      long timeStart, long timeEnd, int statCode[], boolean gpsRequired, String andSelect) {
    // see SelectionConstraints
    if (wh == null) {
      wh = new StringBuffer();
    }

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
        if (i > 0) {
          wh.append(" OR ");
        }
        wh.append(FLD_statusCode).append("=").append(statCode[i]);
      }
      wh.append(")");
    }

    /* gps required */
    if (gpsRequired) {
      // This section states that if either of the latitude/longitude are '0',
      // then do not include the record in the select. This may not be valid
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
  /**
   * Retrieves events from database from a particular account/device over a specified
   * time interval. If invalid account or device ID or start/end times provided, a
   * new empty EventData object is returned.
   * 
   * @param acctId account ID to match
   * @param devId device ID to match
   * @param timeStart start of time interval to search
   * @param timeEnd end of time interval to search
   * @param limitType something to do with ordering of database records?
   * @param limit limit on number of records returned?
   * @return Array of EventData objects that match the supplied parameters.
   * @throws DBException if a database error is encountered.
   */
  public static EventData[] getRangeEvents(String acctId, String devId, long timeStart,
      long timeEnd, int limitType, long limit) throws DBException {

    /* invalid account/device */
    if ((acctId == null) || acctId.equals("")) {
      return new EventData[0];
    }
    else if ((devId == null) || devId.equals("")) {
      return new EventData[0];
    }

    /* invalid time range */
    if ((timeStart > 0L) && (timeEnd > 0L) && (timeStart > timeEnd)) {
      return new EventData[0];
    }

    /* where clause */
    StringBuffer wh = new StringBuffer();
    EventData.getWhereClause(wh, acctId, devId, timeStart, timeEnd, null /* statCode[] */,
        false /* gpsRequired */, null /* andSelect */);

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
      ae = (EventData[]) DBRecord.select(EventData.getFactory(), wh.toString());
    }
    finally {
      DBRecord.unlockTables();
    }
    if (ae == null) {
      // no records
      return new EventData[0];
    }
    else if (limitType == LIMIT_TYPE_FIRST) {
      // records are in ascending order
      return ae;
    }
    else {
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

  /**
   * Counts the number of events in the database from a particular account/device over
   * a specified time interval.
   * 
   * @param acctId account ID to match
   * @param devId device ID to match
   * @param timeStart start of time interval to search
   * @param timeEnd end of time interval to search
   * @return Number of database records that match the supplied parameters.
   * @throws DBException if a database error is encountered.
   */
  public static long getRecordCount(String acctId, String devId, long timeStart, long timeEnd)
      throws DBException {
    StringBuffer wh = new StringBuffer();
    EventData.getWhereClause(wh, acctId, devId, timeStart, timeEnd, null /* statCode[] */,
        false /* gpsRequired */, null /* andSelect */);
    try {
      return DBRecord.getRecordCount(EventData.getFactory(), wh);
    }
    catch (SQLException sqe) {
      throw new DBException("Getting record count", sqe);
    }
  }

}
