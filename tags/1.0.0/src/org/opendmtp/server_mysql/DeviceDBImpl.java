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

import java.io.EOFException;
import java.io.IOException;

import org.opendmtp.codes.Encoding;
import org.opendmtp.codes.ServerErrors;
import org.opendmtp.dbtools.DBEdit;
import org.opendmtp.dbtools.DBException;
import org.opendmtp.server.db.DeviceDB;
import org.opendmtp.server.db.PayloadTemplate;
import org.opendmtp.server_mysql.db.Account;
import org.opendmtp.server_mysql.db.Device;
import org.opendmtp.server_mysql.db.EventData;
import org.opendmtp.server_mysql.db.EventTemplate;
import org.opendmtp.server_mysql.dbtypes.DTProfileMask;
import org.opendmtp.util.GeoEvent;
import org.opendmtp.util.Print;
import org.opendmtp.util.RTConfig;

/**
 * Provides access to information about the device.
 * 
 * @author Martin D. Flynn
 * @author George Lee
 */
public class DeviceDBImpl implements DeviceDB {

  /** The device used by this DeviceDBImpl. */
  private Device device = null;

  /**
   * Create a new DeviceDBImpl using the specified device.
   * 
   * @param dev The device to use in this database..
   */
  public DeviceDBImpl(Device dev) {
    this.device = dev;
  }

  /**
   * Get the account name associated with this device.
   * 
   * @return The account ID of the account associated with this device.
   */
  public String getAccountName() {
    return this.device.getAccountID();
  }

  /**
   * Get the device name associated with this device.
   * 
   * @return The device ID of the device in this database entry.
   */
  public String getDeviceName() {
    return this.device.getDeviceID();
  }

  /**
   * Get the description of the device.
   * 
   * @return The descrption of the device.
   */
  public String getDescription() {
    return this.device.getDescription();
  }

  /**
   * Get the activity status of the device.
   * 
   * @return True if the device is active, false otherwise.
   */
  public boolean isActive() {
    return this.device.getIsActive();
  }

  /**
   * Get the maximum number of allowed events for the device.
   * 
   * @return The device's maximum allowed events.
   */
  public int getMaxAllowedEvents() {
    return this.device.getMaxAllowedEvents();
  }

  /**
   * Get the number of events logged by this device during a time interval. Returns -1 if there is
   * an error in retrieving the number of event records.
   * 
   * @param timeStart The start of the time interval.
   * @param timeEnd The end of the time interval.
   * @return The number of events that occurred between timeStart and timeEnd.
   */
  public long getEventCount(long timeStart, long timeEnd) {
    try {
      long count = EventData.getRecordCount(this.getAccountName(), this.getDeviceName(), timeStart,
          timeEnd);
      return count;
    }
    catch (DBException dbe) {
      dbe.printException();
      return -1L;
    }
  }

  /**
   * Get the maximum time interval between packets.
   * 
   * @return The unit limit interval of the device.
   */
  public int getLimitTimeInterval() {
    return this.device.getUnitLimitInterval();
  }

  /**
   * Get the maximum number of connections allowed by the device.
   * 
   * @return The total max connections of the device.
   */
  public int getMaxTotalConnections() {
    return this.device.getTotalMaxConn();
  }

  /**
   * Get the maximum number of connections allowed per minute by the device.
   * 
   * @return The total max connections per minute of the device.
   */
  public int getMaxTotalConnectionsPerMinute() {
    return this.device.getTotalMaxConnPerMin();
  }

  /**
   * Get the byte mask of the total connection profile for the device. Returns a 0 byte if the mask
   * is not initialized.
   * 
   * @return The total profile mask of the device (or 0).
   */
  public byte[] getTotalConnectionProfile() {
    DTProfileMask v = this.device.getTotalProfileMask();
    return (v != null) ? v.getByteMask() : new byte[0];
  }

  /**
   * Set the byte mask of the total connection profile for the device. Sets the limit time interval
   * of the mask using getLimitTimeInterval().
   * 
   * @param profile The new byte mask of the profile.
   */
  public void setTotalConnectionProfile(byte[] profile) {
    DTProfileMask mask = new DTProfileMask(profile);
    mask.setLimitTimeInterval(this.getLimitTimeInterval());
    this.device.setTotalProfileMask(mask);
  }

  /**
   * Get the time of the last total connection.
   * 
   * @return The time of the last total connection.
   */
  public long getLastTotalConnectionTime() {
    return this.device.getLastTotalConnectTime();
  }

  /**
   * Set the time of the last total connection.
   * 
   * @param time The new time of the last total connection.
   */
  public void setLastTotalConnectionTime(long time) {
    this.device.setLastTotalConnectTime(time);
  }

  /**
   * Get the maximum number of duplex connections allowed by the device.
   * 
   * @return The number of duplex connections allowed by the device.
   */
  public int getMaxDuplexConnections() {
    return this.device.getDuplexMaxConn();
  }

  /**
   * Get the maximum number of duplex connections per minute allowed by the device.
   * 
   * @return The number of duplex connections per minute allowed by the device.
   */
  public int getMaxDuplexConnectionsPerMinute() {
    return this.device.getDuplexMaxConnPerMin();
  }

