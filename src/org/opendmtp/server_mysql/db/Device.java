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

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import org.opendmtp.dbtools.DBConnection;
import org.opendmtp.dbtools.DBEdit;
import org.opendmtp.dbtools.DBException;
import org.opendmtp.dbtools.DBFactory;
import org.opendmtp.dbtools.DBField;
import org.opendmtp.dbtools.DBRecord;
import org.opendmtp.dbtools.DBRecordKey;
import org.opendmtp.server_mysql.DBConfig;
import org.opendmtp.server_mysql.dbtypes.DTProfileMask;
import org.opendmtp.server_mysql.dbtypes.DTUniqueID;

/*
 import java.lang.*;
 import java.util.*;
 import java.math.*;
 import java.io.*;
 import java.sql.*;

 import org.opendmtp.util.*;

 import org.opendmtp.dbtools.*;
 import org.opendmtp.server_mysql.dbtypes.*;
 import org.opendmtp.server_mysql.*;*/

/**
 * Representation of a database Device.
 * 
 * @author Martin D. Flynn
 * @author Elayne Man
 */
public class Device extends DBRecord {

  /**
   * The first limit type.
   */
  public static final int LIMIT_TYPE_FIRST = EventData.LIMIT_TYPE_FIRST;

  /**
   * The last limit type.
   */
  public static final int LIMIT_TYPE_LAST = EventData.LIMIT_TYPE_LAST;

  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------
  // SQL table definition below

  /**
   * Table Name.
   */
  public static final String TABLE_NAME = "Device";  
  
  /**
   * Account ID.
   */
  public static final String FLD_accountID = "accountID";
  
  /**
   * Device ID.
   */
  public static final String FLD_deviceID = "deviceID";
  
  /**
   * Unique ID.
   */
  public static final String FLD_uniqueID = "uniqueID";
  
  /**
   * Description.
   */
  public static final String FLD_description = "description";
  
  /**
   * Serial number.
   */
  public static final String FLD_serialNumber = "serialNumber";
  
  /**
   * Active.
   */
  public static final String FLD_isActive = "isActive";
  
  /**
   * Notification email.
   */
  public static final String FLD_notifyEmail = "notifyEmail";
  
  /**
   * Supported encoding.
   */
  public static final String FLD_supportedEncodings = "supportedEncodings";
  
  /**
   * Unit limit intervals.
   */
  public static final String FLD_unitLimitInterval = "unitLimitInterval";
  
  /**
   * Maximum allowed events.
   */
  public static final String FLD_maxAllowedEvents = "maxAllowedEvents";
  
  /**
   * Last total connection time.
   */
  public static final String FLD_lastTotalConnectTime = "lastTotalConnectTime";
  
  /**
   * Total profile mask.
   */
  public static final String FLD_totalProfileMask = "totalProfileMask";
  
  /**
   * Total maximum connections.
   */
  public static final String FLD_totalMaxConn = "totalMaxConn";
  
  /**
   * Total maximum connections per minute.
   */
  public static final String FLD_totalMaxConnPerMin = "totalMaxConnPerMin";
  
  /**
   * Last duplex connection time.
   */
  public static final String FLD_lastDuplexConnectTime = "lastDuplexConnectTime";
  
  /**
   * Duplex profile mask.
   */
  public static final String FLD_duplexProfileMask = "duplexProfileMask";
  
  /**
   * Duplex maximum connection.
   */
  public static final String FLD_duplexMaxConn = "duplexMaxConn";
  
  /**
   * Duplex maximum connection per minute.
   */
  public static final String FLD_duplexMaxConnPerMin = "duplexMaxConnPerMin";
  
