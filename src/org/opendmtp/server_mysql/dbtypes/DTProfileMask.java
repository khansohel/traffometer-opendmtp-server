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
 * Wrapper class for "profile masks" (whatever those are). Appears to have something
 * to do with time limit intervals?
 * 
 * @author Martin D. Flynn
 * @author Robert S. Brewer
 */
public class DTProfileMask extends DBFieldType {

  // ------------------------------------------------------------------------

  private byte profileMask[] = null;

  /**
   * Sets profile mask field using provided profile mask byte array.
   *  
   * @param profileMask the new profile mask. If null, the field will be set to
   * a new empty byte array.
   */
  public DTProfileMask(byte profileMask[]) {
    this.profileMask = (profileMask != null) ? profileMask : new byte[0];
  }

  /**
   * Sets profile mask field using provided profile mask converted from String of
   * hex to an array of bytes.
   *  
   * @param val the new profile mask as a hex String. If invalid, the field
   * will be set to a new empty byte array.
   */
  public DTProfileMask(String val) {
    super(val);
    this.profileMask = StringTools.parseHex(val, new byte[0]);
  }

  /**
   * Sets profile mask field using provided database result set.
   *  
   * @param rs result set from SQL database.
   * @param fldName name of field containing profile mask.
   * @throws SQLException if there is an error while accessing the database.
   */
  public DTProfileMask(ResultSet rs, String fldName) throws SQLException {
    super(rs, fldName);
    // set to default value if 'rs' is null
    this.profileMask = (rs != null) ? rs.getBytes(fldName) : new byte[0];
  }

  /**
   * Converts profile mask byte array into ASCII hex representation.
   * 
   * @return string with representation of the bytes in hex.
   * @see org.opendmtp.dbtools.DBFieldType#toString()
   */
  public String toString() {
    return "0x" + StringTools.toHexString(this.profileMask);
  }

  // ------------------------------------------------------------------------

  /**
   * Some sort of expansion of the profile mask byte array based on a time
   * interval. Quite cryptic.
   * 
   * @param minutes number of minutes (but why?)
   */
  public void setLimitTimeInterval(int minutes) {
    int byteLen = (minutes + 7) / 8;
    if (this.profileMask.length != byteLen) {
      byte newMask[] = new byte[byteLen];
      int len = (this.profileMask.length < byteLen) ? this.profileMask.length : byteLen;
      System.arraycopy(this.profileMask, 0, newMask, 0, len);
      this.profileMask = newMask;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Getter for profile mask.
   * 
   * @return the profile mask.
   */
  public byte[] getByteMask() {
    return this.profileMask;
  }

}
