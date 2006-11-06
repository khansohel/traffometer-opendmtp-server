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
//  2006/04/02  Martin D. Flynn
//      Added 'format' attribute
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.dbtools;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.opendmtp.util.Print;
import org.opendmtp.util.RTProperties;
import org.opendmtp.util.StringTools;

/**
 * Holds many different properties for DBFields, allowing for getters, setters, multiple
 * constructors with different arguments and other establish properties.
 * 
 * @author Martin D. Flynn
 * @author Joshua Stupplebeen
 * @author Brandon Lee
 */
public class DBField {

  // ------------------------------------------------------------------------

  /**
   * final variable set to 1.
   */
  public static final int KEY_PRIMARY = 1;
  /**
   * final variable set to 2.
   */
  public static final int KEY_UNIQUE = 2;
  /**
   * final variable set to 3.
   */
  public static final int KEY_INDEX = 3;

  // ------------------------------------------------------------------------
  // EDIT_NEVER : Never editable (maintained by system)
  // EDIT_NEW : Only editable when new records are created
  // EDIT_ADMIN : Editable by admin only
  // EDIT_PUBLIC : Editable by anyone having access to the data

  /**
   * final variable set to -1.
   */
  public static final int EDIT_NEVER = -1;
  /**
   * final variable set to 0.
   */
  public static final int EDIT_NEW = 0;
  /**
   * final variable set to 1.
   */
  public static final int EDIT_ADMIN = 1;
  /**
   * final variable set to 2.
   */
  public static final int EDIT_PUBLIC = 2;
  /**
   * final variable set to 9999.
   */
  public static final int EDIT_RDONLY = 9999;

  // ------------------------------------------------------------------------
  // field attributes

  /**
   * final variable string "key".
   */
  public static final String ATTR_KEY = "key"; // [true/false]
  /**
   * final variable set to "altkey".
   */
  public static final String ATTR_ALTKEY = "altkey"; // [true/false]
  /**
   * final variable set to "edit".
   */
  public static final String ATTR_EDIT = "edit"; // [0/1/2] editable mode
  /**
   * final variable set to "title".
   */
  public static final String ATTR_TITLE = "title"; // title
  /**
   * final variable set to "format".
   */
  public static final String ATTR_FORMAT = "format"; // format

  // ------------------------------------------------------------------------

  private String name = null;
  private Class javaClass = null;
  private Constructor javaClassConst = null;
  private String dataType = null;
  private boolean isPriKey = false;
  private boolean isAltKey = false;
  private RTProperties attr = null;
  private DBFactory factory = null;

  // ------------------------------------------------------------------------

  /**
   * DBField constructor with a single parameter, an instance of the DBField. 
   * The properties of the passed DBField are then copied to the private data 
   * values of the constructed DBField.
   * 
   * @param other Another instance of the DBField class.
   */
  public DBField(DBField other) {
    this.name = other.name;
    this.javaClass = other.javaClass;
    this.dataType = other.dataType;
    this.attr = other.attr;
    this.isPriKey = other.isPriKey;
    this.isAltKey = other.isAltKey;
  }

  /**
   * DBField constructor with 3 parameters.
   * 
   * @param name Name of the field.
   * @param dataType Datatype of the field.
   * @param isPriKey True if value is a primary key, false otherwise.
   */
  public DBField(String name, String dataType, boolean isPriKey) {
    // used by DBFactory.getTableColumns
    this.name = name;
    this.javaClass = null;
    this.dataType = dataType;
    this.attr = new RTProperties("");
    this.isPriKey = isPriKey;
    this.isAltKey = false;
  }

  /**
   * DBField constructor with 4 parameters.
   * 
   * @param name Name of the field.
   * @param javaClass Class type of the field.
   * @param dataType Datatype of the field.
   * @param attr Attributes of the field.
   */
  public DBField(String name, Class javaClass, String dataType, String attr) {
    this.name = name;
    this.javaClass = javaClass;
    this.dataType = dataType;
    this.attr = new RTProperties((attr != null) ? attr : "");
    this.isPriKey = this.getBooleanAttribute(ATTR_KEY);
    this.isAltKey = this.getBooleanAttribute(ATTR_ALTKEY);
  }