  /**
   * Field information.
   */
  private static DBField FieldInfo[] = {
      new DBField(FLD_accountID, String.class, DBField.TYPE_STRING(32), "title=Account_ID key=true"),
      new DBField(FLD_deviceID, String.class, DBField.TYPE_STRING(32), "title=Device_ID key=true"),
      new DBField(FLD_uniqueID, DTUniqueID.class, DBField.TYPE_UINT64,
          "title=Unique_ID altkey=true"),
      new DBField(FLD_description, String.class, DBField.TYPE_STRING(128),
          "title=Description edit=2"),
      new DBField(FLD_serialNumber, String.class, DBField.TYPE_STRING(24),
          "title=Serial_Number edit=2"),
      new DBField(FLD_isActive, Boolean.TYPE, DBField.TYPE_BOOLEAN, "title=Is_Active edit=2"),
      new DBField(FLD_notifyEmail, String.class, DBField.TYPE_STRING(128),
          "title=Notification_EMail_Address edit=2"),
      new DBField(FLD_supportedEncodings, Integer.TYPE, DBField.TYPE_UINT8,
          "title=Supported_Encodings edit=2"),
      new DBField(FLD_unitLimitInterval, Integer.TYPE, DBField.TYPE_UINT16,
          "title=Accounting_Time_Interval_Minutes edit=2"),
      new DBField(FLD_maxAllowedEvents, Integer.TYPE, DBField.TYPE_UINT16,
          "title=Max_Events_per_Interval edit=2"),
      new DBField(FLD_lastTotalConnectTime, Long.TYPE, DBField.TYPE_UINT32,
          "title=Last_Total_Connect_Time edit=2"),
      new DBField(FLD_totalProfileMask, DTProfileMask.class, DBField.TYPE_BINARY,
          "title=Total_Profile_Mask"),
      new DBField(FLD_totalMaxConn, Integer.TYPE, DBField.TYPE_UINT16,
          "title=Max_Total_Connections_per_Interval edit=2"),
      new DBField(FLD_totalMaxConnPerMin, Integer.TYPE, DBField.TYPE_UINT16,
          "title=Max_Total_Connections_per_Minute edit=2"),
      new DBField(FLD_lastDuplexConnectTime, Long.TYPE, DBField.TYPE_UINT32,
          "title=Last_Duplex_Connect_Time"),
      new DBField(FLD_duplexProfileMask, DTProfileMask.class, DBField.TYPE_BINARY,
          "title=Duplex_Profile_Mask"),
      new DBField(FLD_duplexMaxConn, Integer.TYPE, DBField.TYPE_UINT16,
          "title=Max_Duplex_Connections_per_Interval edit=2"),
      new DBField(FLD_duplexMaxConnPerMin, Integer.TYPE, DBField.TYPE_UINT16,
          "title=Max_Duplex_Connections_per_Minute edit=2"), newField_lastUpdateTime(), };

  /**
   * Representation of a database key.
   * 
   */
  public static class Key extends DBRecordKey {

    /**
     * Default constructor.
     * 
     */
    public Key() {
      super();
    }

    /**
     * Constructor for the Key given an account ID and device ID.
     * 
     * @param acctId The account ID.
     * @param devId The device ID.
     */
    public Key(String acctId, String devId) {
      super.setFieldValue(FLD_accountID, acctId);
      super.setFieldValue(FLD_deviceID, devId);
    }

    /**
     * Returns the factory value.
     * 
     * @return The factory value.
     */
    public DBFactory getFactory() {
      return Device.getFactory();
    }
  }

  /* factory constructor */
  private static DBFactory factory = null;

  /**
   * Returns the factory value. If there is none, it creates one.
   * 
   * @return The factory value.
   */
  public static DBFactory getFactory() {
    if (factory == null) {
      factory = new DBFactory(TABLE_NAME, FieldInfo, KEY_PRIMARY, Device.class, Device.Key.class);
    }
    return factory;
  }

  /**
   * Calls the super method of the Device.
   * 
   */
  public Device() {
    super();
  }

  /* database record */
  /**
   * Calls the super method of the Device given a Device key.
   * 
   * @param key The Device key.
   */
  public Device(Device.Key key) {
    super(key);
  }

  // SQL table definition above
  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------
  // Bean access fields below

  /**
   * Returns the account ID.
   * 
   * @return The account ID.
   */
  public String getAccountID() {
    String v = (String) this.getFieldValue(FLD_accountID);
    return (v != null) ? v : "";
  }

  /**
   * Sets the account ID.
   * 
   * @param v The account ID.
   */
  private void setAccountID(String v) {
    this.setFieldValue(FLD_accountID, ((v != null) ? v : ""));
  }

  /**
   * Returns the Device ID.
   * 
   * @return The Device ID.
   */
  public String getDeviceID() {
    String v = (String) this.getFieldValue(FLD_deviceID);
    return (v != null) ? v : "";
  }

