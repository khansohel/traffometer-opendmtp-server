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
//      Added "getField(String)"
//  2006/04/09  Martin D. Flynn
//      Integrate DBException
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.dbtools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.opendmtp.util.Print;

/**
 * Implements the DBRecordKey containing information to construct the DBRecord class. Methods
 * pertain to specific field operations to create/destroy/alter data within the database through 
 * the use of dynamically created SQL statements and data.
 * 
 * @author Martin D. Flynn, Joshua Stupplebeen
 * 
 */
public abstract class DBRecordKey {

  // ------------------------------------------------------------------------

  private DBFieldValues fieldValues = null;
  private DBRecord record = null;

  // ------------------------------------------------------------------------

  /**
   * Protected constructor returning an instance of the super class.
   */
  protected DBRecordKey() {
    super();
  }

  /**
   * Abstract method.
   * 
   * @return DBFactory intance.
   */
  public abstract DBFactory getFactory();

  // ------------------------------------------------------------------------
  // DBFactory convience methods

  /**
   * Retrieves the Table name.
   * 
   * @return String Table name.
   */
  public String getTableName() {
    return this.getFactory().getTableName();
  }

  /**
   * Retrieves the Key fields. This method only calls itself after calling the getFactory method.
   * It returns the database key fields in an array. Assuming it returns all the keys contained in
   * the database.
   * 
   * @return DBField[] Array of Keys.
   */
  public DBField[] getKeyFields() {
    return this.getFactory().getKeyFields();
  }

  /**
   * Retrieves the fields of the database. This method calls itself after calling the getFactory
   * method. It returns the database fields as an array, assuming it returns ALL the fields of the
   * database.
   * 
   * @return DBField[] array of Fields.
   */
  public DBField[] getFields() {
    return this.getFactory().getFields();
  }