  // ------------------------------------------------------------------------

  /**
   * Setter method setting the private factory variable to and instance of the 
   * DBFactory class.
   * 
   * @param factory instance of the DBFactory class.
   */
  public void setFactory(DBFactory factory) {
    this.factory = factory;
  }

  /**
   * Getter method returning an instance of the DBFactory class.
   * 
   * @return returns the private factory class.
   */
  public DBFactory getFactory() {
    return this.factory;
  }

  // ------------------------------------------------------------------------

  /**
   * Getter method for field name.
   * 
   * @return Returns the private field name value.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Getter method for class type.
   * 
   * @return Returns the private class type.
   */
  public Class getTypeClass() {
    return this.javaClass;
  }

  /**
   * Returns a boolean true if the field is flagged as being an primary key 
   * and false otherwise.
   * 
   * @return boolean True if a primary key, false otherwise.
   */
  public boolean isPriKey() {
    return this.isPriKey;
  }

  /**
   * Returns a boolean true if the field is flagged as being an alternate key 
   * and false otherwise.
   * 
   * @return boolean True is an alternate key, false otherwise.
   */
  public boolean isAltKey() {
    return this.isAltKey;
  }

  // ------------------------------------------------------------------------

  /**
   * final variable set to "BOOLEAN".
   */
  public static final String TYPE_BOOLEAN = "BOOLEAN";
  /**
   * final variable set to "INTS".
   */
  public static final String TYPE_INT8 = "INT8";
  /**
   * final variable set to "UINTS".
   */
  public static final String TYPE_UINT8 = "UINT8";
  /**
   * final variable set to "INT16".
   */
  public static final String TYPE_INT16 = "INT16";
  /**
   * final variable set to "UINT16".
   */
  public static final String TYPE_UINT16 = "UINT16";
  /**
   * final variable set to INT32.
   */
  public static final String TYPE_INT32 = "INT32";
  /**
   * final variable set to "UNITS32".
   */
  public static final String TYPE_UINT32 = "UINT32";
  /**
   * final variable set to "INT64".
   */
  public static final String TYPE_INT64 = "INT64";
  /**
   * final variable set to "UINT64".
   */
  public static final String TYPE_UINT64 = "UINT64";
  /**
   * final variable set to "FLOAT".
   */
  public static final String TYPE_FLOAT = "FLOAT";
  /**
   * final variable set to "DOUBLE".
   */
  public static final String TYPE_DOUBLE = "DOUBLE";
  /**
   * final variable set to "BINARY".
   */
  public static final String TYPE_BINARY = "BINARY";
  /**
   * final variable set to "TEXT".
   */
  public static final String TYPE_TEXT = "TEXT";
  /**
   * final variable set to "STRING".
   */
  public static final String TYPE_STRING = "STRING";

  /**
   * Sets the TYPE_STRING size.
   * 
   * @param size new size.
   * @return the string containing TYPE_STRING and new size.
   */
  public static String TYPE_STRING(int size) {
    return TYPE_STRING + "[" + size + "]";
  }

  /**
   * Getter method returning the private dataType value.
   * 
   * @return String dataType.
   */
  public String getDataType() {
    return this.dataType;
  }

  // ------------------------------------------------------------------------