  /**
   * Sets the Device ID.
   * 
   * @param v The Device ID.
   */
  private void setDeviceID(String v) {
    this.setFieldValue(FLD_deviceID, ((v != null) ? v : ""));
  }

  /**
   * Returns the Unique ID of the DT.
   * 
   * @return the Unique ID value.
   */
  public DTUniqueID getUniqueID() {
    DTUniqueID v = (DTUniqueID) this.getFieldValue(FLD_uniqueID);
    return v;
  }

  /**
   * Sets the Unique ID of the DT.
   * 
   * @param v The Unique ID.
   */
  private void setUniqueID(DTUniqueID v) {
    this.setFieldValue(FLD_uniqueID, v);
  }

  /**
   * Returns the description.
   * 
   * @return The description.
   */
  public String getDescription() {
    String v = (String) this.getFieldValue(FLD_description);
    return (v != null) ? v : "";
  }

  /**
   * Sets the description.
   * 
   * @param v The description.
   */
  public void setDescription(String v) {
    this.setFieldValue(FLD_description, ((v != null) ? v : ""));
  }

  /**
   * Returns the serial number.
   * 
   * @return The serial numeber.
   */
  public String getSerialNumber() {
    String v = (String) this.getFieldValue(FLD_serialNumber);
    return (v != null) ? v : "";
  }

  /**
   * Sets the serial number.
   * 
   * @param v The serial number.
   */
  public void setSerialNumber(String v) {
    this.setFieldValue(FLD_serialNumber, ((v != null) ? v : ""));
  }

  /**
   * Returns a true or false depending if the field value is active.
   * 
   * @return True or false depending of the field value is active or not.
   */
  public boolean getIsActive() {
    Boolean v = (Boolean) this.getFieldValue(FLD_isActive);
    return (v != null) ? v.booleanValue() : false;
  }

  /**
   * Sets the active value of the field.
   * 
   * @param v The boolean value.
   */
  public void setIsActive(boolean v) {
    this.setFieldValue(FLD_isActive, v);
  }

  /**
   * Returns the email address used for notification.
   * 
   * @return The notification email address.
   */
  public String getNotifyEmail() {
    String v = (String) this.getFieldValue(FLD_notifyEmail);
    return (v != null) ? v : "";
  }

  /**
   * Sets the email address used for notification.
   * 
   * @param v The notification email address.
   */
  public void setNotifyEmail(String v) {
    this.setFieldValue(FLD_notifyEmail, ((v != null) ? v : ""));
  }

  /**
   * Returns the supported encodings.
   * 
   * @return The number of the supported encodings.
   */
  public int getSupportedEncodings() {
    Integer v = (Integer) this.getFieldValue(FLD_supportedEncodings);
    return (v != null) ? v.intValue() : 0;
  }

  /**
   * Sets the supported encodings.
   * 
   * @param encoding The number of the supported encodings.
   */
  public void setSupportedEncodings(int encoding) {
    this.setFieldValue(FLD_supportedEncodings, encoding);
  }

  /**
   * Adds a supported encoding.
   * 
   * @param encoding The number of the supported encoding.
   */
  public void addEncoding(int encoding) {
    int vi = this.getSupportedEncodings();
    if ((vi & encoding) != encoding) {
      vi |= encoding;
      this.setSupportedEncodings(vi);
    }
  }

  /**
   * Returns the last total connection time.
   * 
   * @return The last total connection time.
   */
  public long getLastTotalConnectTime() {
    Long v = (Long) this.getFieldValue(FLD_lastTotalConnectTime);
    return (v != null) ? v.longValue() : 0L;
  }

  /**
   * Sets the last total connection time.
   * 
   * @param v The last total connection time.
   */
  public void setLastTotalConnectTime(long v) {
    this.setFieldValue(FLD_lastTotalConnectTime, v);
  }

  /**
   * Returns the last duplex connection time.
   * 
   * @return The last duplex connection time.
   */
  public long getLastDuplexConnectTime() {
    Long v = (Long) this.getFieldValue(FLD_lastDuplexConnectTime);
    return (v != null) ? v.longValue() : 0L;
  }

