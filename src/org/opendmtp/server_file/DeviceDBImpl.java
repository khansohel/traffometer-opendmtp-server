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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.opendmtp.codes.Encoding;
import org.opendmtp.codes.ServerErrors;
import org.opendmtp.codes.StatusCodes;
import org.opendmtp.server.db.DeviceDB;
import org.opendmtp.server.db.PayloadTemplate;
import org.opendmtp.util.DateTime;
import org.opendmtp.util.FileTools;
import org.opendmtp.util.GeoEvent;
import org.opendmtp.util.GeoPoint;
import org.opendmtp.util.Print;
import org.opendmtp.util.StringTools;

/**
 * Implementation of the DeviceDB interface.  Provides information about the device.
 * 
 * @author Martin D. Flynn
 * @author George Lee
 */
public class DeviceDBImpl implements DeviceDB {

  /**Maximum number of allowed events.*/
  private static final int MAX_ALLOWED_EVENTS = -1; // no limit

  /**Time interval (in minutes) in between each sent packet.*/
  private static final int LIMIT_TIME_INTERVAL = 0;

  /**Total connections to the device.*/
  private static final int MAX_TOTAL_CONNECTIONS = -1; // no limit

  /**Total connections to the device per minute.*/
  private static final int MAX_TOTAL_CONNECTIONS_PER_MIN = -1; // no limit

  /**Maximum number of duplex connections to the device.*/
  private static final int MAX_DUPLEX_CONNECTIONS = -1; // no limit

  /**Maximum number of duplex connections per minute to the device.*/
  private static final int MAX_DUPLEX_CONNECTIONS_PER_MIN = -1; // no limit

  /**Supported packet encoding methods.*/
  private static int SUPPORTED_ENCODING = Encoding.SUPPORTED_ENCODING_BINARY
      | Encoding.SUPPORTED_ENCODING_BASE64 | Encoding.SUPPORTED_ENCODING_HEX;

  /**Directory in which to store information about account and device events.*/
  private static File dataStoreDirectory = new File(".");

  /**
   * Set a new dataStoreDirectory at a given path. If the path does not exist, a new directory
   * called "./data" is created if the "." directory exists. Else, the new created directory is "."
   * 
   * @param dir The pathname of the directory to store the events in.
   */
  public static void setDataStoreDirectory(File dir) {
    if ((dir != null) && dir.isDirectory()) {
      dataStoreDirectory = dir;
    }
    else {
      dataStoreDirectory = new File("./data");
      if (!dataStoreDirectory.isDirectory()) {
        dataStoreDirectory = new File(".");
      }
    }
  }

  /**
   * Set a new data store directory called "." if a directory has not been set.
   * Else, it returns the current dataStoreDirectory.
   * 
   * @return The file directory that stores the information about events.
   */
  public static File getDataStoreDirectory() {
    if (dataStoreDirectory == null) {
      dataStoreDirectory = new File(".");
    }
    return dataStoreDirectory;
  }

  /** Collection that stores the customPayloadTemplates.*/
  private static HashMap customPayloadTemplates = new HashMap();

  /**ID of the account.*/
  private String accountId = null;

  /**ID of the device.*/
  private String deviceId = null;

  /**Description of the account or device.*/
  private String description = "";

  /**Status of the account or device (active or inactive).*/
  private boolean isActive = true;

  /**Max time interval between packets.*/
  private int limitTimeInterval = LIMIT_TIME_INTERVAL;

  /**Max allowed events by the device.*/
  private int maxAllowedEvents = MAX_ALLOWED_EVENTS;

  /**Maximum total connections allowed by the device.*/
  private int maxTotalConnections = MAX_TOTAL_CONNECTIONS;

  /**Maximum number of connections allowed per minute by the device.*/
  private int maxTotalConnectionsPerMinute = MAX_TOTAL_CONNECTIONS_PER_MIN;

