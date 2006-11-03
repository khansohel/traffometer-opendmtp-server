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
//  2006/04/09  Martin D. Flynn
//      Integrate DBException
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.dbtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Vector;
import org.opendmtp.util.FileTools;
import org.opendmtp.util.MethodAction;
import org.opendmtp.util.Print;
import org.opendmtp.util.StringTools;

/**
 * Representation of a database table. It contains methods and subclasses for creating and manipulating the table's  
 * the table's components such as columns, rows, primary keys. 
 * @author Martin D. Flynn
 * @author Kiet Huynh
 *
 */
public class DBFactory {

  // ------------------------------------------------------------------------
  /** The type of the database table. */
  public static final String DBTABLE_TYPE = "MyISAM"; // "InnoDB"; // "MyISAM";

  /** The Primary key of the table. */
  public static final int KEY_PRIMARY = DBField.KEY_PRIMARY;
  /** The Unique key of the table. */
  public static final int KEY_UNIQUE = DBField.KEY_UNIQUE;
  /** The Index key of the table. */
  public static final int KEY_INDEX = DBField.KEY_INDEX;

  // ------------------------------------------------------------------------
  // MySQL error codes: [as returned by "sqe.getErrorCode()"]
  //   1007 - Database already exists
  //   1045 - Invalid authorization specification: Access denied for user
  //   1049 - Unknown database '??'
  //   1054 - Unknown column '??' in 'field list'.
  //   1062 - Duplicate entry
  //   1064 - Statement syntax error
  //   1100 - Didn't lock all tables
  //   1146 - Table doesn't exist
  //   

  /** The error code indicating that the database already exists. */
  public static final int SQLERR_DATABASE_EXISTS = 1007;

  /** The error code indicating that the authorization is invalid. */
  public static final int SQLERR_INVALID_AUTH = 1045;

  /** The error code indicating that the database is unknown. */
  public static final int SQLERR_UNKNOWN_DATABASE = 1049;

  /** The error code indicating that one of the columns in the table is unknown. */
  public static final int SQLERR_UNKNOWN_COLUMN = 1054;

  /** The error code indicating that the key is duplicate. */
  public static final int SQLERR_DUPLICATE_KEY = 1062;

  /** The error code indicating that there is a statement syntax error. */
  public static final int SQLERR_SYNTAX_ERROR = 1064;

  /** The error code indicating that the table is not locked. */
  public static final int SQLERR_TABLE_NOTLOCKED = 1100;

  /** The error code indicating that the table does not exist. */
  public static final int SQLERR_TABLE_NONEXIST = 1146;

  // ------------------------------------------------------------------------

  /** A Vector containing all the tables in the database. */
  protected static Vector factoryList = new Vector();

  /**
   * Gets a table in the database by name. 
   * @param tableName The name of the table.
   * @return The table if it exists. Otherwise, return null.
   */
  public static DBFactory getFactoryByName(String tableName) {
    Print.logDebug("Searching for Factory: " + tableName);
    for (Iterator i = factoryList.iterator(); i.hasNext();) {
      DBFactory fact = (DBFactory) i.next();
      //Print.logDebug("  => Checking " + fact.getTableName());
      if (fact.getTableName().equals(tableName)) {
        return fact;
      }
    }
    return null;
  }

  // ------------------------------------------------------------------------

  /** The name of the table. */
  private String tableName = null;

  /** 
   * The array containing all the defined columns. These columns 
   * does not necesserily exist in the table.
   * 
   */
  private DBField field[] = null;

  /** The array containing all the columns whose keys are primary keys of the table. */
  private DBField priKeys[] = null;

  /** The array containing all the columns whose keys are alternate keys of the table. */
  private DBField altKeys[] = null;

  /** The type of a key. Is is initialized to primary key which I think it shouldn't be.*/
  private int keyType = KEY_PRIMARY;

  /** The class of a key. */
  private Class keyClass = null;

  /** The class of a record. */
  private Class rcdClass = null;

  // ------------------------------------------------------------------------

  /**
   * Creates a table with the tableName, fields, the type of the keys as given
   * by parameters. The class of record (rcdClass) and the class of the key (keyClass)
   * are initialized to null.
   * @param tableName The name of the table.
   * @param field An array containing all the fields of the table.
   * @param keyType The type of the key.
   */
  public DBFactory(String tableName, DBField field[], int keyType) {
    this(tableName, field, keyType, null, null);
  }

  /**
   * Creates a table with the table name, fields, the type and the class of the keys, and 
   * the class of record as given by parameters.  This table is then added to the array of 
   * tables (factoryList) of the database.
   * @param tableName The name of the table.
   * @param field An array containing all the fields of the table.
   * @param keyType The type of the key.
   * @param rcdClass The class of the record.
   * @param keyClass The class of the key.
   */
  public DBFactory(String tableName, DBField field[], int keyType, Class rcdClass, Class keyClass) {
    this.tableName = tableName;
    this.field = field;
    this.rcdClass = (rcdClass != null) ? rcdClass : DBRecord.class;
    this.keyClass = keyClass;
    this.keyType = keyType;

    /* key fields */
    Vector pk = new Vector();
    Vector ak = new Vector();
    for (int i = 0; i < this.field.length; i++) {
      this.field[i].setFactory(this);
      if (this.field[i].isPriKey()) {
        pk.add(this.field[i]);
      }
      if (this.field[i].isAltKey()) {
        ak.add(this.field[i]);
      }
    }
    this.priKeys = (DBField[]) pk.toArray(new DBField[pk.size()]);
    this.altKeys = (DBField[]) ak.toArray(new DBField[ak.size()]);

    DBFactory.factoryList.add(this);
  }

  // ------------------------------------------------------------------------

