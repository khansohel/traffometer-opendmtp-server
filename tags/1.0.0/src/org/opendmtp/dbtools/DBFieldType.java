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
package org.opendmtp.dbtools;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Abstract class for a DBFieldType.
 * 
 * @author Martin D. Flynn
 * @author Brandon Lee
 */
public abstract class DBFieldType {

  // ------------------------------------------------------------------------

  /**
   * Constructor, calls super.
   */
  public DBFieldType() {
    super();
  }

  /**
   * Constructor single param, calls the constructor after.
   * 
   * @param val contains the field value.
   */
  public DBFieldType(String val) {
    this();
  }

  /**
   * Constructor with three parameters and throws SQLException.
   * 
   * @param rs the result set, may be null.
   * @param fldName the field name.
   * @throws SQLException if error in sql occurs.
   */
  public DBFieldType(ResultSet rs, String fldName) throws SQLException {
    this();
    // override (NOTE: 'rs' may be null!)
  }

  // ------------------------------------------------------------------------

  /**
   * Needed overidden toString class for each subclass.
   * 
   * @return string of DBField object.
   */
  public abstract String toString();

}
