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
// ----------------------------------------------------------------------------
package org.opendmtp.server.db;

import org.opendmtp.util.GeoEvent;

/**
 * DeviceDB provides interface for devices in database using OpenDMTP.
 * @author Martin D. Flynn
 * @author Brandon Horiuchi
 */
public interface DeviceDB {

  /**
   * Get the device account name.
   * @return String account name.
   */
  public String getAccountName();

  /**
   * Get the device name.
   * @return String device name.
   */
  public String getDeviceName();

  /**
   * Get device description.
   * @return String description.
   */
  public String getDescription();

  /**
   * Get the activity status of the device. 
   * @return Boolean true if the device is active, false otherwise.
   */
  public boolean isActive();
  
  /**
   * Get the maximum number of allowed events for the device.
   * @return Number of maximum allowed events.
   */
  public int getMaxAllowedEvents();

  /**
   * Get the number of events logged by the device during time interval.
   * @param timeStart Long integer start time.
   * @param timeEnd Long integer end time.
   * @return Number of events.
   */
  public long getEventCount(long timeStart, long timeEnd);

  /**
   * Get device maximum time interval.
   * @return Maximum time interval.
   */
  public int getLimitTimeInterval();

  /**
   * Get device maximum total connections.
   * @return Number of maximum total connections.
   */
  public int getMaxTotalConnections();

  /**
   * Get device maximum total connections per minute.
   * @return Number of maximum total connections per minute.
   */
  public int getMaxTotalConnectionsPerMinute();
  
  /**
   * Get device total connection profile.
   * @return Total connection profile.
   */
  public byte[] getTotalConnectionProfile();

  /**
   * Set device total connnection profile.
   * @param profile Byte array containing connection profile.
   */
  public void setTotalConnectionProfile(byte[] profile);

  /**
   * Get device last total connection time.
   * @return Time of the last total connection.
   */
  public long getLastTotalConnectionTime();

  /**
   * Set device last total connection time.
   * @param connectTime Time of the last total connection.
   */
  public void setLastTotalConnectionTime(long connectTime);

  /**
   * Get device maximum duplex connections.
   * @return Number of maximum duplex connections.
   */
  public int getMaxDuplexConnections();

  /**
   * Get device maximum duplex connections per minute.
   * @return Number of maximum duplex connections per minute.
   */
  public int getMaxDuplexConnectionsPerMinute();

  /**
   * Get device duplex connection profile.
   * @return Byte array containing duplex connection profile.
   */
  public byte[] getDuplexConnectionProfile();

  /**
   * Set device duplex connection profile.
   * @param profile Byte array containing duplex connection profile.
   */
  public void setDuplexConnectionProfile(byte[] profile);

  /**
   * Get device last duplex connection time.
   * @return Last duplex connection time.
   */
  public long getLastDuplexConnectionTime();

  /**
   * Set device last duplex connection time.
   * @param connectTime Last duplex connection time.
   */
  public void setLastDuplexConnectionTime(long connectTime);

  /**
   * Check if device supports type of encoding.
   * @param encoding Type of encoding to check for support.
   * @return Boolean true if encoding is supported, false otherwise.
   */
  public boolean supportsEncoding(int encoding);

  /**
   * Remove encoding type for device.
   * @param encoding Integer representing type of encoding.
   */
  public void removeEncoding(int encoding);

  /**
   * Add client payload template for device.
   * @param template Payload template to add.
   * @return Boolean true if template added successfully, false otherwise.
   */
  public boolean addClientPayloadTemplate(PayloadTemplate template);

  /**
   * Get client payload template for device.
   * @param custType Integer payload template custom type.
   * @return Client payload template
   */
  public PayloadTemplate getClientPayloadTemplate(int custType);

  /**
   * Insert event for device.
   * @param event Event to insert.
   * @return Integer insertion confirmation.
   */
  public int insertEvent(GeoEvent event);

}
