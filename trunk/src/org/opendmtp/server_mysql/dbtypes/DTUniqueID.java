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
package org.opendmtp.server_mysql.dbtypes;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.opendmtp.dbtools.DBFieldType;
import org.opendmtp.util.StringTools;

/**
 * Wrapper for unique ID type.
 * 
 * @author Martin D. Flynn
 * @author Robert S. Brewer
 */
public class DTUniqueID extends DBFieldType {

  // ------------------------------------------------------------------------

  private long uniqueID = 0L;

  /**
   * Initializes unique ID with provided value.
   * 
   * @param uniqueID provided unique ID.
   */
  public DTUniqueID(long uniqueID) {
    this.uniqueID = uniqueID;
  }

  /**
   * Initializes unique ID with value provided in String.
   * 
   * @param uniqueIDHex String containing unique ID. If 'uniqueIDHex' represents a
   * 'HEX' value, then it must begin with '0x'. Otherwise it will be parsed as a 'Long'.
   */
  public DTUniqueID(String uniqueIDHex) {
    super(uniqueIDHex);
    this.uniqueID = StringTools.parseLong(uniqueIDHex, 0L);
  }

  /**
   * Initializes unique ID using result set from SQL database.
   * 
   * @param rs result set from SQL database query.
   * @param fldName name of field in database.
   * @throws SQLException if there is an error while accessing the database.
   */
  public DTUniqueID(ResultSet rs, String fldName) throws SQLException {
    super(rs, fldName);
    // set to default value if 'rs' is null
    this.uniqueID = (rs != null) ? rs.getLong(fldName) : 0L;
  }

  /**
   * Provides textual representation of field.
   * 
   * @return String containing textual representation.
   */
  public String toString() {
    return String.valueOf(this.uniqueID);
  }

  // ------------------------------------------------------------------------

}
