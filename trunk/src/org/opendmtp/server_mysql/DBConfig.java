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

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opendmtp.util.*;
import org.opendmtp.dbtools.*;

import org.opendmtp.server.base.*;
import org.opendmtp.server.db.*;

import org.opendmtp.server_mysql.db.*;
import org.opendmtp.server_mysql.dbtypes.*;

public class DBConfig
{

    // ------------------------------------------------------------------------

    private static DBFactory DB_TABLES[] = {
        Account.getFactory(),
        Device.getFactory(),
        EventTemplate.getFactory(),
        EventData.getFactory(),
    };

    // ------------------------------------------------------------------------

    private static class DMTP_DBFactory
        implements DMTPServer.DBFactory
    {
        public AccountDB getAccountDB(String acctName) {
            try {
                Account db = Account.getAccount(acctName);
                return (db != null)? new AccountDBImpl(db) : null;
            } catch (DBException dbe) {
                dbe.printException();
                return null;
            }
        }
        public DeviceDB  getDeviceDB(long uniqId) {
            try {
                Device db = Device.getDevice(uniqId);
                return (db != null)? new DeviceDBImpl(db) : null;
            } catch (DBException dbe) {
                dbe.printException();
                return null;
            }
        }
        public DeviceDB  getDeviceDB(String acctId, String devName) {
            try {
                Device db = Device.getDevice(acctId, devName);
                return (db != null)? new DeviceDBImpl(db) : null;
            } catch (DBException dbe) {
                dbe.printException();
                return null;
            }
        }
    }

    // ------------------------------------------------------------------------

    private static boolean didInit = false;
    
    public static void init(String argv[], boolean interactive)
    {
        if (!didInit) {
            didInit = true;

            /* command line options */
            RTConfig.setCommandLineArgs(argv);
            if (interactive) {
                RTConfig.setFile(RTKey.LOG_FILE,null);      // no log file
                Print.setLogHeaderLevel(Print.LOG_WARN);    // include log header on WARN/ERROR/FATAL
                RTConfig.setBoolean(RTKey.LOG_INCL_DATE, false);        // exclude date
                RTConfig.setBoolean(RTKey.LOG_INCL_STACKFRAME, true);   // include stackframe
            } else {
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
    
    // ------------------------------------------------------------------------
    
    public static void main(String argv[])
    {
        DBConfig.init(argv,true);
        
        /* default 'rootUser'/'rootPass' */
        // The following may be required for some of the following operations
        //  -rootUser=<root>
        //  -rootPass=<pass>
        if (!RTConfig.hasProperty("rootUser")) {
            // set default root user/pass
            RTConfig.setString("rootUser", "root");
            RTConfig.setString("rootPass", "");
        } else
        if (!RTConfig.hasProperty("rootPass")) {
            // 'rootUser' has been specified, but 'rootPass' is missing.
            RTConfig.setString("rootPass", "");
        }
        
        /* create tables */
        if (RTConfig.getBoolean("initTables",false)) {
            RTConfig.setBoolean("createdb", true);
            RTConfig.setBoolean("grant"   , true);
            RTConfig.setBoolean("tables"  , true);
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
            } catch (DBException dbe) {
                Print.logException("Error creating account:" + acctID, dbe);
            }
        }

        /* create a default device */
        if (RTConfig.hasProperty("newDevice")) {
            String acctID = RTConfig.getString(new String[] { "account", "newAccount" }, null);
            String devID  = RTConfig.getString("newDevice", null);
            try {
                DeviceDBImpl.createNewDevice(acctID, devID);
                Print.logInfo("Created device: " + acctID + "," + devID);
            } catch (DBException dbe) {
                Print.logException("Error creating account:device: " + acctID + "," + devID, dbe);
            }
        }
        
    }
    
}