  /**
   * Gets all the columns currently in a table and puts them in an array.  
   * Each colum has a name, a type, and whether it is a primary key.
   * @return An array containing all the columns of the database.
   * @throws DBException When there are any errors occur in the process
   *         of getting the columns.  
   */
  protected DBField[] getTableColumns() throws DBException {
    String showCols = "SHOW COLUMNS FROM " + this.getTableName();
    Statement stmt = null;
    ResultSet rs = null;
    Vector dbf = new Vector();
    try {
      stmt = DBConnection.getDefaultConnection().execute(showCols);
      rs = stmt.getResultSet();
      while (rs.next()) {
        String name = rs.getString("Field");
        String type = rs.getString("Type");
        boolean isPriKey = !"".equals(rs.getString("Key"));
        dbf.add(new DBField(name, type, isPriKey));
      }
    }
    catch (SQLException sqe) {
      throw new DBException("Unable to get fields", sqe);
    }
    finally {
      if (rs != null) {
        try {
          rs.close();
        }
        catch (Throwable t) {
        }
      }
      if (stmt != null) {
        try {
          stmt.close();
        }
        catch (Throwable t) {
        }
      }
    }
    return (DBField[]) dbf.toArray(new DBField[dbf.size()]);
  }

  /**
   * Checks to see if the defined columns matches the columns currently in the
   * table. First, it checks to see if the number of defined columns is the same
   * of the number of columns in the table. Then it column individually. This 
   * method may have a BUG. When comparing columns individually, it assumes that 
   * the two array, one containing defined columns and one containing columns in 
   * the table, are sorted. If they were sorted, the method works as expected. 
   * Otherwise, the method does not work as expected. This can be fixed by sorting
   * the two array before comparing them.
   * @return False if the number of defined column differs from the number of 
   * column currently in the table or if the name of the columns between the two 
   * are different. Otherwise, returns true.
   */
  public boolean validateColumns() {

    /* defined columns */
    DBField dbf[] = this.getFields();
    if ((dbf == null) || (dbf.length == 0)) {
      Print.logError(this.getTableName() + ": No table columns defined!!!");
      return false;
    }

    /* table columns */
    DBField dbc[];
    try {
      ;
      dbc = this.getTableColumns();
    }
    catch (DBException dbe) {
      Print.logError(this.getTableName() + ": Error reading table columns!");
      return false;
    }

    /* same number of columns? */
    if (dbf.length != dbc.length) {
      Print.logError(this.getTableName() + ": invalid number of fields");
      return false;
    }

    /* compare individual columns */
    for (int i = 0; i < dbf.length; i++) {
      String dbfn = dbf[i].getName(), dbcn = dbc[i].getName();
      if (!dbfn.equals(dbcn)) {
        Print.logError(this.getTableName() + ": Found '" + dbcn + "', expected '" + dbfn + "'");
        return false;
      }
    }
    return true;

  }

  // ------------------------------------------------------------------------

  /**
   * Maps old name to new name. However, I am not sure what Martin tries to
   * map.
   * @param fn The new name.
   * @return return the new name.
   */
  public String getMappedFieldName(String fn) { // map old name to new name
    if (this.getTableName().equals("AssetEvent") && fn.equals("alarmCode")) {
      return "statusCode";
    }
    return fn;
  }

  /**
   * Gets all the defined columns.
   * @return An array containing all the columns in a table.
   */
  public DBField[] getFields() {
    return this.field;
  }