  /**
   * Sets the last duplex connection time.
   * 
   * @param v The last duplex connection time.
   */
  public void setLastDuplexConnectTime(long v) {
    this.setFieldValue(FLD_lastDuplexConnectTime, v);
  }

  /**
   * Returns the unit limit interval.
   * 
   * @return The unit limit interval.
   */
  public int getUnitLimitInterval() {
    Integer v = (Integer) this.getFieldValue(FLD_unitLimitInterval);
    return (v != null) ? v.intValue() : 0;
  }

  /**
   * Sets the unit limit interval.
   * 
   * @param v The unit limit interval.
   */
  public void setUnitLimitInterval(int v) {
    this.setFieldValue(FLD_unitLimitInterval, v);
  }

  /**
   * Returns the maximum allowed events.
   * 
   * @return The maximum allowed events.
   */
  public int getMaxAllowedEvents() {
    Integer v = (Integer) this.getFieldValue(FLD_maxAllowedEvents);
    return (v != null) ? v.intValue() : 1;
  }

  /**
   * Sets the maximum allowed events.
   * 
   * @param max The maximum allowed events.
   */
  public void setMaxAllowedEvents(int max) {
    this.setFieldValue(FLD_maxAllowedEvents, max);
  }

  /**
   * Returns the total profile mask value.
   * 
   * @return The total profile mask value.
   */
  public DTProfileMask getTotalProfileMask() {
    DTProfileMask v = (DTProfileMask) this.getFieldValue(FLD_totalProfileMask);
    return v;
  }

  /**
   * Sets the total profile mask value.
   * 
   * @param v The total profile mask value.
   */
  public void setTotalProfileMask(DTProfileMask v) {
    this.setFieldValue(FLD_totalProfileMask, v);
  }

  /**
   * Returns the total maximum connections.
   * 
   * @return The total maximum connections.
   */
  public int getTotalMaxConn() {
    Integer v = (Integer) this.getFieldValue(FLD_totalMaxConn);
    return (v != null) ? v.intValue() : 0;
  }

  /**
   * Sets the total maximum connections.
   * 
   * @param v The total maximum connections.
   */
  public void setTotalMaxConn(int v) {
    this.setFieldValue(FLD_totalMaxConn, v);
  }

  /**
   * Returns the total maximum connections per minute.
   * 
   * @return The total maximum connections per minute.
   */
  public int getTotalMaxConnPerMin() {
    Integer v = (Integer) this.getFieldValue(FLD_totalMaxConnPerMin);
    return (v != null) ? v.intValue() : 0;
  }

  /**
   * Returns the total maximum connections per minute.
   * 
   * @param v The total maximum connections per minute.
   */
  public void setTotalMaxConnPerMin(int v) {
    this.setFieldValue(FLD_totalMaxConnPerMin, v);
  }

  /**
   * Returns the duplex profile mask value.
   * 
   * @return The duplex profile mask value.
   */
  public DTProfileMask getDuplexProfileMask() {
    DTProfileMask v = (DTProfileMask) this.getFieldValue(FLD_duplexProfileMask);
    return v;
  }

  /**
   * Sets the duplex profile mask value.
   * 
   * @param v The duplex profile mask value.
   */
  public void setDuplexProfileMask(DTProfileMask v) {
    this.setFieldValue(FLD_duplexProfileMask, v);
  }

  /**
   * Returns the duplex maxiumum connection.
   * 
   * @return The duplex maxiumum connection.
   */
  public int getDuplexMaxConn() {
    Integer v = (Integer) this.getFieldValue(FLD_duplexMaxConn);
    return (v != null) ? v.intValue() : 0;
  }

  /**
   * Sets the duplex maxiumum connection.
   * 
   * @param max The duplex maxiumum connection.
   */
  public void setDuplexMaxConn(int max) {
    this.setFieldValue(FLD_duplexMaxConn, max);
  }

  /**
   * Returns the duplex maxiumum connections per minute.
   * 
   * @return The duplex maxiumum connection per minute.
   */
  public int getDuplexMaxConnPerMin() {
    Integer v = (Integer) this.getFieldValue(FLD_duplexMaxConnPerMin);
    return (v != null) ? v.intValue() : 0;
  }

