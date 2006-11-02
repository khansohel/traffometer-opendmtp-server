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

import org.opendmtp.util.StringTools;

/**
 * Class defines Unique ID object to uniquely identify device.
 * @author Martin D. Flynn
 * @author Brandon Horiuchi
 */
public class UniqueID {

  // ------------------------------------------------------------------------

  private long id = 0L;

  /**
   * Constructor for unique ID.
   * @param id Long integer id.
   */
  public UniqueID(long id) {
    this.id = id;
  }

  /**
   * Constructor for unique ID.
   * @param id Byte array id.
   */
  public UniqueID(byte id[]) {
    this.id = 0L;
    for (int i = 0; (i < id.length) && (i < 8); i++) {
      this.id = (this.id << 8) | ((int) id[i] & 0xFF);
    }
  }

  /**
   * Constructor for unique ID.
   * @param id String id.
   */
  public UniqueID(String id) {
    this(StringTools.parseHex(id, null));
  }

  // ------------------------------------------------------------------------
  /**
   * Checks if ID is valid.
   * @return true if ID is valid
   */
  public boolean isValid() {

    /* check proper size */
    if ((id & 0xFFFF000000000000L) != 0) {
      return false;
    }

    /* check checksum */
    int c = 0;
    //for (int i = 0; i < 6; i++) {
    //    c ^= (int)id[i] & 0xFF;
    //}
    return (c == 0) ? true : false;

  }

  /**
   * Gets ID.
   * @return Long integer ID.
   */
  public long getId() {
    return this.id;
  }

  // ------------------------------------------------------------------------
  /**
   * Compares two IDs.
   * @param other Object to compare.
   * @return Boolean true if same.
   */
  public boolean equals(Object other) {
    if (other instanceof UniqueID) {
      long id1 = ((UniqueID) other).getId();
      long id2 = this.getId();
      return (id1 == id2);
    }
    return false;
  }

  // ------------------------------------------------------------------------
  /**
   * Returns string representation of unique ID.
   * @return String representation of unique ID.
   */
  public String toString() {
    if (this.isValid()) {
      return "0x" + StringTools.toHexString(this.getId());
    }
    else {
      return "";
    }
  }

  // ------------------------------------------------------------------------

}
