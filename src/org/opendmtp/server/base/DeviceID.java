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
package org.opendmtp.server.base;

import org.opendmtp.codes.ServerErrors;
import org.opendmtp.server.db.DeviceDB;
import org.opendmtp.server.db.PayloadTemplate;
import org.opendmtp.util.DateTime;
import org.opendmtp.util.Print;
import org.opendmtp.util.StringTools;

/**
 * DeviceID contains device related objects AccountID and DeviceID as well as 
 * various status conditions.
 * 
 * @author Martin D. Flynn
 * @author Brandon Lee
 */
public class DeviceID {

  // ------------------------------------------------------------------------
  // - DeviceID
  // - AccountID name [key]
  // - DeviceID name [key]
  // - UniqueID code [altKey]
  // - Event notification email
  // - isActive
  // - Supported encodings
  // - Time of last connection
  // - Time interval over which below limits apply
  // - Max events per unit time
  // - Total connection profile mask
  // - Max total connections per unit time
  // - Max total connections per minute (0..3)
  // - Duplex connection profile mask
  // - Max duplex connections per unit time
  // - Max duplex connections per minute (0..3)
  // - DeviceIDErrors (logged errors)
  // - AccountID name [key]
  // - DeviceID name [key]
  // - ArrivalTime [key]
  // - Index [key] (uniquifier)
  // - Error packet
  // - CustomTemplate
  // - AccountID name [key]
  // - DeviceID name [key]
  // - Packet header [key]
  // - Custom template type [key]
  // - Custom template definition

  // device ID
  private DeviceDB db = null;
  private AccountID accountId = null;
  private ValidateConnection connectionValidator = null;

  /**
   * Returns the DeviceDB by unique ID or null.
   * 
   * @param uniqId the unique Id of the deviceDB.
   * @return the deviceDB or null.
   * @throws PacketParseException if device is null.
   */
  private static DeviceDB GetDeviceDB(UniqueID uniqId) throws PacketParseException {
    
    DMTPServer.DBFactory fact = DMTPServer.getDBFactory();
    if (fact != null) {
      long id = uniqId.getId();
      DeviceDB db = fact.getDeviceDB(id);
      if (db != null) {
        return db;
      }
      else {
        Print.logError("Device not found: UniqueID " + StringTools.toHexString(id, 48));
        // fall through
      }
    }
    return null;
  }

  /**
   * Returns the DeviceDB by accountID and device name. Returns null if not found.
   * 
   * @param acctId the AccountID of the device.
   * @param devName the string name of the device.
   * @return the specified device or null if not found.
   * @throws PacketParseException not sure how.
   */
  private static DeviceDB GetDeviceDB(AccountID acctId, String devName) throws PacketParseException {
    DMTPServer.DBFactory fact = DMTPServer.getDBFactory();
    if (fact != null) {
      String acctName = acctId.getAccountName();
      DeviceDB db = fact.getDeviceDB(acctName, devName);
      if (db != null) {
        return db;
      }
      else {
        Print.logError("Device not found: Acct/Dev " + acctName + "/" + devName);
        // fall through
      }
    }
    return null;
  }

  /**
   * Calls constructor.
   * 
   * @param uniqId the id of the device.
   * @return DeviceId the new instance of uniqId.
   * @throws PacketParseException if param is not valid.
   */
  public static DeviceID loadDeviceID(UniqueID uniqId) throws PacketParseException {
    
    return new DeviceID(uniqId);
  }

  /**
   * Call constructor. 
   * 
   * @param acctId the accountID .
   * @param devName the name of the device.
   * @return DeviceId new instance of DeviceID of parameters.
   * @throws PacketParseException if there is an error in given parameters.
   */
  public static DeviceID loadDeviceID(AccountID acctId, String devName) throws PacketParseException {
    return new DeviceID(acctId, devName);
  }

  /**
   * Private constructor.  Assigns accountId to instance if uniqId is ok.
   * 
   * @param uniqId the id of the device.
   * @throws PacketParseException If device doesn't exist.
   */
  private DeviceID(UniqueID uniqId) throws PacketParseException {

    // validate arguments 
    if ((uniqId == null) || !uniqId.isValid()) {
      Print.logError("UniqueID is invalid: " + uniqId);
      throw new PacketParseException(ServerErrors.NAK_ID_INVALID, null); // errData ok
    }

    // device exists? 
    this.db = GetDeviceDB(uniqId);
    if (this.db == null) {
      Print.logError("UniqueID not found: " + uniqId);
      throw new PacketParseException(ServerErrors.NAK_ID_INVALID, null); // errData ok
    }

    // account
    this.accountId = AccountID.loadAccountID(this.db.getAccountName());
    Print.logInfo("Loaded device: " + this);
  }

  /**
   * Constructor.  Assigns device of accountId and Device name if exists.
   * 
   * @param acctId the accountId.
   * @param devName the name of the device.
   * @throws PacketParseException if device doesn't exsit.
   */
  private DeviceID(AccountID acctId, String devName) throws PacketParseException {

    // validate arguments 
    if ((devName == null) || devName.equals("")) {
      Print.logError("Device name is null/empty");
      throw new PacketParseException(ServerErrors.NAK_DEVICE_INVALID, null); // errData ok
    }
    else if (acctId == null) {
      Print.logError("AccountID is null");
      throw new PacketParseException(ServerErrors.NAK_ACCOUNT_INVALID, null); // errData ok
    }

    // device exists? 
    this.db = GetDeviceDB(acctId, devName);
    if (this.db == null) {
      Print.logError("Device not found: " + devName);
      throw new PacketParseException(ServerErrors.NAK_DEVICE_INVALID, null); // errData ok
    }

    // account 
    this.accountId = acctId;
    Print.logInfo("Loaded device: " + this);

  }
  