  /**
   * Sets the duplex maxiumum connections per minute.
   * 
   * @param max The duplex maxiumum connection per minute.
   */
  public void setDuplexMaxConnPerMin(int max) {
    this.setFieldValue(FLD_duplexMaxConnPerMin, max);
  }

  // Bean access fields above
  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

  /**
   * Checks the event for specific rule triggers. Place special event rules checking in this method.
   * For instance, email can be sent using "org.opendmtp.util.SendMail" based on trigger which have
   * occur due to the receipt of a particular type of event.
   * 
   * @param evdb The EventData database
   */
  public void checkEventRules(EventData evdb) {

  }

  /**
   * Returns the events in a specified time range.
   * 
   * @param timeStart The start time.
   * @param timeEnd The end time.
   * @param limitType The limit type.
   * @param limit The limit value.
   * @return The event data array.
   * @throws DBException If the event does not exist.
   * 
   */
  public EventData[] getRangeEvents(long timeStart, long timeEnd, int limitType, long limit)
      throws DBException {
    return EventData.getRangeEvents(this.getAccountID(), this.getDeviceID(), timeStart, timeEnd,
        limitType, limit);
  }

  /**
   * Returns the most recent limit events.
   * 
   * @param limit The limit value.
   * @return The event data array.
   * @throws DBException If the event does not exist.
   */
  public EventData[] getLatestEvents(long limit) throws DBException {
    long startTime = -1L;
    long endTime = -1L;
    return EventData.getRangeEvents(this.getAccountID(), this.getDeviceID(), startTime, endTime,
        LIMIT_TYPE_LAST, limit);
  }

  /**
   * Overidden method that returns the account ID and device ID.
   * 
   * @return The account ID and device ID in String form.
   */
  public String toString() {
    return this.getAccountID() + "/" + this.getDeviceID();
  }

  /**
   * Determines if the given Device exists given the account ID and device ID.
   * 
   * @param acctID The account ID.
   * @param devID The device ID.
   * @return True or fale if the account exists or not.
   * @throws DBException if error occurs while testing existance.
   */
  public static boolean exists(String acctID, String devID) throws DBException {
    if ((acctID != null) && (devID != null)) {
      Device.Key devKey = new Device.Key(acctID, devID);
      return devKey.exists();
    }
    return false;
  }

  /**
   * Returns the device given the account ID and device ID.
   * 
   * @param acctID The account ID.
   * @param devID The device ID.
   * @return the Device.
   * @throws DBException if error occurs while retrieving record.
   */
  public static Device getDevice(String acctID, String devID) throws DBException {
    if ((acctID != null) && (devID != null)) {
      Device.Key key = new Device.Key(acctID, devID);
      if (key.exists()) {
        return (Device) key.getDBRecord(true);
      }
    }
    return null;
  }

  /**
   * Returns the device given the unique ID.
   * 
   * @param uniqID The unique ID.
   * @return the Device.
   * @throws DBException if error occurs while retrieving record.
   */
  public static Device getDevice(long uniqID) throws DBException {

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
      sel.append(FLD_uniqueID).append("='").append(uniqID).append("'");
      sel.append(") LIMIT 1");
      // Note: The index on the column FLD_uniqueID does not enforce uniqueness
      // (since null/empty values are allowed and needed)

      /* get records */
      stmt = DBConnection.getDefaultConnection().execute(sel.toString());
      rs = stmt.getResultSet();
      while (rs.next()) {
        String acctId = rs.getString(FLD_accountID);
        String devId = rs.getString(FLD_deviceID);
        device = new Device(new Device.Key(acctId, devId));
        device.setFieldValues(rs);
        break; // only one record
      }

    }
    catch (SQLException sqe) {
      throw new DBException("Get Device UniqueID", sqe);
    }
    finally {
      if (rs != null) {
        try {
          rs.close();
        }
        catch (Throwable t) {
        }
      }
      if (stmt != null) {
        try {
          stmt.close();
        }
        catch (Throwable t) {
        }
      }
    }

    return device;
  }

  /**
   * The main method.
   * 
   * @param args The command-line parameter array
   */
  public static void main(String args[]) {
    DBConfig.init(args, true);
    Device.Key key = new Device.Key("opendmtp", "mobile");
    DBEdit dbEdit = new DBEdit(key);
    dbEdit.print();
  }

}
