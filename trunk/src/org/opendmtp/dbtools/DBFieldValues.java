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
//  2006/04/11  Martin D. Flynn
//      'toString(<FieldName>)' now returns a default value consistent with the
//      field type if the field has not yet been assigned an actual value.
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.dbtools;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.opendmtp.util.OrderedMap;
import org.opendmtp.util.Print;
import org.opendmtp.util.StringTools;

/**
 * Holds Specifies DB fields values of a valueMap and a field ma.  Methods clear, set, get values, 
 * or checking for specific field values.
 * 
 * @author Martin D. Flynn
 * @author Brandon Lee
 */
public class DBFieldValues {

  private DBRecordKey recordKey = null;
  private OrderedMap valueMap = null;
  private OrderedMap fieldMap = null;


  /**
   * Sets the valueMap and the fieldMap with new instances.
   */
  private DBFieldValues() {
    this.valueMap = new OrderedMap();
    this.fieldMap = new OrderedMap();
  }

  /**
   * Calls constructor and sets DBRecordKey and fills fieldMap.
   * 
   * @param rcdKey new record key object.
   */
  public DBFieldValues(DBRecordKey rcdKey) {
    this();
    this.recordKey = rcdKey;
    DBField fld[] = rcdKey.getFields();
    for (int i = 0; i < fld.length; i++) {
      String fldName = fld[i].getName();
      this.fieldMap.put(fldName, fld[i]);
    }
  }

  /**
   * Clears all Fields values.
   */
  public void clearFieldValues() {
    if (this.recordKey != null) {
      DBField fld[] = this.recordKey.getFields();
      for (int i = 0; i < fld.length; i++) {
        if (!fld[i].isPriKey()) {
          this.setFieldValue(fld[i].getName(), null);
        }
      }
    }
    else {
      Print.logStackTrace("DBRecordKey has not been set!");
    }
  }

  /**
   * Returns the feild specified.
   * 
   * @param fldName the name of the field to return.
   * @return the DBField to specified or null.
   */
  public DBField getField(String fldName) {
    if (this.recordKey != null) {
      return this.recordKey.getField(fldName);
    }
    else {
      Print.logStackTrace("DBRecordKey has not been set!");
      return null;
    }
  }

  /**
   * Check for a field.
   * 
   * @param fldName the field name.
   * @return boolean if field is present.
   */
  public boolean hasField(String fldName) {
    return this.fieldMap.containsKey(fldName);
  }

  /**
   * Sets specified field.
   * 
   * @param fldName name of field.
   * @param newVal object containing the new value.
   * @return boolean if it was successful.
   */
  public boolean setFieldValue(String fldName, Object newVal) {
    if (this.hasField(fldName)) {
      // field type validation?
      this.valueMap.put(fldName, newVal);
      DBRecord rcd = this.recordKey._getDBRecord();
      if (rcd != null) {
        Object oldVal = this.getFieldValue(fldName);
        rcd.setChanged(fldName, oldVal, newVal);
      }
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * sets field value with an integer Object.
   * 
   * @param fldName name of field.
   * @param val the new integer object value.
   * @return boolean result of operation success.
   */
  public boolean setFieldValue(String fldName, int val) {
    return this.setFieldValue(fldName, new Integer(val));
  }

  /**
   * sets field value with an long Object.
   * 
   * @param fldName name of field.
   * @param val the new long object value.
   * @return boolean result of operation success.
   */
  public boolean setFieldValue(String fldName, long val) {
    return this.setFieldValue(fldName, new Long(val));
  }

  /**
   * sets field value with an double Object.
   * 
   * @param fldName name of field.
   * @param val the new double object value.
   * @return boolean result of operation success.
   */
  public boolean setFieldValue(String fldName, double val) {
    return this.setFieldValue(fldName, new Double(val));
  }

  /**
   * sets field value with an boolan Object.
   * 
   * @param fldName name of field.
   * @param val the new boolean object value.
   * @return boolean result of operation success.
   */
  public boolean setFieldValue(String fldName, boolean val) {
    return this.setFieldValue(fldName, new Boolean(val));
  }

  /**
   * sets field value with an byte array or empty byte arry if val is null.
   * 
   * @param fldName name of field.
   * @param val the new double object value.
   * @return boolean result of operation success.
   */
  public boolean setFieldValue(String fldName, byte val[]) {
    return this.setFieldValue(fldName, (Object) ((val != null) ? val : new byte[0]));
  }

  /**
   * sets all field values with values in ResultSet object.
   * 
   * @param rs result set with to be field values.
   * @throws SQLException
   */
  public void setFieldValues(ResultSet rs) throws SQLException {
    
    if (this.recordKey != null) {
      
      DBField fld[] = this.recordKey.getFields();
      
      for (int i = 0; i < fld.length; i++) {
        
        Object val = fld[i].getResultSetValue(rs);
        this.setFieldValue(fld[i].getName(), val);
      }
    }
    else {
      Print.logStackTrace("DBRecordKey has not been set!");
    }
  }

  /**
   * Returns an Object containing the field value.
   * 
   * @param fldName the name of the field to retrieve.
   * @return Object the value of that field.
   */
  public Object getFieldValue(String fldName) {
    
    Object val = (fldName != null) ? this.valueMap.get(fldName) : null;
    if (val != null) {
      // field value found
      return val;
    }
    else if (this.hasField(fldName)) {
      // field name found, but value is null (which may be the case if the value was undefined)
      // Print.logError("Field value is null: " + fldName);
      return null;
    }
    else {
      Print.logStackTrace("Field not found: " + fldName);
      return null;
    }
  }

  /**
   * Overwrites the toString method.
   * 
   * @param fldName the name of the specified field.
   * @return string a string containing the field value.
   */
  public String toString(String fldName) {
    
    Object obj = this.getFieldValue(fldName);
    
    if (obj == null) {
      
      // return a default value consistent with the field type
      DBField fld = this.getField(fldName);
      
      if (fld != null) {
        
        obj = fld.getDefaultValue();
        
        if (obj != null) {
          return obj.toString();
        }
        else {
          // Implementation error, this should never occur
          Print.logStackTrace("Field doesn't support a default value: " + fldName);
          return "";
        }
      }
      else {
        // Implementation error, this should never occur
        // If we're here, the field doesn't exist.
        return "";
      }
    }
    else if (obj instanceof Boolean) {
      return ((Boolean) obj).booleanValue() ? "1" : "0";
    }
    else if (obj instanceof byte[]) {
      return "0x" + StringTools.toHexString((byte[]) obj);
    }
    else {
      return obj.toString();
    }
  }

}