  /**
   * final variable 8bit Java 'boolean'.
   */
  public static final String HIB_BOOLEAN = "boolean"; 
  /**
   * final variable 8bit (signed) Java 'byte'.
   */
  public static final String HIB_INT8 = "byte";
  /**
   * final variable 8bit Java 'byte'.
   */
  public static final String HIB_UINT8 = "unsigned byte"; 
  /**
   * final variable 16bit (signed).
   */
  public static final String HIB_INT16 = "short"; 
  /**
   * final variable 16bit (signed).
   */
  public static final String HIB_UINT16 = "unsigned short"; 
  /**
   * final variable 32bit (signed) Java 'int'.
   */
  public static final String HIB_INT32 = "integer"; 
  /**
   * final variable 32bit Java 'int'.
   */
  public static final String HIB_UINT32 = "unsigned integer";
  /**
   * final variable 64bit (signed) Java 'long'.
   */
  public static final String HIB_INT64 = "long";
  /**
   * final variable 64bit Java 'long'.
   */
  public static final String HIB_UINT64 = "unsigned long";
  /**
   * final variable 8bit Java 'float'.
   */
  public static final String HIB_FLOAT = "float";
  /**
   * final variable 8bit Java 'double'.
   */
  public static final String HIB_DOUBLE = "double";
  /**
   * final variable  max (2^16 - 1) bytes 'binary'.
   */
  public static final String HIB_BINARY = "binary";
  /**
   * final variable  max (2^16 - 1) bytes  'text'.
   */
  public static final String HIB_TEXT = "text"; //
  /**
   * final variable 8bit Java 'string'.
   */
  public static final String HIB_STRING = "string";

  /**
   * Includes size into HIB_STRING.
   * 
   * @param size the size of the HIB_STRING.
   * @return a string with HIB plus size.
   */
  public static String HIB_STRING(int size) {
    return HIB_STRING + "(" + size + ")";
  }

  /**
   * Returns a string of the Hibernate type'.
   * 
   * @return string of the instance's data type.
   */
  private String _getHibernateType() {
    String dt = this.dataType.toUpperCase();
    if (dt.equals(TYPE_BOOLEAN)) {
      return HIB_BOOLEAN;
    }
    else if (dt.equals(TYPE_INT8)) {
      return HIB_INT8;
    }
    else if (dt.equals(TYPE_UINT8)) {
      return HIB_UINT8;
    }
    else if (dt.equals(TYPE_INT16)) {
      return HIB_INT16;
    }
    else if (dt.equals(TYPE_UINT16)) {
      return HIB_UINT16;
    }
    else if (dt.equals(TYPE_INT32)) {
      return HIB_INT32;
    }
    else if (dt.equals(TYPE_UINT32)) {
      return HIB_UINT32;
    }
    else if (dt.equals(TYPE_INT64)) {
      return HIB_INT64;
    }
    else if (dt.equals(TYPE_UINT64)) {
      return HIB_UINT64;
    }
    else if (dt.equals(TYPE_FLOAT)) {
      return HIB_FLOAT;
    }
    else if (dt.equals(TYPE_DOUBLE)) {
      return HIB_DOUBLE;
    }
    else if (dt.equals(TYPE_BINARY)) {
      return HIB_BINARY;
    }
    else if (dt.equals(TYPE_TEXT)) {
      return HIB_TEXT;
    }
    else if (dt.startsWith(TYPE_STRING + "[")) {
      // String x = dt.substring(TYPE_STRING.length() + 1);
      // int len = StringTools.parseInt(x, 32);
      // return HIB_STRING(len);
      return HIB_STRING;
    }
    else {
      Print.logError("Unrecognized type: " + dt);
      return HIB_STRING(32);
    }
  }

  /**
   * Calls _getHibernateType and returns string given from that function.
   * 
   * @return hibType the type of instance's type.
   */
  public String getHibernateType() {
    String hibType = _getHibernateType();
    return hibType;
  }

  // ------------------------------------------------------------------------

  /**
   * final SQL variable 'NOT NULL'.
   */
  public static final String SQL_NOT_NULL = "NOT NULL";
  
