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
//      Integrated 'DBException'
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.server_mysql;

import org.opendmtp.dbtools.DBAdmin;
import org.opendmtp.dbtools.DBException;
import org.opendmtp.dbtools.DBFactory;
import org.opendmtp.server.db.AccountDB;
import org.opendmtp.server.db.DeviceDB;
import org.opendmtp.server_mysql.db.Account;
import org.opendmtp.server_mysql.db.Device;
import org.opendmtp.server_mysql.db.EventData;
import org.opendmtp.server_mysql.db.EventTemplate;
import org.opendmtp.server.base.DMTPServer;
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

  /**Stores the factory classes for the Account, Device, EventTemplate, and EventData classes.*/
  private static DBFactory DB_TABLES[] = { Account.getFactory(), Device.getFactory(),
      EventTemplate.getFactory(), EventData.getFactory(), };

  /**
   * Class that retrieves Account DB's and Device DB's.
   * 
   * @author Martin D. Flynn
   * @author George Lee
   */
  private static class DMTP_DBFactory implements DMTPServer.DBFactory {

    /**
     * Get the AccountDB with the specified account name. Returns null if the account is not found
     * or if there is an error in retrieving the account.
     * 
     * @param acctName The name of the account to be retrieved.
     * @return The AccountDBImpl whose Account has the specified account name.
     */
    public AccountDB getAccountDB(String acctName) {
      try {
        Account db = Account.getAccount(acctName);
        return (db != null) ? new AccountDBImpl(db) : null;
      }
      catch (DBException dbe) {
        dbe.printException();
        return null;
      }
    }

    /**
     * Gets the DeviceDB with the specified ID. Returns null if the device is not found or if there
     * is an error in retrieving the DeviceDB.
     * 
     * @param uniqId The unique ID of the device.
     * @return A DeviceDBImpl where the device has the specified ID.
     */
    public DeviceDB getDeviceDB(long uniqId) {
      try {
        Device db = Device.getDevice(uniqId);
        return (db != null) ? new DeviceDBImpl(db) : null;
      }
      catch (DBException dbe) {
        dbe.printException();
        return null;
      }
    }

    /**
     * Gets the DeviceDB with the specified ID and device name. Returns null if the device is not
     * found or if there is an error in retrieving the DeviceDB.
     * 
     * @param acctId The account ID associated with the device to be retrieved..
     * @param devName The name of the device to be retrieved.
     * @return A DeviceDBImpl where the device has the specified account ID and device name.
     */
    public DeviceDB getDeviceDB(String acctId, String devName) {
      try {
        Device db = Device.getDevice(acctId, devName);
        return (db != null) ? new DeviceDBImpl(db) : null;
      }
      catch (DBException dbe) {
        dbe.printException();
        return null;
      }
    }
  }

  /**Initialization status of the class.  Class is uninitialized until init is called.*/
  private static boolean didInit = false;

  /**
   * Initializes the DBConfig class.  Sets options and registers database with the server.
   * 
   * @param argv Command line options that are passed to RTConfig.
   * @param interactive Sets RTConfig options when set to true.  Does nothing if set to false.
   */
  public static void init(String argv[], boolean interactive) {
    if (!didInit) {
      didInit = true;

      /* command line options */
      RTConfig.setCommandLineArgs(argv);
      if (interactive) {
        RTConfig.setFile(RTKey.LOG_FILE, null); // no log file
        Print.setLogHeaderLevel(Print.LOG_WARN); // include log header on WARN/ERROR/FATAL
        RTConfig.setBoolean(RTKey.LOG_INCL_DATE, false); // exclude date
        RTConfig.setBoolean(RTKey.LOG_INCL_STACKFRAME, true); // include stackframe
      }
      else {
        //RTConfig.setBoolean(RTKey.LOG_INCL_DATE, true);
        //RTConfig.setBoolean(RTKey.LOG_INCL_STACKFRAME, false);
      }

      /* set database access info */
      RTConfig.setString(RTKey.DB_NAME, "dmtp");
      RTConfig.setString(RTKey.DB_HOST, "localhost");
      RTConfig.setString(RTKey.DB_USER, "dmtp");
      RTConfig.setString(RTKey.DB_PASS, "opendmtp");

      /* register tables */
      DBAdmin.addTableFactories(DB_TABLES);

      /* register OpenDMTP protocol DB interface */
      DMTPServer.setDBFactory(new DBConfig.DMTP_DBFactory());

    }

  }

  /**
   * Main method for the DBConfig class. Requires the use of a root username and password. Sets
   * these fields if they are not initialized. Sets up the RTConfig and creates a default account
   * and device.
   * 
   * @param argv Command line paramters to pass to the init() method.
   */
  public static void main(String argv[]) {
    DBConfig.init(argv, true);

    /* default 'rootUser'/'rootPass' */
    // The following may be required for some of the following operations
    //  -rootUser=<root>
    //  -rootPass=<pass>
    if (!RTConfig.hasProperty("rootUser")) {
      // set default root user/pass
      RTConfig.setString("rootUser", "root");
      RTConfig.setString("rootPass", "");
    }
    else if (!RTConfig.hasProperty("rootPass")) {
      // 'rootUser' has been specified, but 'rootPass' is missing.
      RTConfig.setString("rootPass", "");
    }

    /* create tables */
    if (RTConfig.getBoolean("initTables", false)) {
      RTConfig.setBoolean("createdb", true);
      RTConfig.setBoolean("grant", true);
      RTConfig.setBoolean("tables", true);
      // The following are required:
      //   -rootUser=<root>
      //   -rootPass=<pass>
      // These should be available in the config file
      //   db.sql.user=<Grant_User>       - GRANT only
      //   db.sql.pass=<Grant_Pass>       - GRANT only
      //   db.sql.name=<DataBase_Name>    - GRANT only
    }

    /* execute commands present in run-time config */
    if (!DBAdmin.execCommands()) {
      System.exit(1);
    }

    /* create a default account */
    if (RTConfig.hasProperty("newAccount")) {
      String acctID = RTConfig.getString("newAccount", null);
      try {
        AccountDBImpl.createNewAccount(acctID);
        Print.logInfo("Created account:" + acctID);
      }
      catch (DBException dbe) {
        Print.logException("Error creating account:" + acctID, dbe);
      }
    }

    /* create a default device */
    if (RTConfig.hasProperty("newDevice")) {
      String acctID = RTConfig.getString(new String[] { "account", "newAccount" }, null);
      String devID = RTConfig.getString("newDevice", null);
      try {
        DeviceDBImpl.createNewDevice(acctID, devID);
        Print.logInfo("Created device: " + acctID + "," + devID);
      }
      catch (DBException dbe) {
        Print.logException("Error creating account:device: " + acctID + "," + devID, dbe);
      }
    }

  }

}
