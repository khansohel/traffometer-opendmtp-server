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

/**
 * AccountDB provides a simple interface for account type classes.
 * @author Martin D. Flynn
 * @author Brandon Horiuchi
 */
public interface AccountDB {
  
  /**
   * Get the name of the account.
   * @return String account name.
   */
  public String getAccountName();

  /**
   * Get the description of the account.
   * @return description of the account.
   */
  public String getDescription();

  /**
   * Get the activity status of the account.
   * @return True if the account is active, false otherwise.
   */
  public boolean isActive();
}