  /**
   * final SQL variable set to "TINYINT".
   */
  public static final String SQL_BOOLEAN = "TINYINT";
  /**
   * final SQL variable set to 'TINYINT'.
   */
  public static final String SQL_INT8 = "TINYINT"; 
  /**
   * final SQL variable set to 'TINYINT UNSIGNED'.
   */
  public static final String SQL_UINT8 = "TINYINT UNSIGNED"; 
  /**
   * final SQL variable set to 'SMALLINT'.
   */
  public static final String SQL_INT16 = "SMALLINT"; 
  /**
   * final SQL variable set to 'SMALLINT UNSIGNED'.
   */
  public static final String SQL_UINT16 = "SMALLINT UNSIGNED";
  /**
   * final SQL variable 32bit (signed) set to 'INT'.
   */
  public static final String SQL_INT32 = "INT";
  /**
   * final SQL variable 32bit Java 'int' set to 'INT UNSIGNED'.
   */
  public static final String SQL_UINT32 = "INT UNSIGNED";
  /**
   * final SQL variable 64bit (signed) Java 'long' set to 'BIGINT'.
   */
  public static final String SQL_INT64 = "BIGINT";
  /**
   * final SQL variable 64bit Java 'long' set to 'BIGINT UNSIGNED'.
   */
  public static final String SQL_UINT64 = "BIGINT UNSIGNED";
  /**
   * final SQL variable set to 'FLOAT'.
   */
  public static final String SQL_FLOAT = "FLOAT";
  /**
   * final SQL variable set to 'DOUBLE'.
   */
  public static final String SQL_DOUBLE = "DOUBLE";
  /**
   * final SQL variable set to 'BLOB', max (2^16 - 1) bytes.
   */
  public static final String SQL_BINARY = "BLOB";
  /**
   * final SQL variable set to 'TEXT'.
   */
  public static final String SQL_TEXT = "TEXT";
  /**
   * final SQL variable set to 'VARCHAR'.
   */
  public static final String SQL_VARCHAR = "VARCHAR";

  /**
   * Returns a SQL_VARCHAR string with specified size.
   * 
   * @param size to be placed in string.
   * @return string of SQL_VARCHAR and size.
   */
  public static String SQL_VARCHAR(int size) {
    return SQL_VARCHAR + "(" + size + ")";
  }

  /**
   * Returns a string with SQL type.
   * 
   * @return string with appropriate contents.
   */
  private String _getSqlType() {
    String dt = this.dataType.toUpperCase();
    if (dt.equals(TYPE_BOOLEAN)) {
      return SQL_BOOLEAN;
    }
    else if (dt.equals(TYPE_INT8)) {
      return SQL_INT8;
    }
    else if (dt.equals(TYPE_UINT8)) {
      return SQL_UINT8;
    }
    else if (dt.equals(TYPE_INT16)) {
      return SQL_INT16;
    }
    else if (dt.equals(TYPE_UINT16)) {
      return SQL_UINT16;
    }
    else if (dt.equals(TYPE_INT32)) {
      return SQL_INT32;
    }
    else if (dt.equals(TYPE_UINT32)) {
      return SQL_UINT32;
    }
    else if (dt.equals(TYPE_INT64)) {
      return SQL_INT64;
    }
    else if (dt.equals(TYPE_UINT64)) {
      return SQL_UINT64;
    }
    else if (dt.equals(TYPE_FLOAT)) {
      return SQL_FLOAT;
    }
    else if (dt.equals(TYPE_DOUBLE)) {
      return SQL_DOUBLE;
    }
    else if (dt.equals(TYPE_BINARY)) {
      return SQL_BINARY; // BLOB
    }
    else if (dt.equals(TYPE_TEXT)) {
      return SQL_TEXT; // CLOB
    }
    else if (dt.startsWith(TYPE_STRING + "[")) {
      String x = dt.substring(TYPE_STRING.length() + 1);
      int len = StringTools.parseInt(x, 32);
      return SQL_VARCHAR(len);
    }
    else {
      Print.logError("Unrecognized type: " + dt);
      return SQL_VARCHAR(32);
    }
  }