  /**Total connection profile mask.*/
  private byte totalConnectionProfile[] = new byte[0];

  /**Time of the last connection.*/
  private long lastTotalConnectionTime = 0L;

  /**Maximum number of duplex connections to the device.*/
  private int maxDuplexConnections = MAX_DUPLEX_CONNECTIONS;

  /**Maximum number of duplex connections per minute to the device.*/
  private int maxDuplexConnectionsPerMinute = MAX_DUPLEX_CONNECTIONS_PER_MIN;

  /**Mask for a duplex connection.*/
  private byte duplexConnectionProfile[] = new byte[0];

  /**Length of the time of the last duplex connection.*/
  private long lastDuplexConnectionTime = 0L;

  /**
   * Create a new DeviceDB with the specified account ID and deviceID.
   * 
   * @param acctId The ID of the account.
   * @param devId The ID of the device.  Also used as the description of the device.
   */
  public DeviceDBImpl(String acctId, String devId) {
    this.accountId = acctId;
    this.deviceId = devId;
    this.description = devId;
  }

  /**
   * Get the name of the account.
   * 
   * @return The account ID of the DeviceDB.
   */
  public String getAccountName() {
    return this.accountId;
  }

  /**
   * Get the name of the device.
   * 
   * @return The device ID of the DeviceDB.
   */
  public String getDeviceName() {
    return this.deviceId;
  }

  /**
   * Get the description of the device.
   * 
   * @return The descrpition of the DeviceDB.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Check to see if the device is active.
   * 
   * @return True if the device is active, false otherwise.
   */
  public boolean isActive() {
    return this.isActive;
  }

  /**
   * Get the maximum number of allowed events for the device.
   * 
   * @return The number of events allowed by the device.
   */
  public int getMaxAllowedEvents() {
    return this.maxAllowedEvents;
  }

  /**
   * Get the number of events that occurred between two timestamps.
   * Current implementation always returns 0, as the events are not counted.
   * 
   * @param timeStart The start time of the time interval.
   * @param timeEnd The end time of the time interval.
   * @return The number of events that occurred in the interval.
   */
  public long getEventCount(long timeStart, long timeEnd) {
    return 0; // don't count events
  }

  /**
   * Get the maximum time interval between packets.
   * 
   * @return The maximum time interval between packets.
   */
  public int getLimitTimeInterval() {
    return this.limitTimeInterval;
  }

  /**
   * Get the maximum number of total connections.
   * 
   * @return The maximum number of total connections for this device.
   */
  public int getMaxTotalConnections() {
    return this.maxTotalConnections;
  }

  /**
   * Get the maximum number of total connections per minute.
   * 
   * @return The maximum number of total connections for this device per minute.
   */
  public int getMaxTotalConnectionsPerMinute() {
    return this.maxTotalConnectionsPerMinute;
  }

  /**
   * Get the byte mask for the device's total connection profile.
   * 
   * @return The total connection profile for the device.
   */
  public byte[] getTotalConnectionProfile() {
    return this.totalConnectionProfile;
  }

  /**
   * Set the byte mask for the device's total connection profile.
   * 
   * @param profile The new total connection profile for the device.
   */
  public void setTotalConnectionProfile(byte[] profile) {
    this.totalConnectionProfile = profile;
  }

  /**
   * Get the time of the last total connection.
   * 
   * @return The time of the last total connection.
   */
  public long getLastTotalConnectionTime() {
    return this.lastTotalConnectionTime;
  }

  /**
   * Set the time of the last total connection.
   * 
   * @param time The time of the total connection.
   */
  public void setLastTotalConnectionTime(long time) {
    this.lastTotalConnectionTime = time;
  }

  /**
   * Get the maximum number of duplex connections.
   * 
   * @return The maximum number of duplex connections allowed by the device.
   */
  public int getMaxDuplexConnections() {
    return this.maxDuplexConnections;
  }

