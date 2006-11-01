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

import java.lang.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.io.*;
import java.text.*;
import java.sql.*;

import org.opendmtp.util.*;

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
  
  /** The type of a key. */
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

  
  public Class getKeyClass() {
    return this.keyClass;
  }

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

  public DBField[] getAltKeyFields() {
    return this.altKeys;
  }

  // ------------------------------------------------------------------------

  public Class getRecordClass() {
    return this.rcdClass;
  }

  public String getTableName() {
    return this.tableName;
  }

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

  public static class ValidationLog {
    private String tableName = "";
    private String header = "";
    private StringBuffer out = null;
    private boolean inclWarn = true;
    private int hasErrors = 0;

    public ValidationLog(String tableName, boolean inclWarn) {
      this.tableName = (tableName != null) ? tableName : "";
      this.inclWarn = inclWarn;
      this.out = new StringBuffer();
    }

    public void logHeader(String header) {
      this.header = (header != null) ? header : "";
    }

    public boolean hasHeader() {
      return !this.header.equals("");
    }

    public void logInfo(String msg) {
      this.out.append("\n  [INFO] " + msg);
    }

    public void logWarn(String msg) {
      if (this.inclWarn) {
        this.out.append("\n  [WARN] " + msg);
        this.hasErrors++;
      }
    }

    public void logSevere(String msg) {
      this.out.append("\n  [SEVERE] " + msg);
      this.hasErrors++;
    }

    public boolean hasErrors() {
      return (this.hasErrors > 0);
    }

    public String toString() {
      return this.header + this.out.toString();
    }
  }

  public static class ValidationNotImplementedException extends Exception {
    public ValidationNotImplementedException(String msg) {
      super(msg);
    }
  }

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

  public void createTable() throws DBException {
    try {
      this._createTable();
    }
    catch (SQLException sqe) {
      throw new DBException("Table creation", sqe);
    }
  }

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

  public void dropTable() throws DBException {
    try {
      this._dropTable();
    }
    catch (SQLException sqe) {
      throw new DBException("Drop table", sqe);
    }
  }

  protected void _dropTable() throws SQLException, DBException {
    String drop = "DROP TABLE IF EXISTS " + this.getTableName();
    // MySQL: drop table if exists <TableName>
    this.executeUpdate(drop);
  }

  // ------------------------------------------------------------------------

  public void dumpTable(File toFile) throws DBException {
    this._dumpTable(toFile);
  }

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

  protected static class MySQLDumpReader {
    private int pushedByte = -1;
    private FileInputStream fis = null;

    public MySQLDumpReader(File file) throws IOException {
      super();
      this.fis = new FileInputStream(file);
    }

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

    public void close() throws IOException {
      this.fis.close();
    }
  }

  // ------------------------------------------------------------------------

  protected static String LOAD_EXT_DUMP = ".dump";
  protected static String LOAD_EXT_SQL = ".sql";
  protected static String LOAD_EXT_TXT = ".txt";

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

  protected void executeUpdate(String sql) throws SQLException, DBException {
    DBConnection.getDefaultConnection().executeUpdate(sql);
  }

  protected Statement execute(String sql) throws SQLException, DBException {
    return DBConnection.getDefaultConnection().execute(sql);
  }

  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

  protected static String _beanMethodName(String prefix, String fieldName) {
    StringBuffer sb = new StringBuffer(prefix);
    sb.append(fieldName.substring(0, 1).toUpperCase());
    sb.append(fieldName.substring(1));
    return sb.toString();
  }

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