  /**
   * Calls _getSQLType and based on if its the primary key attaches SQL_NOT_NULL var to string.
   * 
   * @return sqlType string containing type.
   */
  public String getSqlType() {
    
    String sqlType = _getSqlType();
    
    if (this.isPriKey()) {
      if (sqlType.toUpperCase().endsWith(SQL_NOT_NULL.toUpperCase())) {
        return sqlType;
      }
      else {
        return sqlType + " " + SQL_NOT_NULL;
      }
    }
    else {
      return sqlType;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Returns length of dataType string.
   * 
   * @return an integer representing the length.
   */
  public int getLength() {
    String dt = this.dataType.toUpperCase();
    if (dt.startsWith(TYPE_STRING + "[")) {
      String x = dt.substring(TYPE_STRING.length() + 1);
      int len = StringTools.parseInt(x, 32);
      return len;
    }
    else {
      return 0;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Seems to check where in the string dataType TYPE_TEXT sub-string is.
   * 
   * @return boolean if true/false.
   */
  public boolean isCLOB() {
    return (this.dataType.toUpperCase().indexOf(TYPE_TEXT) >= 0);
  }

  /**
   * Seems to check where in the string dataType TYPE_BINARY sub-string is.
   * 
   * @return boolean if true/false.
   */
  public boolean isBLOB() {
    return (this.dataType.toUpperCase().indexOf(TYPE_BINARY) >= 0);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns if has the attribute of string key given.
   * 
   * @param key the attribute to check.
   * @return boolean if true/false.
   */
  public boolean hasAttribute(String key) {
    return this.attr.hasProperty(key);
  }

  /**
   * Returns if has the boolean attribute of string key given.
   * 
   * @param key the attribute to check.
   * @return boolean if true/false.
   */
  public boolean getBooleanAttribute(String key) {
    return this.attr.hasProperty(key) ? this.attr.getBoolean(key, true) : false;
  }

  /**
   * Returns attribute of string key given.
   * 
   * @param key attribute to get.
   * @return string attribute.
   */
  public String getStringAttribute(String key) {
    return this.attr.getString(key, null);
  }

  /**
   * Returns title.
   * 
   * @return string title.
   */
  public String getTitle() {
    return this.attr.getString(ATTR_TITLE, this.getName()).replace('_', ' ');
  }

  /**
   * Returns format.
   * 
   * @return string with the format.
   */
  public String getFormat() {
    return this.attr.getString(ATTR_FORMAT, null);
  }

  /**
   * Returns if it is editable in mode. No idea what its comparing.
   * 
   * @param mode the mode that it
   * @return boolean no idea what its comparing.
   */
  public boolean isEditable(int mode) {
    int edit = this.attr.getInt(ATTR_EDIT, EDIT_NEVER);
    return (mode <= edit);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns an object that contains result set value.
   * 
   * @param rs the result set.
   * @return an Object containing the set value or null.
   * @throws SQLException is thrown if problem with new instance.
   */
  public Object getResultSetValue(ResultSet rs) throws SQLException {
    String n = this.getName();
    Class jvc = this.getTypeClass();
    if (jvc == String.class) {
      return (rs != null) ? rs.getString(n) : "";
    }
    else if ((jvc == Integer.class) || (jvc == Integer.TYPE)) {
      return new Integer((rs != null) ? rs.getInt(n) : 0);
    }
    else if ((jvc == Long.class) || (jvc == Long.TYPE)) {
      return new Long((rs != null) ? rs.getLong(n) : 0L);
    }
    else if ((jvc == Float.class) || (jvc == Float.TYPE)) {
      return new Float((rs != null) ? rs.getFloat(n) : 0.0F);
    }
    else if ((jvc == Double.class) || (jvc == Double.TYPE)) {
      return new Double((rs != null) ? rs.getDouble(n) : 0.0);
    }
    else if ((jvc == Boolean.class) || (jvc == Boolean.TYPE)) {
      return new Boolean((rs != null) ? (rs.getInt(n) != 0) : false);
    }
    else if (DBFieldType.class.isAssignableFrom(jvc)) {
      if (this.javaClassConst == null) {
        try {
          this.javaClassConst = jvc.getConstructor(new Class[] { ResultSet.class, String.class });
        }
        catch (Throwable t) { // NoSuchMethodException
          Print.logError("Unable to obtain proper constructor: " + t);
          return null;
        }
      }
      try {
        return this.javaClassConst.newInstance(new Object[] { rs, n });
      }
      catch (Throwable t) { // InstantiationException, etc.
        if (t instanceof SQLException) {
          throw (SQLException) t; // re-throw SQLExceptions
        }
        Print.logError("Unable to instantiate: " + t);
        return null;
      }
    }
    else {
      Print.logError("Unsupported Java class: " + StringTools.className(jvc));
      return null;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Returns Object with default result set value or null.
   * 
   * 
   * @return the object with default values.
   */
  public Object getDefaultValue() {
    try {
      return this.getResultSetValue(null);
    }
    catch (SQLException sqe) {
      // this will(should) never occur
      return null;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Checks if quoteValue is ok.
   * 
   * @return boolean if true or not.
   */
  public boolean quoteValue() {
    if (this.isCLOB()) {
      return true;
    }
    else if (this.isBLOB()) {
      // This assumes that the value is presented in raw hex form.
      // If the value is presented in hex, quoting will procude invalid results.
      return true;
    }
    else {
      String t = this.getDataType().toUpperCase();
      return t.startsWith(TYPE_STRING);
    }
  }

  /**
   * Return the value of the Object.
   * 
   * @param v the object to get the value from.
   * @return string with object contents.
   */
  public String getQValue(Object v) {
    
    if (this.isBLOB()) {
      byte data[] = null;
      if (v == null) {
        data = new byte[0];
      }
      else if (v instanceof byte[]) {
        // this is the preferred Object type
        data = (byte[]) v;
      }
      else if (v instanceof String) {
        String vs = (String) v;
        if (vs.equals("")) {
          data = new byte[0];
        }
        else if (vs.startsWith("0x")) {
          data = StringTools.parseHex(vs, null);
          if (data == null) {
            data = StringTools.getBytes(vs.toCharArray());
          }
        }
        else {
          data = StringTools.getBytes(vs.toCharArray());
        }
      }
      else {
        Print.logError("Unsupported BLOB object type: " + StringTools.className(v));
        String vs = v.toString(); // no trimming
        // Notes: Because of the current character encoding, using 'vs.getBytes()'
        // may create a byte array with more elements that the original character
        // array. Instead, we need to convert to a byte array char by char.
        // data = StringTools.getBytes(vs);
        data = StringTools.getBytes(vs.toCharArray());
      }
      return (data.length > 0) ? ("0x" + StringTools.toHexString(data)) : "\"\"";
    }
    else {
      String vs = (v != null) ? v.toString().trim() : "";
      return this.quoteValue() || vs.equals("") ? StringTools.quoteString(vs) : vs;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Overiding equals method. Returns true if equal to this instance false otherwise.
   * 
   * @param other the object to compare to.
   * @return the results of comparison.
   */
  public boolean equals(Object other) {
    if (!(other instanceof DBField)) {
      return false;
    }
    DBField fld = (DBField) other;
    if (!this.getName().equals(fld.getName())) {
      return false;
    }
    else if (!this.getSqlType().equals(fld.getSqlType())) {
      return false;
    }
    else {
      return true;
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Returns a string with field definition.
   * 
   * @return string with the name and sql type.
   */
  public String getFieldDefinition() {
    return this.name + " " + this.getSqlType();
  }

  /**
   * Returns a string of this instance.
   * 
   * @return string of field definitions.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(this.getFieldDefinition());
    if (this.isPriKey) {
      sb.append(" key");
    }
    if (this.isAltKey) {
      sb.append(" altkey");
    }
    return sb.toString();
  }

  // ------------------------------------------------------------------------

}
