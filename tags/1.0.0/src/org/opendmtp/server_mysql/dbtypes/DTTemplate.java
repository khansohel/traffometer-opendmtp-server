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
import org.opendmtp.util.RTProperties;
import org.opendmtp.util.StringTools;

/**
 * Some sort of wrapper for property templates.
 * 
 * @author Martin D. Flynn
 * @author Robert S. Brewer
 */
public class DTTemplate extends DBFieldType {

  // ------------------------------------------------------------------------
  // boolean hiRes = false; // 0..1
  // int fldType = -1; // 0..128
  // int fldNdx = 0; // 0..255
  // int fldLen = 0; // 0..255
  // "0=H|23|0|3

  // ------------------------------------------------------------------------

  private RTProperties templateProps = null;

  /**
   * Initializes template properties field.
   */
  public DTTemplate() {
    super();
    this.templateProps = new RTProperties("");
  }

  /**
   * Initializes template properties field using provided template.
   * 
   * @param template string containing properties template. 
   */
  public DTTemplate(String template) {
    super(template);
    this.templateProps = new RTProperties((template != null) ? template : "");
  }

  /**
   * Initializes template properties field using result set from SQL database.
   * 
   * @param rs result set from SQL database query.
   * @param fldName name of field in database.
   * @throws SQLException if there is an error while accessing the database.
   */
  public DTTemplate(ResultSet rs, String fldName) throws SQLException {
    super(rs, fldName);
    // set to default value if 'rs' is null
    this.templateProps = new RTProperties((rs != null) ? rs.getString(fldName) : "");
  }

  /**
   * Provides textual representation of field.
   * 
   * @return String containing textual representation.
   */
  public String toString() {
    return this.templateProps.toString();
  }

  // ------------------------------------------------------------------------
  // "#=<type>|[H|L]|<index>|<length>"

  private static final char FIELD_VALUE_SEPARATOR = '|';

  /**
   * Clears the properties in the class field.
   */
  public void clearFields() {
    this.templateProps.clearProperties();
  }

  /**
   * Returns a field extracted from the template properties at the requested index.
   * 
   * @param ndx index of field to be extracted from properties.
   * @return extracted field.
   */
  public Field getField(int ndx) {
    String name = String.valueOf(ndx);
    String ftmp = this.templateProps.getString(name, null);
    return ((ftmp != null) && !ftmp.equals("")) ? new Field(ftmp) : null;
  }

  /**
   * Sets a field in the template properties at the requested index.
   * 
   * @param ndx index of field to be set in properties.
   * @param fld field value to be set in properties.
   */
  public void setField(int ndx, Field fld) {
    String name = String.valueOf(ndx);
    this.templateProps.setString(name, fld.toString());
  }

  /**
   * Wrapper for database fields of this type.
   * 
   * @author Martin D. Flynn
   * @author Robert S. Brewer
   */
  public static class Field {
    private int type = -1;
    private boolean isHiRes = false;
    private int index = 0;
    private int length = 0;

    /**
     * Initializes class fields values using given values.
     * 
     * @param type type of field.
     * @param hiRes true if high resolution, false otherwise.
     * @param ndx index in properties.
     * @param len length of field.
     */
    public Field(int type, boolean hiRes, int ndx, int len) {
      this.type = type;
      this.isHiRes = hiRes;
      this.index = ndx;
      this.length = len;
    }

    /**
     * Initializes class fields values using values parsed out of given String.
     * 
     * @param s String to be parsed for values.
     */
    public Field(String s) {
      String f[] = StringTools.parseString(s, FIELD_VALUE_SEPARATOR);
      this.type = (f.length > 0) ? StringTools.parseInt(f[1], -1) : -1;
      this.isHiRes = (f.length > 1) ? f[0].equalsIgnoreCase("H") : false;
      this.index = (f.length > 2) ? StringTools.parseInt(f[2], 0) : 0;
      this.length = (f.length > 3) ? StringTools.parseInt(f[3], 0) : 0;
    }

    /**
     * Accessor for hiRes field.
     * 
     * @return true if field is high resolution, false otherwise.
     */
    public boolean isHiRes() {
      return this.isHiRes;
    }

    /**
     * Accessor for type field.
     * 
     * @return type of this field.
     */
    public int getType() {
      return this.type;
    }

    /**
     * Accessor for index field.
     * 
     * @return index of this field.
     */
    public int getIndex() {
      return this.index;
    }

    /**
     * Accessor for length field.
     * 
     * @return length of this field.
     */
    public int getLength() {
      return this.length;
    }

    /**
     * Provides textual representation of field. It will look like:
     * "#=type|[H|L]|index|length"
     * 
     * @return String containing textual representation.
     * @see java.lang.Object#toString()
     */
    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(this.isHiRes() ? "H" : "L");
      sb.append(FIELD_VALUE_SEPARATOR);
      sb.append(this.getType());
      sb.append(FIELD_VALUE_SEPARATOR);
      sb.append(this.getIndex());
      sb.append(FIELD_VALUE_SEPARATOR);
      sb.append(this.getLength());
      return sb.toString();
    }

    /**
     * Compares two DTTemplates by converting to Strings and then comparing Strings.
     * 
     * @param other the other DTTemplate to be compared to.
     * @return true if the fields of both objects are the same.
     */
    public boolean equals(Object other) {
      if (other instanceof Field) {
        return this.toString().equals(other.toString());
      }
      else {
        return false;
      }
    }
  }

  // ------------------------------------------------------------------------

}