  /**
   * Get the maximum number of duplex connections per minute.
   * 
   * @return The max number of duplex connections per minute allowed by the device.
   */
  public int getMaxDuplexConnectionsPerMinute() {
    return this.maxDuplexConnectionsPerMinute;
  }

  /**
   * Get the byte mask of the device's duplex connection profile.
   * 
   * @return The duplexConnectionProfile for the device.
   */
  public byte[] getDuplexConnectionProfile() {
    return this.duplexConnectionProfile;
  }

  /**
   * Set the byte mask of the device's duplex connection profile.
   * 
   * @param profile The byte string of the new duplexConnectionProfile.
   */
  public void setDuplexConnectionProfile(byte[] profile) {
    this.duplexConnectionProfile = profile;
  }

  /**
   * Get the time of the last duplex connection.
   * 
   * @return The lastDuplexConnectionTime of the device.
   */
  public long getLastDuplexConnectionTime() {
    return this.lastDuplexConnectionTime;
  }

  /**
   * Set the time of the last duplex connection.
   * 
   * @param time The time of the last duplex connection.
   */
  public void setLastDuplexConnectionTime(long time) {
    this.lastDuplexConnectionTime = time;
  }

  /**
   * Check to see if an encoding method is supported by the device.
   * 
   * @param encoding The integer string representing the type of encoding.
   * @return True if the encoding method is supported, false otherwise.
   */
  public boolean supportsEncoding(int encoding) {
    return ((SUPPORTED_ENCODING & encoding) != 0);
  }

  /**
   * Remove an encoding method currently supported by the device.
   * Does nothing if the encoding method is unsupported.
   * 
   * @param encoding The integer string representing the encoding method.
   */
  public void removeEncoding(int encoding) {
    if ((SUPPORTED_ENCODING & encoding) != 0) {
      SUPPORTED_ENCODING &= ~encoding;
    }
  }

  /**
   * Add a payload template to the collection of custom templates.
   * Uses the packet type as the key for the HashMap.
   * Returns true if the addition is successful.
   * 
   * @param template The PayloadTemplate to be added to the collection.
   * @return True if the payload template is successfully added, false otherwise.
   */
  public boolean addClientPayloadTemplate(PayloadTemplate template) {
    if (template != null) {
      int custType = template.getPacketType();
      customPayloadTemplates.put(new Integer(custType), template);
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Get the PayloadTemplate from the collection using the packet type as the key.
   * 
   * @param custType The key for the PayloadTemplate.
   * @return The PayloadTemplate from the HashMap with the specified key.
   */
  public PayloadTemplate getClientPayloadTemplate(int custType) {
    return (PayloadTemplate) customPayloadTemplates.get(new Integer(custType));
  }

  /**
   * Create a file for an event and store it in the data store directory.
   * 
   * @param geoEvent The event to be stored into the directory.
   * @return Server error code NAK_OK if the file is saved correctly, NAK_EVENT_ERROR otherwise.
   */
  public int insertEvent(GeoEvent geoEvent) {

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
    DateTime ts = new DateTime(geoEvent.getTimestamp());
    int statusCode = geoEvent.getStatusCode();
    String statusDesc = StatusCodes.GetCodeDescription(statusCode);
    GeoPoint gp = geoEvent.getGeoPoint();
    double speed = geoEvent.getSpeed();
    double heading = geoEvent.getHeading();
    double altitude = geoEvent.getAltitude();

    /* format */
    // YYYY/MM/DD,hh:mm:ss,<status>,<latitude>,<logitude>,<speed>,<heading>,<altitude>
    StringBuffer fmt = new StringBuffer();
    ts.format("yyyy/MM/dd,HH:mm:ss", fmt, null); // local TimeZone
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
    }
    catch (IOException ioe) {
      Print.logException("Unable to save to file: " + dataFile, ioe);
      return ServerErrors.NAK_EVENT_ERROR;
    }

  }
}
