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

import java.io.EOFException;
import java.io.IOException;

import org.opendmtp.dbtools.DBEdit;
import org.opendmtp.dbtools.DBException;
import org.opendmtp.server.db.AccountDB;
import org.opendmtp.server_mysql.db.Account;
import org.opendmtp.util.Print;
import org.opendmtp.util.RTConfig;


/**
 * Stores an account and provides access functions to its fields.
 * 
 * @author Martin D. Flynn
 * @author George Lee
 * 
 */
public class AccountDBImpl implements AccountDB {

  /** The account of this AccountDB. */
  private Account account = null;

  /**
   * Creates a new AccountDBImpl with the specified Account.
   * 
   * @param acct The account to be used with this AccountDB.
   */
  public AccountDBImpl(Account acct) {
    this.account = acct;
  }

  /**
   * Get the name of the account. The ID of the account is used as the name.
   * 
   * @return The account ID of the account.
   */
  public String getAccountName() {
    return this.account.getAccountID();
  }

  /**
   * Get the description of the account.
   * 
   * @return The account's descrption.
   */
  public String getDescription() {
    return this.account.getDescription();
  }

  /**
   * Get the activity status of the account.
   * 
   * @return True if the account is active, false otherwise.
   */
  public boolean isActive() {
    return this.account.getIsActive();
  }

  /**
   * Return a string representation of the account.
   * 
   * @return The string returned by the account.toString() method. Returns the empty string if the
   *         account is not initialized.
   */
  public String toString() {
    return (this.account != null) ? this.account.toString() : "";
  }

  /**
   * Either creates the account with the specified ID or retrieves the account with the specified
   * ID. Throws an exception if retrieving the account with the specified ID fails, the account ID
   * is not specified, or if the account is not initialized.
   * 
   * @param acctID The ID of the account to be retrieved or created.
   * @param create Set to true to create an account with the specified ID. Set to false to search
   *        for the account with the specified ID.
   * @return A new or existing Account with the specified ID.
   * @throws DBException Exception is thrown if retrieval of the Account fails, if the ID is not
   *         specified, if there is an error in determining if the account exists, or if the account
   *         is null.
   */
  public static Account getAccount(String acctID, boolean create) throws DBException {
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
        acct = (Account) acctKey.getDBRecord();
        acct.setIsActive(true);
        acct.setDescription("New Device");
      }
      else {
        throw new DBException("Account-ID does not exists '" + acctKey + "'");
      }
    }
    else {
      acct = Account.getAccount(acctID); // may throw DBException
      if (acct == null) {
        throw new DBException("Unable to read existing Account-ID '" + acctKey + "'");
      }
    }

    return acct;

  }

  /**
   * Creates a new account with the specified account ID. Simply uses the getAccount() method with
   * the create parameter set to true.
   * 
   * @param acctID The ID of the account to be created.
   * 
   * @throws DBException Exception thrown if there is an error in checking if the ID exists.
   */
  public static void createNewAccount(String acctID) throws DBException {
    Account acct = AccountDBImpl.getAccount(acctID, true);
    acct.save();
  }

  private static final String ARG_ACCOUNT = "account";
  private static final String ARG_EDIT = "edit";
  private static final String ARG_CREATE = "create";

  /**
   * Display usage information about using this class.
   */
  private static void usage() {
    Print.logInfo("Usage:");
    Print.logInfo("  java ... " + AccountDBImpl.class.getName() + " {options}");
    Print.logInfo("Options:");
    Print.logInfo("  -create         To create a new Account");
    Print.logInfo("  -edit           To edit an existing (or newly created) Account");
    Print.logInfo("  -account=<id>   Account ID to create/edit");
    System.exit(1);
  }

  /**
   * Creates and/or edits a default user account. Initializes the configuration using parameters
   * from the command line. Displays error information if there are problems in initializing the
   * account.
   * 
   * @param argv Command line parameters to pass to DBConfig.
   */
  public static void main(String argv[]) {
    DBConfig.init(argv, true);
    // Commands:
    // [-create | -edit]
    // -account=<name>

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
    }
    catch (DBException dbe) {
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
      }
      else {
        try {
          AccountDBImpl.createNewAccount(acctID);
          Print.logInfo("Created Account: " + acctID);
        }
        catch (DBException dbe) {
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
      }
      else {
        try {
          Account account = AccountDBImpl.getAccount(acctID, false); // may throw DBException
          DBEdit editor = new DBEdit(account);
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
