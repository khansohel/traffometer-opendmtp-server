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

import org.opendmtp.server.db.AccountDB;

/**
 * Creates an account with a specified account ID.
 * 
 * @author Martin D. Flynn
 * @author George Lee
 */
public class AccountDBImpl implements AccountDB {

  /**The id of the account.*/
  private String accountId = null;

  /**
   * Creates an account with the specified account ID.
   * 
   * @param acctId The ID to be assigned to this account.
   */
  public AccountDBImpl(String acctId) {
    this.accountId = acctId;
  }

  /**
   * Get the name of the account.  Implementation simply returns the ID.
   * 
   * @return The ID of the account.
   */
  public String getAccountName() {
    return this.accountId;
  }

  /**
   * Get the description of the account.  Implementation simply returns the ID.
   * 
   * @return The ID of the account.
   */
  public String getDescription() {
    return this.accountId;
  }

  /**
   * Determine if the account is active.  Always returns true.
   * 
   * @return True if the account is active, false otherwise.
   */
  public boolean isActive() {
    return true;
  }
}
