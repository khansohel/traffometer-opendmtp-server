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
//      Integrated DBException
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
import org.opendmtp.server.db.*;

import org.opendmtp.dbtools.*;
import org.opendmtp.server_mysql.db.*;
import org.opendmtp.server_mysql.dbtypes.*;

public class AccountDBImpl
    implements AccountDB
{
    
    // ------------------------------------------------------------------------

    private Account account = null;
    
    public AccountDBImpl(Account acct) 
    {
        this.account = acct;
    }
    
    // ------------------------------------------------------------------------

    public String getAccountName() 
    {
        return this.account.getAccountID();
    }
    
    public String getDescription()
    {
        return this.account.getDescription();
    }
    
    public boolean isActive()
    {
        return this.account.getIsActive();
    }
   
    // ------------------------------------------------------------------------

    public String toString()
    {
        return (this.account != null)? this.account.toString() : "";
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static Account getAccount(String acctID, boolean create)
        throws DBException
    {
        // does not return null
        
        /* account-id specified? */
        if ((acctID == null) || acctID.equals("")) {
            throw new DBException("Account-ID not specified.");
        }

        /* get/create account */
        Account acct = null;
        Account.Key acctKey = new Account.Key(acctID);
        if (!acctKey.exists()) { // may throw DBException
            if (create) {
                acct = (Account)acctKey.getDBRecord();
                acct.setIsActive(true);
                acct.setDescription("New Device");
            } else {
                throw new DBException("Account-ID does not exists '" + acctKey + "'");
            }
        } else {
            acct = Account.getAccount(acctID); // may throw DBException
            if (acct == null) {
                throw new DBException("Unable to read existing Account-ID '" + acctKey + "'");
            }
        }
        
        return acct;
        
    }
    
    // ------------------------------------------------------------------------

    public static void createNewAccount(String acctID)
        throws DBException
    {
        Account acct = AccountDBImpl.getAccount(acctID, true);
        acct.save();
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Main admin entry point below
    
    private static final String ARG_ACCOUNT = "account";
    private static final String ARG_EDIT    = "edit";
    private static final String ARG_CREATE  = "create";

    private static void usage()
    {
        Print.logInfo("Usage:");
        Print.logInfo("  java ... " + AccountDBImpl.class.getName() + " {options}");
        Print.logInfo("Options:");
        Print.logInfo("  -create         To create a new Account");
        Print.logInfo("  -edit           To edit an existing (or newly created) Account");
        Print.logInfo("  -account=<id>   Account ID to create/edit");
        System.exit(1);
    }
    
    public static void main(String argv[])
    {
        DBConfig.init(argv,true);
        // Commands:
        //   [-create | -edit]
        //   -account=<name>
        
        /* accountID */
        String acctID = RTConfig.getString(new String[] { "acct", ARG_ACCOUNT }, "");

        /* account-id specified? */
        if ((acctID == null) || acctID.equals("")) {
            Print.logError("Account-ID not specified.");
            usage();
        }
        
        /* account exists? */
        boolean accountExists = false;
        try {
            accountExists = Account.exists(acctID);
        } catch (DBException dbe) {
            Print.logError("Error determining if Account exists: " + acctID);
            System.exit(99);
        }
        
        /* option count */
        int opts = 0;

        /* create default account */
        if (RTConfig.getBoolean(ARG_CREATE, false)) {
            opts++;
            if (accountExists) {
                Print.logWarn("Account already exists: " + acctID);
            } else {
                try {
                    AccountDBImpl.createNewAccount(acctID);
                    Print.logInfo("Created Account: " + acctID);
                } catch (DBException dbe) {
                    Print.logError("Error creating Account: " + acctID);
                    dbe.printException();
                    System.exit(99);
                }
            }
        }

        /* edit */
        if (RTConfig.getBoolean(ARG_EDIT, false)) {
            opts++;
            if (!accountExists) {
                Print.logError("Account does not exist: " + acctID);
            } else {
                try {
                    Account account = AccountDBImpl.getAccount(acctID,false); // may throw DBException
                    DBEdit  editor  = new DBEdit(account);
                    editor.edit(); // may throw IOException
                } catch (IOException ioe) {
                    if (ioe instanceof EOFException) {
                        Print.logError("End of input");
                    } else {
                        Print.logError("IO Error");
                    }
                } catch (DBException dbe) {
                    Print.logError("Error editing Account: " + acctID);
                    dbe.printException();
                    System.exit(99);
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