  /**
   * Returns a specific database field denoted by the field name passed as a parameter.
   * 
   * @param fldName Field name.
   * @return DBField Field with name fldName.
   */
  public DBField getField(String fldName) {
    return this.getFactory().getField(fldName);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the current Record's field values if any exist.
   * 
   * @return DBFieldValues Current record's field values.
   */
  protected DBFieldValues getFieldValues() {
    if (this.fieldValues == null) {
      this.fieldValues = new DBFieldValues(this);
    }
    return this.fieldValues;
  }

  /**
   * Returns the object value from the field denoted by fldname.
   * 
   * @param fldName Field name.
   * @return Object Field value.
   */
  public Object getFieldValue(String fldName) {
    return this.getFieldValues().getFieldValue(fldName);
  }

  /**
   * Sets the field value to the Object val.
   * 
   * @param fldName Field name.
   * @param val Object value.
   * @return boolean Determines if the set method was successful.
   */
  public boolean setFieldValue(String fldName, Object val) {
    return this.getFieldValues().setFieldValue(fldName, val);
  }

  /**
   * Sets the field value to the boolean val.
   * 
   * @param fldName Field name.
   * @param val Boolean value.
   * @return boolean Determines if the set method was successful.
   */
  public boolean setFieldValue(String fldName, boolean val) {
    return this.getFieldValues().setFieldValue(fldName, val);
  }

  /**
   * Sets the field value to the int val.
   * 
   * @param fldName Field name.
   * @param val int value.
   * @return Determines if the set method was successful.
   */
  public boolean setFieldValue(String fldName, int val) {
    return this.getFieldValues().setFieldValue(fldName, val);
  }

  /**
   * Sets the Field value to the long val.
   * 
   * @param fldName Field name.
   * @param val Long val.
   * @return Determines if the set method was successful.
   */
  public boolean setFieldValue(String fldName, long val) {
    return this.getFieldValues().setFieldValue(fldName, val);
  }

  /**
   * Sets the Field value to the double val.
   * 
   * @param fldName Field name.
   * @param val Double value.
   * @return Determines if the set method was successful.
   */
  public boolean setFieldValue(String fldName, double val) {
    return this.getFieldValues().setFieldValue(fldName, val);
  }

  // ------------------------------------------------------------------------

  /**
   * Determins if the current recordKey exists by calling the _exists method.
   * 
   * @throws DBException Throws a DBException if a database error occurs.
   */
  public boolean exists() throws DBException {
    try {
      return this._exists();
    }
    catch (SQLException sqe) {
      throw new DBException("Record existance '" + this + "'", sqe);
    }
  }

  /**
   * Returns a boolean value indicating whether the record contains any Key values in the database
   * table. Returns true if it does and false otherwise. If an SQLExcetion occurs all tables are
   * locked.
   * 
   * @return boolean Does the record contain any key values.
   * @throws SQLException Throws an SQLException if an sql error occurs.
   * @throws DBException Throws a DBException if a database error occurs.
   */
  protected boolean _exists() throws SQLException, DBException {
    DBField kfld[] = this.getKeyFields();
    if (kfld.length == 0) {
      return false;
    }
    Statement stmt = null;
    ResultSet rs = null;
    boolean exists = false;
    StringBuffer sb = new StringBuffer();
    try {
      String firstKey = kfld[0].getName();
      // MySQL: SELECT <Keys> FROM <TableName> <KeyWhere>
      sb.append("SELECT ").append(firstKey).append(" FROM ").append(this.getTableName());
      sb.append(this.getWhereClause());
      stmt = DBConnection.getDefaultConnection().execute(sb.toString()); // may throw DBException
      rs = stmt.getResultSet();
      exists = rs.next();
    }
    catch (SQLException sqe) {
      if (sqe.getErrorCode() == DBFactory.SQLERR_TABLE_NOTLOCKED) {
        Print.logError("SQL Lock Error: " + sqe);
        Print.logError("Hackery! Forcing lock on table: " + this.getTableName());
        // MySQL: LOCK TABLES <TableName> READ
        DBConnection.getDefaultConnection().execute("LOCK TABLES " + this.getTableName() + " READ")
            .close();
        stmt = DBConnection.getDefaultConnection().execute(sb.toString());
        rs = stmt.getResultSet();
        exists = rs.next();
        // MySQL: UNLOCK TABLES
        DBConnection.getDefaultConnection().execute("UNLOCK TABLES").close();
      }
      else {
        throw sqe;
      }
    }
    finally {
      if (rs != null) {
        rs.close();
      }
      if (stmt != null) {
        stmt.close();
      }
    }
    return exists;
  }

  // ------------------------------------------------------------------------

  /**
   * Attempts to delete a record from the database. Calls the _delete method.
   * 
   * @throws DBException Throws a DBException if a database error occurs.
   */
  public void delete() throws DBException {
    try {
      this._delete();
    }
    catch (SQLException sqe) {
      throw new DBException("Record deletion", sqe);
    }
  }

  /**
   * Deletes the current record from the database table.
   * 
   * @throws SQLException Throws an SQLException if an SQL error occurs.
   * @throws DBException Throws a DBException if a database error occurs.
   */
  protected void _delete() throws SQLException, DBException {
    StringBuffer sb = new StringBuffer();
    sb.append("DELETE FROM ").append(this.getTableName());
    sb.append(this.getWhereClause());
    DBConnection.getDefaultConnection().executeUpdate(sb.toString());
  }

  // ------------------------------------------------------------------------

  /**
   * Generates the SQL code to select both key fields and values from the database table. 
   * Generates SQL code looks like "WHERE AND 'fldName' = 'keyFlds.getQValue' ".
   * 
   * @return String SQL Statement.
   */
  public String getWhereClause() {
    StringBuffer sb = new StringBuffer();
    sb.append(" WHERE (");
    DBField[] keyFlds = this.getKeyFields();
    DBFieldValues fldVals = this.getFieldValues();
    for (int i = 0; i < keyFlds.length; i++) {
      if (i > 0) {
        sb.append(" AND ");
      }
      String fldName = keyFlds[i].getName();
      sb.append(fldName);
      sb.append("=");
      sb.append(keyFlds[i].getQValue(fldVals.toString(fldName)));
    }
    sb.append(")");
    return sb.toString();
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the current DBRecord.
   * 
   * @return DBRecord the current record.
   */
  /* package */DBRecord _getDBRecord() {
    return this.record; // may be null
  }

  /**
   * Returns the current DBRecord passing false to the boolean constructor.
   * 
   * @return DBRecord the current record.
   */
  public DBRecord getDBRecord() {
    return this.getDBRecord(false);
  }

  /**
   * Reloads the current DBRecord with the boolean constructor.
   * 
   * @param reload boolean constructor value.
   * @return DBRecord The reloaded record data.
   */
  public DBRecord getDBRecord(boolean reload) {
    // returns null if there is an error

    /* create record */
    if (this.record == null) {
      try {
        this.record = DBRecord._createDBRecord(this);
      }
      catch (DBException dbe) {
        // Implementation error (this should never occur)
        // an NPE will likely follow
        Print.logStackTrace("Implementation error - cant' create DB record", dbe);
        return null;
      }
    }

    /* reload */
    if (reload) {
      // 'reload' is ignored if key does not exist
      this.record.reload();
    }

    /* return record (never null) */
    return this.record;

  }

  // ------------------------------------------------------------------------

  /**
   * Determines if the object passed as a parameter is 'equal' to the current record key by
   * comparing their key field values.
   * 
   * @param other Object comparing to the current RecordKey.
   * @return boolean Determines if the two objects are equal.
   */
  public boolean equals(Object other) {
    if (other == null) {

      return false;

    }
    else if (this.getClass().equals(other.getClass())) {

      /* get key fields */
      DBField thisKfld[] = this.getKeyFields();
      DBField othrKfld[] = ((DBRecordKey) other).getKeyFields();
      if (thisKfld.length != othrKfld.length) {
        return false;
      }

      /* compare field values */
      DBFieldValues thisFval = this.getFieldValues();
      DBFieldValues othrFval = ((DBRecordKey) other).getFieldValues();
      for (int i = 0; (i < thisKfld.length); i++) {
        if (!thisKfld[i].equals(othrKfld[i])) {
          return false;
        }
        Object thisKey = thisFval.getFieldValue(thisKfld[i].getName());
        Object othrKey = othrFval.getFieldValue(othrKfld[i].getName());
        if ((thisKey == null) || (othrKey == null)) {
          if (thisKey != othrKey) {
            return false;
          }
        }
        else if (!thisKey.equals(othrKey)) {
          return false;
        }
      }

      /* equals */
      return true;
    }

    return false;
  }

  /**
   * Converts the current RecordKey's key values to a string.
   * 
   * @return String Record Key values.
   */
  public String toString() {
    DBField kf[] = this.getKeyFields();
    if (kf.length == 0) {
      return "<null>";
    }
    else {
      DBFieldValues fv = this.getFieldValues();
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < kf.length; i++) {
        if (i > 0) {
          sb.append(",");
        }
        sb.append(fv.toString(kf[i].getName()));
      }
      return sb.toString();
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Attempts to evaluate if the character passed is a valid character for identification. 
   * Accepted characters in this implementation include: 
   * alphanumeric characters, '.', '_', '@', '&'.
   * 
   * @param ch character being evaluated.
   * @return boolean Determining if the character is a valid identifier.
   */
  public static boolean isValidIDChar(char ch) {
    // At a minimum, avoid the following special chars:
    // $ - substitution character
    // {} - have had problems using this character in MySQL
    // % - MySQL wildcard character
    // * - generic wildcard character
    // \ - escape character
    // ? - just don't use it
    // , - will get confused as a field separator
    // | - will get confused as a field separator
    // / - will get confused as a field separator
    // = - will get confused as a key=value separator
    // "'` - quotation characters
    // # - possible beginning of comment
    // ~ - just don't use it
    // ? - just don't use it
    // ^ - just don't use it
    // Pending possibles:
    // ! - Looks like '|'?
    // - - ?
    // + - ?
    // @abc,#abc,_abc,.abc,&abc
    if (Character.isLetterOrDigit(ch)) {
      return true;
    }
    else if ((ch == '.') || (ch == '_')) {
      // definately accept these
      return true;
    }
    else if ((ch == '@') || (ch == '&')) {
      // we'll consider these
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Filters the valid identifier characters from the text ID.
   * 
   * @param text String identifier.
   * @return String Valid identifier.
   */
  public static String FilterID(String text) {
    // ie. "sky.12", "acme@123"
    if ((text != null) && !text.equals("")) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < text.length(); i++) {
        char ch = Character.toLowerCase(text.charAt(i));
        if (DBRecordKey.isValidIDChar(ch)) {
          sb.append(ch);
        }
      }
      return sb.toString();
    }
    else {
      return text;
    }
  }

}
