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
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.server_file;

import java.io.File;

import org.opendmtp.server.base.DMTPServer;
import org.opendmtp.server.db.AccountDB;
import org.opendmtp.server.db.DeviceDB;

import org.opendmtp.util.Print;
import org.opendmtp.util.RTConfig;
import org.opendmtp.util.RTKey;

/**
 * Configures and initializes a new database for the DMTP Server. Also provides a class that creates
 * Device and Account DB's.
 * 
 * @author Martin D. Flynn
 * @author George Lee
 */
public class DBConfig {

  /**
   * Contains methods that create new AccountDBImpl's and DeviceDBImpl's.
   * 
   * @author Martin D. Flynn
   * @author George Lee
   */
  private static class DMTP_DBFactory implements DMTPServer.DBFactory {

    /**
     * Create a new Account DB.
     * 
     * @param acctName The name or ID of the account.
     * @return A new AccountDBImpl object.
     */
    public AccountDB getAccountDB(String acctName) {
      return new AccountDBImpl(acctName);
    }

    /**
     * Create a new Device DB. This method always returns null.
     * 
     * @param uniqId The ID of the device.
     * @return Returns null always.
     */
    public DeviceDB getDeviceDB(long uniqId) {
      return null;
    }

    /**
     * Create a new Device DB with the specified ID and name.
     * 
     * @param acctId The account ID for the deviceDB.
     * @param devName The name of the device for the deviceDB.
     * @return A new DeviceDB with the specified account ID and device name.
     */
    public DeviceDB getDeviceDB(String acctId, String devName) {
      return new DeviceDBImpl(acctId, devName);
    }
  }

  /**
   * Initialize the server and the RTConfig. Also sets up the data store directory for the device.
   * 
   * @param argv Command line parameters to pass to RTConfig.
   * @param interactive Sets RTConfig options when set to true. Does nothing if set to false.
   */
  public static void init(String argv[], boolean interactive) {

    /* command line options */
    RTConfig.setCommandLineArgs(argv);
    if (interactive) {
      RTConfig.setFile(RTKey.LOG_FILE, null); // no log file
      Print.setLogHeaderLevel(Print.LOG_WARN); // include log header on WARN/ERROR/FATAL
      RTConfig.setBoolean(RTKey.LOG_INCL_DATE, false); // exclude date
      RTConfig.setBoolean(RTKey.LOG_INCL_STACKFRAME, true); // include stackframe
    }
    else {
      // RTConfig.setBoolean(RTKey.LOG_INCL_DATE, true);
      // RTConfig.setBoolean(RTKey.LOG_INCL_STACKFRAME, false);
    }

    /* set the data-store directory for the received events */
    File storeDir = RTConfig.getFile(Main.ARG_STOREDIR, null);
    DeviceDBImpl.setDataStoreDirectory(storeDir);
    Print.logInfo("Account/Device Events will be stored in directory '"
        + DeviceDBImpl.getDataStoreDirectory() + "'");

    /* register OpenDMTP protocol DB interface */
    DMTPServer.setDBFactory(new DBConfig.DMTP_DBFactory());

  }
}