  /**
   * Returns the device name.
   * 
   * @return the string with device name.
   */
  public String getDeviceName() {
    return this.db.getDeviceName();
  }

  /**
   * Returns AccountID.
   * 
   * @return AccountId object.
   */
  public AccountID getAccountID() {
    return this.accountId;
  }

  /**
   * Returns boolean of device status.
   * 
   * @return true if device is active
   */
  public boolean isActive() {
    // check to see if this device is still active in the database
    return this.db.isActive();
  }

  /**
   * Returns a ValidateConnection object.
   * 
   * @return the current connection validator object, if null creates a new one and sends that.
   */
  public ValidateConnection getConnectionValidator() {
    
    if (this.connectionValidator == null) {
      this.connectionValidator = new ValidateConnection(this.db.getLimitTimeInterval());
    }
    return this.connectionValidator;
  }

  /**
   * Returns true if connections are of allowable per time period for all connections.
   * Sets total connections and connection time.
   * 
   * @param isDuplex lets method know id connection is duplex or not.
   * @return returns true if all is ok else returns false.
   */
  public boolean markAndValidateConnection(boolean isDuplex) {
    
    long nowTime = DateTime.getCurrentTimeSec();

    // test total connections 
    byte totalConn[] = this.getConnectionValidator().markConnection(
        this.db.getMaxTotalConnections(), this.db.getMaxTotalConnectionsPerMinute(),
        this.db.getTotalConnectionProfile(), (nowTime - this.db.getLastTotalConnectionTime()));
    
    if (totalConn == null) {
      
      // exceed total allowable connections per time period
      Print.logError("Exceeded total allowable conections");
      return false;
    }
    this.db.setTotalConnectionProfile(totalConn); // may be redundant
    this.db.setLastTotalConnectionTime(nowTime);

    // test duplex connections 
    if (isDuplex) {
      byte duplexConn[] = this.getConnectionValidator().markConnection(
          this.db.getMaxDuplexConnections(), this.db.getMaxDuplexConnectionsPerMinute(),
          this.db.getDuplexConnectionProfile(), (nowTime - this.db.getLastDuplexConnectionTime()));
      if (duplexConn == null) {
        // exceed allowable duplex connections per time period
        Print.logError("Exceeded allowable duplex conections");
        return false;
      }
      this.db.setDuplexConnectionProfile(duplexConn); // may be redundant
      this.db.setLastDuplexConnectionTime(nowTime);
    }

    // save total/duplex connections mask 
    return true;

  }

  /**
   * Checks if Device supports enconding.
   * 
   * @param encoding the encoding to check.
   * @return true if supports encoding, false otherwise.
   */
  public boolean supportsEncoding(int encoding) {
    return this.db.supportsEncoding(encoding);
  }

  /**
   * Removes encoding from device.
   * 
   * @param encoding the encoding to remove.
   */
  public void removeEncoding(int encoding) {
    this.db.removeEncoding(encoding);
  }

  /**
   * Adds client PayloadTemplate.
   * 
   * @param template the template to add.
   * @return returns if it was successful or not.
   */
  public boolean addClientPayloadTemplate(PayloadTemplate template) {
    return this.db.addClientPayloadTemplate(template);
  }

  /**
   * Returns client payload template.
   * 
   * @param custType specifies the payload type.
   * @return the PayloadTemplate object of client.
   */
  public PayloadTemplate getClientPayloadTemplate(int custType) {
    return this.db.getClientPayloadTemplate(custType);
  }

  /**
   * Saves event.
   * 
   * @param event the event to save.
   * @return error interger code.
   */
  public int saveEvent(Event event) {
    
    //never used variable.
    Packet packet = event.getPacket();

    long timeEnd = DateTime.getCurrentTimeSec();
    long timeStart = timeEnd - DateTime.MinuteSeconds(this.db.getLimitTimeInterval());
    if ((this.db.getMaxAllowedEvents() > 0)
        && (this.db.getEventCount(timeStart, timeEnd) >= this.db.getMaxAllowedEvents())) {

      // excessive events
      Print.logError("Excessive events");
      return ServerErrors.NAK_EXCESSIVE_EVENTS;

    }
    else {

      // insert event 
      int err = this.db.insertEvent(event.getGeoEvent());
      // ServerErrors.NAK_DUPLICATE_EVENT
      // ServerErrors.NAK_EVENT_ERROR
      // ServerErrors.NAK_OK
      return err;
    }
  }

  /**
   * Loging error.
   * Could be for future implimentation, Nothing in method.
   * 
   * @param errCode type of error?
   * @param errData maybe the error to log?
   */
  public void logError(int errCode, byte errData[]) {
  }

  /**
   * Logging Diagonostics.
   * Could be for future implimentation, Nothing in method.
   * 
   * @param diagCode type of code?
   * @param diagData maybe the data to log?
   */
  public void logDiagnostic(int diagCode, byte diagData[]) {
  }

  /**
   * Logging properties.
   * Could be for future implimentation,  Nothing in method.
   * 
   * @param propKey type of error?
   * @param propVal maybe the error to log?
   */
  public void logProperty(int propKey, byte propVal[]) {
  }

  /**
   * Returns device's contents into a string.
   * 
   * @return a string that contains device contents or an empty string.
   */
  public String toString() {
    return (this.db != null) ? this.db.toString() : "";
  }
}