  /**
   * Get the byte mask associated with the duplex connection profile for the device. Returns a 0
   * byte if the byte mask has not been set.
   * 
   * @return The byte string associated with the duplex connection profile for the device (or 0).
   */
  public byte[] getDuplexConnectionProfile() {
    DTProfileMask v = this.device.getDuplexProfileMask();
    return (v != null) ? v.getByteMask() : new byte[0];
  }

  /**
   * Set the byte mask associated with the duplex connection profile for the device.
   * 
   * @param profile The byte string of the new duplex connection profile.
   */
  public void setDuplexConnectionProfile(byte[] profile) {
    DTProfileMask mask = new DTProfileMask(profile);
    mask.setLimitTimeInterval(this.getLimitTimeInterval());
    this.device.setDuplexProfileMask(mask);
  }

  /**
   * Get the time of the last duplex connection to the device.
   * 
   * @return The time of the last duplex connection.
   */
  public long getLastDuplexConnectionTime() {
    return this.device.getLastDuplexConnectTime();
  }

  /**
   * Set the time of the last duplex connection to the device.
   * 
   * @param time The new time of the last duplex connection.
   */
  public void setLastDuplexConnectionTime(long time) {
    this.device.setLastDuplexConnectTime(time);
  }

  /**
   * Determine if an encoding method is supported by the device.
   * 
   * @param encoding The mask of the encoding method.
   * @return True if the encoding method is supported, false otherwise.
   */
  public boolean supportsEncoding(int encoding) {
    // 'encoding' is a mask containing one (or more) of the following:
    // Encoding.SUPPORTED_ENCODING_BINARY
    // Encoding.SUPPORTED_ENCODING_BASE64
    // Encoding.SUPPORTED_ENCODING_HEX
    // Encoding.SUPPORTED_ENCODING_CSV
    int vi = this.device.getSupportedEncodings();
    return ((vi & encoding) != 0);
  }

  /**
   * Remove an encoding method from the list of supported encodings.
   * 
   * @param encoding The encoding method to be removed from the list.
   */
  public void removeEncoding(int encoding) {
    int vi = this.device.getSupportedEncodings();
    if ((vi & encoding) != 0) {
      vi &= ~encoding;
      this.device.setSupportedEncodings(vi);
    }
  }

  /**
   * Save a new PayloadTemplate using information from the device.
   * 
   * @param template The template to be saved.
   * @return True if the template was saved successfully, false otherwise.
   */
  public boolean addClientPayloadTemplate(PayloadTemplate template) {
    return EventTemplate.SetPayloadTemplate(this.getAccountName(), this.getDeviceName(), template);
  }

  /**
   * Get the PayloadTemplate associated with the custType. Returns null if the template is not
   * found.
   * 
   * @param custType The key used to retrieve the PayloadTemplate.
   * @return The PayloadTemplate associated with the key or null if the template is not found.
   */
  public PayloadTemplate getClientPayloadTemplate(int custType) {
    return EventTemplate.GetPayloadTemplate(this.getAccountName(), this.getDeviceName(), custType);
  }

  /**
   * Add an event to the record associated with this device. Returns server error codes.
   * 
   * @param geoEvent The event to be recorded and saved.
   * @return ServerErrors.NAK_OK if the save was successful. ServerErrors.NAK_EVENT_Error if there
   *         is a problem with saving the record.
   */
  public int insertEvent(GeoEvent geoEvent) {

    /* create key */
    EventData.Key evKey = new EventData.Key(this.getAccountName(), this.getDeviceName(), geoEvent
        .getTimestamp(), geoEvent.getStatusCode());

    /* populate record */
    EventData evdb = (EventData) evKey.getDBRecord();
    evdb.setFieldValue(EventData.FLD_dataSource, geoEvent.getDataSource());
    evdb.setFieldValue(EventData.FLD_rawData, geoEvent.getRawData());
    evdb.setFieldValue(EventData.FLD_latitude, geoEvent.getLatitude());
    evdb.setFieldValue(EventData.FLD_longitude, geoEvent.getLongitude());
    evdb.setFieldValue(EventData.FLD_speedKPH, geoEvent.getSpeed());
    evdb.setFieldValue(EventData.FLD_heading, geoEvent.getHeading());
    evdb.setFieldValue(EventData.FLD_altitude, geoEvent.getAltitude());
    evdb.setFieldValue(EventData.FLD_distanceKM, geoEvent.getDistance());
    evdb.setFieldValue(EventData.FLD_topSpeedKPH, geoEvent.getTopSpeed());
    evdb.setFieldValue(EventData.FLD_geofenceID1, geoEvent.getGeofence(0));
    evdb.setFieldValue(EventData.FLD_geofenceID2, geoEvent.getGeofence(1));

    /* save */
    try {
      evdb.save();
    }
    catch (DBException dbe) {
      return ServerErrors.NAK_EVENT_ERROR;
    }

    /* check rules and return */
    this.device.checkEventRules(evdb);
    return ServerErrors.NAK_OK;

  }