  /**
   * Gets the index of a column by name in the array (DBField[]) that contains all the 
   * defined columns.
   * @param name The name of the column.
   * @return The index of the column in the array (DBField[]) containing all the 
   *         defined columns. If the name of the column is not found, returns -1.
   */
  public int getFieldIndex(String name) {
    if (name != null) {
      String realName = this.getMappedFieldName(name);
      DBField f[] = this.getFields();
      for (int i = 0; i < f.length; i++) {
        if (f[i].getName().equals(realName)) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Gets a defined column by name. 
   * @param name The name of the column.
   * @return The specified column. If the column does not exist, returns null.
   */
  public DBField getField(String name) {
    int x = this.getFieldIndex(name);
    return (x >= 0) ? this.getFields()[x] : null;
  }

  /**
   * Gets the names of all defined columns and saves them to a String array.
   * @return A String array containing the names of all columns in the table.
   */
  public String[] getFieldNames() {
    DBField f[] = this.getFields();
    String fn[] = new String[f.length];
    for (int i = 0; i < f.length; i++) {
      fn[i] = f[i].getName();
    }
    return fn;
  }

  /**
   * I don't know what this method does. 
   * @param key 
   * @param value
   * @return A DBField array containing all the fields.
   */
  public DBField[] getFieldsWithBoolean(String key, boolean value) {
    Vector af = new Vector();
    for (int i = 0; i < this.field.length; i++) {
      if (this.field[i].getBooleanAttribute(key) == value) {
        af.add(this.field[i]);
      }
    }
    return (DBField[]) af.toArray(new DBField[af.size()]);
  }

  /**
   * Gets all defined columns and puts them to an array of type DBField[]. 
   * The difference between this and the getField method is that this method's
   * parameter is an array containing all column names. The parameter of the
   * getField accepts only a single name as its parameter. If the name is invalid,
   * no column will be returned. Thus, the size of the returned array may be less 
   * the size of the parameter array.
   * @param fieldNames An array containing all column names.
   * @return An array containing all defined columns whose names (valid names) are specified in
   * specified in the parameter.
   */
  public DBField[] getNamedFields(String fieldNames[]) {
    Vector fields = new Vector();
    for (int i = 0; i < fieldNames.length; i++) {
      DBField fld = this.getField(fieldNames[i]);
      if (fld != null) {
        fields.add(fld);
      }
      else {
        Print.logStackTrace("Invalid field for table: " + fieldNames[i]);
      }
    }
    return (DBField[]) fields.toArray(new DBField[fields.size()]);
  }

  // ------------------------------------------------------------------------

  /**
   * Gets all of the columns whose keys are the primary keys in the table.
   * @return A DBField array containing columns whose keys are primar keys
   * in the table.
   */
  public DBField[] getKeyFields() {
    return this.priKeys; // should never be null
  }

  /**
   * Gets the names of the columns whose keys are the primary keys in the table.
   * @return A String array containing the names of the columns whose keys are
   * primary keys in the table.
   */
  public String[] getKeyNames() {
    DBField f[] = this.getKeyFields();
    String kn[] = new String[f.length];
    for (int i = 0; i < f.length; i++) {
      kn[i] = f[i].getName();
    }
    return kn;
  }

  /**
   * Gets the names of the columns whose keys are the primary keys in the table. 
   * @return A string containing all the names of the colums whose keys are
   * the primary keys in the table. Names are delimited by commas.
   */
  public String getSelectKeyNames() {
    DBField f[] = this.getKeyFields();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < f.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(f[i].getName());
    }
    return sb.toString();
  }

  /**
   * Gets the type of the key.
   * @return The type of the key.
   */
  public String getKeyType() {
    return DBFactory.getKeyTypeName(this.keyType);
  }

  /**
   * Gets the type of a key. A type can be primary, unique, or index. 
   * The type is unknown when it is not one of the three types mentioned.
   * @param type The number corresponding to a specific type (1 for primary,
   * 2 for unique, and 3 for index).
   * @return The type of the key. Returns "UNKNOWN" if it is not one of the 
   * three types mentioned.
   */
  public static String getKeyTypeName(int type) {
    switch (type) {
    case KEY_PRIMARY:
      return "PRIMARY KEY";
    case KEY_UNIQUE:
      return "UNIQUE";
    case KEY_INDEX:
      return "INDEX";
    default:
      return "UNKNOWN";
    }
  }

  /**
   * Gets the class of the key.
   * @return The class of the key.
   */
  public Class getKeyClass() {
    return this.keyClass;
  }

  /**
   * Create an instance of DBRecordKey.
   * @return An instance of DBRRecordKey.
   * @throws DBException If any errors occur.
   */
  public DBRecordKey createKey() throws DBException {
    if (this.keyClass != null) {
      try {
        Constructor kc = this.keyClass.getConstructor(new Class[0]);
        return (DBRecordKey) kc.newInstance(new Object[0]);
      }
      catch (Throwable t) { // NoSuchMethodException, ...
        // Implementation error (should never occur)
        throw new DBException("Key Creation", t);
      }
    }
    return null;
  }

  /**
   * Sets the name and value for all the columns whose keys are primary key. The
   * names and the values are in the Resultset which come from an SQL statement 
   * execution.
   * @param rs Any ResultSet
   * @return A instance of DBRecordKey is looped. Thus, I think any array should
   * be returned or the method should be "void". Thus, I am not sure what Martin 
   * wanted to do in this method.
   * @throws DBException
   */
  protected DBRecordKey createKey(ResultSet rs) throws DBException {
    DBRecordKey key = this.createKey(); // may throw DBException
    if (rs != null) {
      DBField pk[] = this.getKeyFields();
      try {
        for (int i = 0; i < pk.length; i++) {
          key.setFieldValue(pk[i].getName(), pk[i].getResultSetValue(rs));
        }
      }
      catch (SQLException sqe) {
        throw new DBException("Creating Key", sqe);
      }
    }
    return key;
  }

  // ------------------------------------------------------------------------

  /**
   * Gets all columns whose keys are alternate keys and saves them in an array
   * of type DBField.
   * @return A DBField array contains all columns whose keys are alternate keys.
   */
  public DBField[] getAltKeyFields() {
    return this.altKeys;
  }

  // ------------------------------------------------------------------------

  /**
   * Gets a class of the record.
   */
  public Class getRecordClass() {
    return this.rcdClass;
  }

  /**
   * Gets the name of a table.
   * @return The name of the table.
   */
  public String getTableName() {
    return this.tableName;
  }

  /**
   * Check to see if the table exists in the database. 
   * @return True if the table exists. If the table or the database does not
   * exit, return false.
   * @throws DBException If any errors occur.
   */
  public boolean tableExists() throws DBException {
    try {
      StringBuffer sb = new StringBuffer();
      sb.append("SELECT COUNT(*) FROM ").append(this.getTableName());
      this.execute(sb.toString()); // may throw DBException, SQLException
      return true;
    }
    catch (SQLException sqe) {
      if (sqe.getErrorCode() == SQLERR_TABLE_NONEXIST) {
        return false;
      }
      else if (sqe.getErrorCode() == SQLERR_UNKNOWN_DATABASE) {
        String dbName = DBConnection.getDBName();
        Print.logError("Database does not exist '" + dbName + "'"); // thus, table does not exist
        return false;
      }
      else {
        String dbName = DBConnection.getDBName();
        throw new DBException("Table Existance '" + dbName + "'", sqe);
      }
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Creates a log file.  
   * @author Martin D. Flynn
   * @author Kiet Huynh
   */
  public static class ValidationLog {

    /** The name of the table. */
    private String tableName = "";
    /** The header of the log. */
    private String header = "";
    /** The output string buffer. */
    private StringBuffer out = null;
    /** Boolean variable indicating if "warnings" is included or not. */
    private boolean inclWarn = true;
    /** The number of errors. */
    private int hasErrors = 0;

    /**
     * Initializes table name and the "inclWarn" when an object of this class
     * is created.
     * @param tableName The name of the table.
     * @param inclWarn Boolean variable indicating if "warnings" is included or not.
     */
    public ValidationLog(String tableName, boolean inclWarn) {
      this.tableName = (tableName != null) ? tableName : "";
      this.inclWarn = inclWarn;
      this.out = new StringBuffer();
    }

    /** 
     * Sets the header of the log.
     * @param header The header of the log.
     */
    public void logHeader(String header) {
      this.header = (header != null) ? header : "";
    }

    /**
     * Checks if the log has a header.
     * @return True if the log has a header. Otherwise, returns false.
     */
    public boolean hasHeader() {
      return !this.header.equals("");
    }

    /**
     * Appends a message to the log file. 
     * @param msg The message to be appended.
     */
    public void logInfo(String msg) {
      this.out.append("\n  [INFO] " + msg);
    }

    /**
     * Appends warnings to the log if "inclWarn" is true. 
     * The number of errors in incremented by 1 of a log
     * is appended.
     * @param msg The message to be appended.
     */
    public void logWarn(String msg) {
      if (this.inclWarn) {
        this.out.append("\n  [WARN] " + msg);
        this.hasErrors++;
      }
    }

    /**
     * Appends warning to the log if the errors is severe no matter what value
     * of the "inclWarn". The number of errors in incremented by 1 of a log
     * is appended.
     * @param msg The message to be appended.
     */
    public void logSevere(String msg) {
      this.out.append("\n  [SEVERE] " + msg);
      this.hasErrors++;
    }

    /**
     * Checks if there are any errors.
     * @return True if there are any errors. Otherwise, false.
     */
    public boolean hasErrors() {
      return (this.hasErrors > 0);
    }

    /**
     * Convert the log to a string.
     * @return The string representing the log.
     */
    public String toString() {
      return this.header + this.out.toString();
    }
  }

  /**
   * Handles exceptions when a table is validated. 
   * @author Martin D. Flynn
   * @author Kiet Huynh
   *
   */
  public static class ValidationNotImplementedException extends Exception {

    /**
     * ValidationNotImplentedException is instantiated.
     * @param msg Error message.
     */
    public ValidationNotImplementedException(String msg) {
      super(msg);
    }
  }

  /**
   * Validates a table. I am not sure what in the table Martin tries to validate.
   * @param inclWarn Specifies whether warning should be include in the log.
   * @return True if the table passes the validation. Otherwise, return false.
   */
  public boolean validateTable(boolean inclWarn) {
    // This method is intended to be executed from the command line
    String TN = this.getTableName();
    Print.logInfo("");
    Print.logInfo("Validating " + TN + ":");

    boolean pass = true;
    try {

      /* validation constructor */
      MethodAction valConst = null;
      try {
        valConst = new MethodAction(this.getRecordClass(), null, new Class[] { ResultSet.class,
            ValidationLog.class });
      }
      catch (Throwable t) { // NoSuchMethodException, ...
        throw new ValidationNotImplementedException("Missing validation Constructor");
      }

      /* 'select' */
      StringBuffer sbSelect = new StringBuffer();
      sbSelect.append("SELECT * FROM ").append(TN); //.append(" ORDER BY ").append(FLD_ownerKey);
      Statement stmt = DBConnection.getDefaultConnection().execute(sbSelect.toString());
      ResultSet rs = stmt.getResultSet();
      while (rs.next()) {
        ValidationLog failLog = new ValidationLog(TN, inclWarn);
        try {
          valConst.invoke(new Object[] { rs, failLog });
          if (failLog.hasErrors()) {
            Print.logError(failLog.toString());
            pass = false;
          }
          else if (inclWarn && !failLog.hasHeader()) {
            throw new ValidationNotImplementedException("No log header");
          }
        }
        catch (Throwable t) { // InvocationTargetException, ValidationNotImplementedException, ...
          Print.logException("Validating " + TN + ": ", t);
          pass = false;
        }
      }

      /* close */
      try {
        rs.close();
      }
      catch (Throwable t) {
      }
      try {
        stmt.close();
      }
      catch (Throwable t) {
      }

      /* passed? */
      if (pass) {
        String e = inclWarn ? "errors/warnings" : "severe errors";
        Print.logInfo("  No " + e + " detected");
      }
      return pass;

    }
    catch (ValidationNotImplementedException vnie) {
      Print.logError("  Validation not implemented: " + vnie.getMessage());
    }
    catch (DBException dbe) {
      Print.logException("Validating " + TN + ": ", dbe);
    }
    catch (SQLException sqe) {
      Print.logException("Validating " + TN + ": ", sqe);
    }

    return false;

  }

  // ------------------------------------------------------------------------

  /**
   * Creates a database table. The name of the table is the value of the instance variable 
   * "tableName", all the fields are the values of the instance variables "field", "priKeys",
   * "altKeys".
   * @throws SQLException If any database access errors occur.
   * @throws DBException If any dababase access errors occur. MySQl is
   *         not running or it can't find the MySQL server.
   */
  public void createTable() throws DBException {
    try {
      this._createTable();
    }
    catch (SQLException sqe) {
      throw new DBException("Table creation", sqe);
    }
  }

  /**
   * Creates a database table. The name of the table is the value of the instance variable 
   * "tableName", all the fields are the values of the instance variables "field", "priKeys",
   * "altKeys".
   * @throws SQLException If any database access errors occur.
   * @throws DBException If any dababase access errors occur. MySQl is
   *         not running or it can't find the MySQL server.
   */
  protected void _createTable() throws SQLException, DBException {
    StringBuffer sb = new StringBuffer();
    // MySQL: create table <TableName> ( <Fields...>, <KeyType> ( <Keys...> ), <KeyIndex> altindex ( <AltKeys...> ) )
    sb.append("CREATE TABLE ").append(this.getTableName()).append(" (");

    /* fields */
    DBField f[] = this.getFields();
    for (int fx = 0; fx < f.length; fx++) {
      if (fx > 0) {
        sb.append(", ");
      }
      sb.append(f[fx].getFieldDefinition());
    }

    /* primary keys */
    DBField pk[] = this.getKeyFields();
    if (pk.length > 0) {
      sb.append(", ");
      sb.append(this.getKeyType()).append(" (");
      for (int pkx = 0; pkx < pk.length; pkx++) {
        if (pkx > 0) {
          sb.append(", ");
        }
        sb.append(pk[pkx].getName());
      }
      sb.append(")");
    }

    /* alternate keys */
    DBField ak[] = this.getAltKeyFields();
    if (ak.length > 0) {
      // ", altIndex ( alt1, alt2, alt3 )"
      sb.append(", ");
      sb.append(DBFactory.getKeyTypeName(KEY_INDEX)).append(" altIndex (");
      for (int akx = 0; akx < ak.length; akx++) {
        if (akx > 0) {
          sb.append(", ");
        }
        sb.append(ak[akx].getName());
      }
      sb.append(")");
    }

    /* table type */
    sb.append(") type=").append(DBTABLE_TYPE);
    this.executeUpdate(sb.toString());

  }

  // ------------------------------------------------------------------------

  /**
   * Drop a database table if it exists. The name of the table is the value of the instance 
   * variable "tableName".
   * @throws SQLException If any database access errors occur, usually because table does
   * not exist.
   * @throws DBException If any dababase access errors occur. MySQl is
   *         not running or it can't find the MySQL server.
   */
  public void dropTable() throws DBException {
    try {
      this._dropTable();
    }
    catch (SQLException sqe) {
      throw new DBException("Drop table", sqe);
    }
  }

  /**
   * Drop a database table if it exists. The name of the table is the value of the instance 
   * variable "tableName".
   * @throws SQLException If any database access errors occur, usually because table does
   * not exist.
   * @throws DBException If any dababase access errors occur. MySQl is
   *         not running or it can't find the MySQL server.
   */
  protected void _dropTable() throws SQLException, DBException {
    String drop = "DROP TABLE IF EXISTS " + this.getTableName();
    // MySQL: drop table if exists <TableName>
    this.executeUpdate(drop);
  }

  // ------------------------------------------------------------------------

  /**
   * Writes the contents of the database table into a file. The method
   * should also throws IOException and SQLException so the caller can know
   * which type of errors has occured. The file format is:
   * <ul>
   * <li>
   * The first line of the file contains the fields' name. It has the format:
   * <p># NameOfField1, NameOfField2, NameOfField3,...</p>
   * </li>
   * <li>
   * Records or rows of the table starting from the second line. It has the format:
   * <p>Value1, Value2, Value3, ...</p>
   * </li>
   * </ul>
   * 
   * @param toFile The file to which the contents of the table will be written.
   * @throws DBException If the dababase access errors occur. MySQl is
   *         not running or it can't find the MySQL server or If the database 
   *         access errors occur. 
   */
  public void dumpTable(File toFile) throws DBException {
    this._dumpTable(toFile);
  }

  /**
   * Writes the contents of the database table into a file. The method
   * should also throws IOException and SQLException so the caller can know
   * which type of errors has occured. The file format is:
   * <ul>
   * <li>
   * The first line of the file contains the fields' name. It has the format:
   * <p># NameOfField1, NameOfField2, NameOfField3,...</p>
   * </li>
   * <li>
   * Records or rows of the table starting from the second line. It has the format:
   * <p>Value1, Value2, Value3, ...</p>
   * </li>
   * </ul>
   * 
   * @param toFile The file to which the contents of the table will be written.
   * @throws DBException If the dababase access errors occur. MySQl is
   *         not running or it can't find the MySQL server or If the database 
   *         access errors occur. 
   */
  protected void _dumpTable(File toFile) throws DBException {
    DBField fields[] = this.getFields();
    FileOutputStream dumpOutStream = null;
    Statement sqlStatement = null;
    ResultSet sqlResultSet = null;

    try {
      dumpOutStream = new FileOutputStream(toFile);

      /* field definition */
      StringBuffer sbFields = new StringBuffer();
      sbFields.append("# ");
      for (int i = 0; i < fields.length; i++) {
        if (i > 0) {
          sbFields.append(", ");
        }
        sbFields.append(fields[i].getName());
      }
      sbFields.append("\n");
      dumpOutStream.write(StringTools.getBytes(sbFields.toString()));

      /* select/dump all */
      sqlStatement = DBRecord.getStatement(this, null);
      sqlResultSet = sqlStatement.getResultSet();
      StringBuffer sbValues = new StringBuffer();
      while (sqlResultSet.next()) {
        sbValues.setLength(0);
        for (int i = 0; i < fields.length; i++) {
          if (i > 0) {
            sbValues.append(", ");
          }
          String value = sqlResultSet.getString(fields[i].getName());
          sbValues.append(fields[i].getQValue(value));
        }
        sbValues.append("\n");
        dumpOutStream.write(StringTools.getBytes(sbValues.toString()));
      }

    }
    catch (IOException ioe) {
      throw new DBException("Dumping table", ioe);
    }
    catch (SQLException sqe) {
      throw new DBException("Dumping table", sqe);
    }
    finally {
      if (sqlResultSet != null) {
        try {
          sqlResultSet.close();
        }
        catch (SQLException sqe) {
        }
      }
      if (sqlStatement != null) {
        try {
          sqlStatement.close();
        }
        catch (SQLException sqe) {
        }
      }
      if (dumpOutStream != null) {
        try {
          dumpOutStream.close();
        }
        catch (IOException ioe) {
        }
      }
    }

  }

  // ------------------------------------------------------------------------

  /**
   * Reads input from a sql dump files. 
   * @author Martin D. Flynn
   * @author Kiet Huynh
   */
  protected static class MySQLDumpReader {

    /** The number that indicate end-0f-file. */
    private int pushedByte = -1;
    /** The File Input Stream. */
    private FileInputStream fis = null;

    /**
     * Initializes input file when an instance of this class is created.
     * @param file The name of the input file.
     * @throws IOException When any IO errors occur.
     */
    public MySQLDumpReader(File file) throws IOException {
      super();
      this.fis = new FileInputStream(file);
    }

    /**
     * Reads a line from the file.   
     * @return The line read.
     * @throws IOException
     */
    public String readLineString() throws IOException {
      byte buff[] = this.readLineBytes();
      if (buff != null) {
        String line = new String(buff, 0, buff.length, StringTools.DEFAULT_CHARSET);
        //Print.logDebug("Line: " + line + " [" + buff.length + "/" + line.length() + "]");
        return line;
      }
      else {
        return null;
      }
    }

    /**
     * Reads a single line from the input file.
     * @return An array containing the line read.
     * @throws IOException When any IO errors occur.
     */
    public byte[] readLineBytes() throws IOException {
      byte buff[] = new byte[10 * 1024];
      int len = 0;
      boolean quoted = false;
      boolean eof = false;
      for (; len < buff.length;) {

        /* read single byte */
        int ch = this.read();
        if (ch < 0) {
          eof = true;
          break;
        }
        //Print.logDebug("Char: " + ((char)ch) + " [" + ch);

        /* parse character */
        if (ch == '\"') {
          quoted = !quoted;
          buff[len++] = '\"';
        }
        else if (ch == '\\') {
          buff[len++] = '\\';
          ch = this.read(); // read next character
          if (ch < 0) {
            break;
          }
          buff[len++] = (byte) ch; // unfiltered if preceded with \
        }
        else if (quoted) {
          buff[len++] = (byte) ch; // unfiltered if quoted
        }
        else if (ch == '\r') {
          ch = this.read(); // skip '\n' (if present)
          if ((ch >= 0) && (ch != '\n')) {
            this.pushedByte = ch & 0xFF;
          }
          break; // end-of-line
        }
        else if (ch == '\n') {
          break; // end-of-line
        }
        else {
          buff[len++] = (byte) ch; // unfiltered
        }

      }
      if (!eof || (len > 0)) {
        byte line[] = new byte[len];
        System.arraycopy(buff, 0, line, 0, len);
        return line;
      }
      else {
        return null;
      }
    }

    /**
     * Reads in a single character of the file. 
     * @return The character read or -1 if end-of-file is reached.
     * @throws IOException If any IO errors occur.
     */
    private int read() throws IOException {
      int b = -1;
      if (this.pushedByte >= 0) {
        b = (byte) this.pushedByte;
        this.pushedByte = -1;
      }
      else {
        b = this.fis.read();
      }
      return (b == -1) ? -1 : (b & 0xFF);
    }

    /**
     * Closes the input stream.
     * @throws IOException When there are any IO errors occur.
     */
    public void close() throws IOException {
      this.fis.close();
    }
  }

  // ------------------------------------------------------------------------

  /** The extention of the dump file that will be loaded. */
  protected static String LOAD_EXT_DUMP = ".dump";
  /** The extention of the sql file that will be loaded. */
  protected static String LOAD_EXT_SQL = ".sql";
  /** The extention of the text file that will be loaded */
  protected static String LOAD_EXT_TXT = ".txt";

  /**
   * Creates a database table from a input file. The file can have extension .txt, sql,
   * .dmp. If the file is not a text file, convert 
   * it to the text file. Then the method reads in the contents of the text  
   * file and uses it to construct SQL statments. It then executes the SQL statements
   * to create the desired table.
   * @param fromFile The file name of the text file whose contents will be used to 
   * created the database. The file format is:
   * <ul>
   * <li>
   * The first line of the file contains the fields' name. It has the format:
   * <p># NameOfField1, NameOfField2, NameOfField3,...</p>
   * </li>
   * <li>
   * Records or rows of the table starting from the second line. It has the format:
   * <p>Value1, Value2, Value3, ...</p>
   * </li>
   * </ul>
   * @throws DBException If the file extension is not recognized.
   */
  public void loadTable(File fromFile) throws DBException {
    String fn = fromFile.getName();
    if (fn.endsWith(LOAD_EXT_DUMP)) {
      this._loadTable(null, fromFile);
    }
    else if (fn.endsWith(LOAD_EXT_SQL)) {
      File sqlFile = fromFile;
      String fields[] = this.readSQLDumpColumns(sqlFile);
      File txtFile = new File(FileTools.removeExtension(fromFile.getPath()) + LOAD_EXT_TXT);
      this._loadTable(fields, txtFile);
    }
    else if (fn.endsWith(LOAD_EXT_TXT)) {
      File sqlFile = new File(FileTools.removeExtension(fromFile.getPath()) + LOAD_EXT_SQL);
      String fields[] = this.readSQLDumpColumns(sqlFile);
      File txtFile = fromFile;
      this._loadTable(fields, txtFile);
    }
    else {
      throw new DBException("Unrecognized file extension '" + fromFile + "'");
    }
  }

  /**
   * Creates a database table. The method first reads in the contents of the text  
   * file and uses it to construct SQL statments. It then executes the SQL statements
   * to create the desired table.
   * @param oldFieldNames 
   * @param fromFile The file name of the text file whose contents will be used to 
   * created the database. The file format is:
   * <ul>
   * <li>
   * The first line of the file contains the fields' name. It has the format:
   * <p># NameOfField1, NameOfField2, NameOfField3,...</p>
   * </li>
   * <li>
   * Records or rows of the table starting from the second line. It has the format:
   * <p>Value1, Value2, Value3, ...</p>
   * </li>
   * </ul>
   * @throws DBException If there are errors reading the file or any database errors.
   */
  protected void _loadTable(String oldFieldNames[], File fromFile) throws DBException {
    DBField newFields[] = this.getFields();
    MySQLDumpReader fr = null;

    try {

      /* open file */
      fr = new MySQLDumpReader(fromFile);

      /* field/column definition */
      if (oldFieldNames == null) {
        String firstLine = fr.readLineString();
        if (firstLine.startsWith("#")) {
          oldFieldNames = StringTools.parseArray(firstLine.substring(1).trim());
        }
        else {
          Print.logError("Missing column definition, unable to load file");
          return;
        }
      }

      /* loop through file */
      for (;;) {

        /* read line */
        String r = fr.readLineString();
        if (r == null) {
          break;
        }
        if ((r == null) || r.startsWith("#")) {
          continue;
        }
        //if ((r.length == 0) || (r[0] == '#')) { continue; }

        /* parse line */
        String rowValues[] = StringTools.parseArray(r);
        //String partialKey = (rowValues.length > 0)? rowValues[0] : "?";
        if (rowValues.length != oldFieldNames.length) {
          Print.logError("Fields - #found != #expected: " + rowValues.length + " != "
              + oldFieldNames.length);
          continue;
        }

        /* set fields */
        DBRecordKey rcdKey = this.createKey(); // may throw DBException
        StringBuffer setSql = new StringBuffer(" SET ");
        boolean addedField = false;
        for (int i = 0; i < oldFieldNames.length; i++) {
          DBField field = this.getField(oldFieldNames[i]);
          if (field != null) {
            if (addedField) {
              setSql.append(", ");
            }

            String val = (i < rowValues.length) ? rowValues[i] : "";
            if (val.equals("\\N")) {
              val = "";
            } // NULL field entry

            /* get field=value pair */
            String fn = field.getName();
            String fqv = field.getQValue(val);
            //Print.logDebug(fn + "=" + fqv + " [" + val + " :" + val.length() + "]");

            /* add field to statement */
            setSql.append(fn).append("=").append(fqv);
            addedField = true;

            if (field.isPriKey() && !rcdKey.setFieldValue(fn, val)) {
              Print.logError("Couldn't find Key fieldName: " + fn);
            }

          }
          else {
            Print.logInfo("Column " + oldFieldNames[i] + " <dropped>");
          }
        }

        /* construct sql statement */
        StringBuffer sbSql = new StringBuffer();
        boolean update = rcdKey.exists(); // may throw DBException
        if (update) {
          sbSql.append("UPDATE ");
          sbSql.append(this.getTableName());
          sbSql.append(setSql.toString());
          sbSql.append(rcdKey.getWhereClause());
          //Print.logDebug("Updating: " + rcdKey);
        }
        else {
          sbSql.append("INSERT INTO ");
          sbSql.append(this.getTableName());
          sbSql.append(setSql.toString());
          //Print.logDebug("Inserting: " + rcdKey);
        }

        /* insert/update */
        Statement sqlStatement = null;
        try {
          //Print.logDebug("[SQL] " + sbSql);
          sqlStatement = DBConnection.getDefaultConnection().execute(sbSql.toString());
        }
        catch (SQLException sqe) {
          if (sqe.getErrorCode() == SQLERR_DUPLICATE_KEY) {
            Print.logInfo("Duplicate Key Skipped: " + rcdKey);
          }
          else {
            throw sqe;
          }
        }
        finally {
          if (sqlStatement != null) {
            try {
              sqlStatement.close();
            }
            catch (SQLException sqe) {
            }
          }
        }

      }

    }
    catch (SQLException sqe) {
      throw new DBException("SQL error", sqe);
    }
    catch (IOException ioe) {
      throw new DBException("Parsing error", ioe);
    }
    finally {
      if (fr != null) {
        try {
          fr.close();
        }
        catch (Throwable t) {
        }
      }
    }

  }

  /**
   * Reads all columns' names from a SQL table file and saves them to a String array.
   * @param tableSQLFile The name of the SQL table file.
   * @return A String array containing all columns' name.
   */
  private String[] readSQLDumpColumns(File tableSQLFile) {

    /* table */
    if (!tableSQLFile.exists() || tableSQLFile.isDirectory()) {
      return null;
    }

    /* parse */
    Vector clist = new Vector();
    BufferedReader fr = null;
    try {
      fr = new BufferedReader(new FileReader(tableSQLFile));
      for (;;) {
        String r = fr.readLine();
        if (r == null) {
          break;
        }
        if ((r.length() == 0) || !Character.isWhitespace(r.charAt(0))) {
          continue;
        }
        String cdef = r.trim();
        int p = cdef.indexOf(" ");
        String cnam = cdef.substring(0, p);
        if (!cnam.equalsIgnoreCase("PRIMARY") && !cnam.equalsIgnoreCase("KEY")) {
          clist.add(cnam);
        }
      }
    }
    catch (IOException ioe) {
      Print.logStackTrace("Parsing error", ioe);
      return null;
    }
    finally {
      if (fr != null) {
        try {
          fr.close();
        }
        catch (Throwable t) {
        }
      }
    }

    /* return columns */
    return (String[]) clist.toArray(new String[clist.size()]);

  }

  // ------------------------------------------------------------------------

  /**
   * Executes the given SQL statement, which may be an INSERT, UPDATE, 
   * or DELETE or an SQL statement that returns nothing.
   * @param sql Any SQL statement.
   * @throws SQLException If database access errors occurs or the given 
   *         SQL statement return a ResutlSet.
   * @throws DBException If MySQL is not runnig, or it can't find the 
   *         MySQL statement.
   */
  protected void executeUpdate(String sql) throws SQLException, DBException {
    DBConnection.getDefaultConnection().executeUpdate(sql);
  }

  /**
   * Executes the SQL statement.
   * @param sql Any SQL statment.
   * @return The result of execution. You must then use the methods
   * getResultSet or getUpdateCount to retrieve the result, and getMoreResults 
   * to move to any subsequent result(s).
   * @throws SQLException If any database access errors occur.
   * @throws DBException If any dababase access errors occur. MySQl is
   *         not running or it can't find the MySQL server.
   */
  protected Statement execute(String sql) throws SQLException, DBException {
    return DBConnection.getDefaultConnection().execute(sql);
  }

  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

  /**
   * Adds a prefix to a field name. The first character of the field name is
   * capitalized. For example, the field name is "minute" and the prefix is "Time".
   * Then after adding the prefix, it becomes "TimeMinute".
   * @return A String containing a prefix and the field name.
   */
  protected static String _beanMethodName(String prefix, String fieldName) {
    StringBuffer sb = new StringBuffer(prefix);
    sb.append(fieldName.substring(0, 1).toUpperCase());
    sb.append(fieldName.substring(1));
    return sb.toString();
  }

  /**
   * Returns a scope of a method. The scope is either public, protected, private,
   * or package.
   * @param mods The modifier.
   * @return The scopes of the method.
   */
  protected static String _methodScope(int mods) {
    if ((mods & Modifier.PUBLIC) == 1) {
      return "public";
    }
    else if ((mods & Modifier.PROTECTED) == 1) {
      return "protected";
    }
    else if ((mods & Modifier.PRIVATE) == 1) {
      return "private";
    }
    else {
      return "package";
    }
  }

  public void validateTableBeanMethods() {
    String tableName = this.getTableName();
    Class tableClass = this.getRecordClass();
    DBField field[] = this.getFields();
    Print.logInfo("");
    Print.logInfo("Validating bean access methods for table: " + tableName);
    for (int i = 0; i < field.length; i++) {
      String fldName = field[i].getName();
      Class typeClass = field[i].getTypeClass();
      boolean isPriKey = field[i].isPriKey();
      boolean isAltKey = field[i].isAltKey();

      /* header */
      Print.logInfo("  Field: " + fldName + " [type=" + typeClass.getName() + "]");
      boolean ok = true;

      /* check getter */
      String getMethN = _beanMethodName("get", fldName);
      Method getMethod = null;
      for (Class target = tableClass; target != null; target = target.getSuperclass()) {
        try {
          getMethod = target.getDeclaredMethod(getMethN, new Class[0]);
          break;
        }
        catch (NoSuchMethodException nsme) {
          // ignore and try again on next iteration
        }
      }
      if (getMethod != null) {
        Class rtnClass = getMethod.getReturnType();
        if (!rtnClass.equals(typeClass)) {
          Print.logError("    Invalid getter return type: " + rtnClass.getName());
          ok = false;
        }
        int mods = getMethod.getModifiers();
        if ((mods & Modifier.PUBLIC) == 0) {
          //Print.logError("    Invalid getter scope: " + _methodScope(mods));
          //ok = false;
        }
      }
      else {
        Print.logError("    Getter not found");
        ok = false;
      }

      /* check setter */
      boolean setFound = false;
      String setMethN = _beanMethodName("set", fldName);
      Method setMethod = null;
      for (Class target = tableClass; target != null; target = target.getSuperclass()) {
        try {
          setMethod = target.getDeclaredMethod(setMethN, new Class[] { typeClass });
          break;
        }
        catch (NoSuchMethodException nsme) {
          // ignore and try again on next iteration
        }
      }
      if (setMethod != null) {
        Class rtnClass = setMethod.getReturnType();
        if (!rtnClass.equals(Void.TYPE)) {
          Print.logError("    Invalid setter return type: " + rtnClass.getName());
          ok = false;
        }
        int mods = setMethod.getModifiers();
        if ((mods & Modifier.PUBLIC) == 0) {
          //Print.logError("    Invalid setter scope: " + _methodScope(mods));
          //ok = false;
        }
      }
      else {
        Print.logError("    Setter not found");
        ok = false;
      }

      /* ok? */
      if (ok) {
        Print.logInfo("    OK");
      }
      //Print.logInfo("");

    }
    Print.logInfo("");
  }

  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

  /**
   * Creates hibernate XML. Currently experimental purpose only.
   */
  public void createHibernateXML() {
    // currently experimental purposes only
    String tableName = this.getTableName();
    Class tableClass = this.getRecordClass();
    Class keyClass = this.getKeyClass();
    DBField key[] = this.getKeyFields();
    DBField field[] = this.getFields();
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\"?>\n");
    sb
        .append("<!DOCTYPE hibernate-mapping PUBLIC \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n");
    sb.append("<hibernate-mapping>\n");
    sb.append("  <class name=\"" + tableClass + "\" table=\"" + tableName + "\">\n");
    if (key.length > 0) {
      sb.append("\n");
      sb.append("    <composite-id name=\"key\" class=\"" + keyClass + "\">\n");
      for (int i = 0; i < key.length; i++) {
        String fldName = key[i].getName();
        Class typeClass = key[i].getTypeClass();
        String hibType = typeClass.getName(); // key[i].getHibernateType();
        int typeLen = key[i].getLength();
        String hibLen = (typeLen > 0) ? ("length=\"" + typeLen + "\"") : "";
        String title = key[i].getTitle();
        sb.append("\n");
        sb.append("      <!-- " + title + "-->\n");
        sb.append("      <key-property name=\"" + fldName + "\" type=\"" + hibType + "\" " + hibLen
            + "/>\n");
      }
      sb.append("\n");
      sb.append("    </composite-id>\n");
    }
    for (int i = 0; i < field.length; i++) {
      boolean isPriKey = field[i].isPriKey();
      boolean isAltKey = field[i].isAltKey();
      if (!isPriKey) {
        String fldName = field[i].getName();
        Class typeClass = field[i].getTypeClass();
        String hibType = typeClass.getName(); // field[i].getHibernateType();
        int typeLen = field[i].getLength();
        String hibLen = (typeLen > 0) ? ("length=\"" + typeLen + "\"") : "";
        String title = field[i].getTitle();
        sb.append("\n");
        sb.append("    <!-- " + title + "-->\n");
        sb.append("    <property  name=\"" + fldName + "\" type=\"" + hibType + "\" column=\""
            + fldName + "\" " + hibLen + ">\n");
      }
    }
    sb.append("\n");
    sb.append("  </class>\n");
    sb.append("</hibernate-mapping>\n");
    File xmlFile = new File("./" + tableName + ".hbm.xml");
    try {
      FileTools.writeFile(StringTools.getBytes(sb), xmlFile);
    }
    catch (IOException ioe) {
      Print.logError("Unable to write file: " + xmlFile + " [" + ioe + "]");
    }
  }

  // ------------------------------------------------------------------------

}
