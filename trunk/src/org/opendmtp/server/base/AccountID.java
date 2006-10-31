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
import org.opendmtp.server.db.AccountDB;
import org.opendmtp.util.Print;



/**
 * AccountID, seems to provide access to existing AccountDB information to check if it is
 * active, name, and a toString method.  Uses a private variable but and a private static 
 * constructor, but public accessors and a public static method to "load account".
 * 
 * @author Martin D. Flynn
 * @author Brandon Lee
 */
public class AccountID {

  // - AccountID
  // - AccountID name [key]
  // - Owner name
  // - Contact email
  // - Event notification email
  // - Account level of service

  /**
   * Identification, used for varification.
   */
  private AccountDB db = null;

  /**
   * Returns a AccountDB from the DMTPServer.DBFactory.  
   * If not found returns null.
   * 
   * @param acctName containing the accountID.
   * @return AccountDB contains the account specified or null.
   * @throws PacketParseException not sure why.
   */
  private static AccountDB GetAccountDB(String acctName) throws PacketParseException {
    
    DMTPServer.DBFactory fact = DMTPServer.getDBFactory();
    
    if (fact != null) {
      AccountDB db = fact.getAccountDB(acctName);
      if (db != null) {
        return db;
      }
    }
    else {
      Print.logError("No factory registered for AccountID");
    }
    return null;
  }

  /**
   * Calls the private constructor to create a new public AccountID instance with param 
   * acctName then returns that instance.
   * 
   * @param acctName a string containing the DBAccount name or ID.
   * @return The instance of the newly created AccountID  the new.
   * @throws PacketParseException if name equals null or "".
   */
  public static AccountID loadAccountID(String acctName) throws PacketParseException {
    
    return new AccountID(acctName);
  }

  /**
   * Private constructor that attaches an DBAcount to the private variable db.  db can result
   * in either null, or the desired AccountID else.  Throws a PacketParseException if the result
   * is null, string acctName is null or empty.
   * 
   * @param acctName the name of the new AccountID
   * @throws PacketParseException throws exception if acctName or bad or not found.
   */
  private AccountID(String acctName) throws PacketParseException {

    // throws exception if acctName is null or ""
    if ((acctName == null) || acctName.equals("")) {
      
      Print.logError("Account name is null/empty");
      throw new PacketParseException(ServerErrors.NAK_ACCOUNT_INVALID, null); // errData ok
    }
    
    // continues if name is ok, throws exception if account doesn't exsist else set to db.
    this.db = GetAccountDB(acctName);
    if (this.db == null) {
      
      Print.logError("AccountID not found: " + acctName);
      throw new PacketParseException(ServerErrors.NAK_ACCOUNT_INVALID, null); // errData ok
    } //end if
  }

  /**
   * Returns a String with the accounts name.
   * 
   * @return String containing the account's name.
   */
  public String getAccountName() {
    
    return this.db.getAccountName();
  }

  /**
   * Checks to see if the database is active.
   * 
   * @return boolean true if active false otherwise.
   */
  public boolean isActive() {
    
    return this.db.isActive();
  }

  /**
   * Overwrites the toString method and returns db as a string or null.
   * 
   * @return String the db as a string.
   */
  public String toString() {
    
    return (this.db != null) ? this.db.toString() : "";
  }
}