  /**
   * Returns a string representation of the device. Returns the empty string if the device is not
   * initialized.
   * 
   * @return The device's toString() method (or the empty string).
   */
  public String toString() {
    return (this.device != null) ? this.device.toString() : "";
  }

  private static final int DEFAULT_ENCODING = Encoding.SUPPORTED_ENCODING_BINARY
      | Encoding.SUPPORTED_ENCODING_BASE64 | Encoding.SUPPORTED_ENCODING_HEX;
  private static final int DEFAULT_UNIT_LIMIT_INTERVAL = 60;
  private static final int DEFAULT_MAX_ALLOWED_EVENTS = 21;
  private static final int DEFAULT_TOTAL_MAX_CONNECTIONS = 10;
  private static final int DEFAULT_TOTAL_MAX_CONNECTIONS_PER_MIN = 2;
  private static final int DEFAULT_DUPLEX_MAX_CONNECTIONS = 6;
  private static final int DEFAULT_DUPLEX_MAX_CONNECTIONS_PER_MIN = 1;

  /**
   * Get/create a device with the specified account ID and device ID. Throws exceptions if the ids
   * are not specified, if the device id is not found (when retrieving the device), or if there is
   * an error in reading the device (when retrieving).
   * 
   * @param acctID The account ID associated with the device.
   * @param devID The ID of the device to retrieve.
   * @param create Set to true to create a device with the account and device ids. Set to false to
   *        search for the device.
   * @return The device with the specified account and device IDs or a new device with the specified
   *         account and device IDs.
   * @throws DBException Exception thrown if the IDs are not specified, if the device id is not
   *         found (when retrieving), or if there is an error in retrieving the device.
   */
  private static Device getDevice(String acctID, String devID, boolean create) throws DBException {
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
        dev = (Device) devKey.getDBRecord();
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
      }
      else {
        throw new DBException("Device-ID does not exists: " + devKey);
      }
    }
    else {
      dev = Device.getDevice(acctID, devID);
      if (dev == null) {
        throw new DBException("Unable to read existing Device-ID: " + devKey);
      }
      return dev;
    }

  }

  /**
   * Create a new device with the specified account ID and device ID. Uses the getDevice() method
   * with create set to true.
   * 
   * @param acctID The account ID to be associated with the device.
   * @param devID The ID of the device to be created.
   * @throws DBException Exception thrown if there is an error in saving the device.
   */
  public static void createNewDevice(String acctID, String devID) throws DBException {
    Device dev = DeviceDBImpl.getDevice(acctID, devID, true);
    dev.save();
  }

  private static final String ARG_ACCOUNT = "account";
  private static final String ARG_DEVICE = "device";
  private static final String ARG_CREATE = "create";
  private static final String ARG_EDIT = "edit";
  private static final String ARG_EVENTS = "events";
  private static final String ARG_OUTPUT = "output";
  private static final String ARG_FORMAT = "format";

  /**
   * Display usage information about using this class.
   */
  private static void usage() {
    Print.logInfo("Usage:");
    Print.logInfo("  java ... " + DeviceDBImpl.class.getName() + " {options}");
    Print.logInfo("Common Options:");
    Print.logInfo("  -account=<id>                  Acount ID which owns Device");
    Print.logInfo("  -device=<id>                   Device ID to create/edit");
    Print.logInfo("  -create                        Create a new Device");
    Print.logInfo("  -edit                          Edit an existing (or newly created) Device");
    System.exit(1);
  }

  /**
   * Adds a device to an account or edits the device associated with the account. Takes commands
   * from the command line and retrieves the account.
   * <p>
   * 
   * System exits with an error if there is an error when retrieving the account or the device. Also
   * exits with an error if there is a problem with writing events to the account.
   * 
   * @param argv Command line parameters to pass to DBConfig.
   */
  public static void main(String argv[]) {
    DBConfig.init(argv, true);
    // Commands:
    // { -create | -edit | -events }
    // -account=<name>
    // -device=<name>
    String acctID = RTConfig.getString(new String[] { "acct", ARG_ACCOUNT }, "");
    String devID = RTConfig.getString(new String[] { "dev", ARG_DEVICE }, "");

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
    }
    catch (DBException dbe) {
      Print.logException("Error loading Account: " + acctID, dbe);
      // dbe.printException();
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
    }
    catch (DBException dbe) {
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
      }
      else {
        try {
          DeviceDBImpl.createNewDevice(acctID, devID);
          Print.logInfo("Created Device '" + acctID + ":" + devID + "'");
        }
        catch (DBException dbe) {
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
      }
      else {
        try {
          Device device = DeviceDBImpl.getDevice(acctID, devID, false); // may throw DBException
          DBEdit editor = new DBEdit(device);
          editor.edit(); // may throw IOException
        }
        catch (IOException ioe) {
          if (ioe instanceof EOFException) {
            Print.logError("End of input");
          }
          else {
            Print.logError("IO Error");
          }
        }
        catch (DBException dbe) {
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
