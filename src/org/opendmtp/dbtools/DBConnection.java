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

import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.ResultSet; 
import java.sql.SQLException; 
import java.sql.Statement; 
import java.util.Map; 

import org.opendmtp.util.DateTime; 
import org.opendmtp.util.Print; 
import org.opendmtp.util.RTConfig; 
import org.opendmtp.util.RTKey; 
import org.opendmtp.util.ThreadLocalMap; 

/**
 * Creates connections to the MySQL database and sends SQL statements
 * to the database to get executed. It also stores results of the SQL 
 * statement execution.
 *
 * @author Martin D. Flynn
 * @author Kiet Huynh
 */
public class DBConnection {

  // ------------------------------------------------------------------------

  /**
   * The constant specifies the JDBC for MySQL server.
   */
  protected static final String MYSQL_JDBC = "mysql";

  /**
   * The constant specifies the driver needed to connect to the MySQL database server.
   */
  protected static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

  /**
   * The constant specifies the driver needed to connect to the MySQL database server.
   */
  protected static final String MYSQL_DRIVER_ALT = "org.gjt.mm.mysql.Driver"; // obsolete

  // ------------------------------------------------------------------------

  // 'true' will cause table locking problems
  // (see DBRecord.TABLE_LOCKING_ENABLED)
  /**
   * Boolean constant indicates if a new connection is always needed when 
   * there is a need to connect to the database. "true" will cause table 
   * locking problems. 
   */
  public static boolean ALWAYS_NEW_CONNECTION = false;

  /* this connection timeout must be less than what is configured in MySQL */
  /**
   * Connection timeout. This connection timeout must be less than what is 
   * configured in MySQL.
   */
  private static long CONNECTION_TIMEOUT = 6L * 3600L; // 6 hours

  // ------------------------------------------------------------------------

  /**
   * The last SQL statement executed by the database.
   */
  private static String LastSQLExecuted = null;

  // ------------------------------------------------------------------------

  /**
   * Gets that hostname of the computer in which mysql database server is running.
   * @return The hostname of the database server.
   */
  protected static String getDBHost() {
    return RTConfig.getString(RTKey.DB_HOST);
  }

  /**
   * Gets the port number through which to connection to the database server can
   * be made.
   * @return The port number.
   */
  protected static int getDBPort() {
    return RTConfig.getInt(RTKey.DB_PORT);
  }

  /**
   * Gets the username that can be used to connect to the database server.
   * @return The username that can be used to connect to the database server.
   */
  protected static String getDBUsername() {
    String user = RTConfig.getString(RTKey.DB_USER);
    return (user != null) ? user : "";
  }

  /**
   * Gets the password that can be used to connect to the database server.
   * @return The password that can be used to connect to the database server.
   */
  protected static String getDBPassword() {
    String pass = RTConfig.getString(RTKey.DB_PASS);
    return (pass != null) ? pass : "";
  }

  // ------------------------------------------------------------------------

  /**
   * Gets a default connection to the database.
   * @return The default connection to the database.
   */
  public static DBConnection getDefaultConnection() {
    String uri = DBConnection.getDBUri();
    String usr = DBConnection.getDBUsername();
    String pwd = DBConnection.getDBPassword();
    return DBConnection.getConnection(uri, usr, pwd);
  }

  /**
   * Gets a DBConnection from the dbConnectionsMap. If it is a new connection,
   * it will be added to the dbConnectionsMap. 
   * @param uri The URI of the database.
   * @param user The username used to connect to the database.
   * @param pass The password used to connect to the database.
   * @return A connection to the database.
   */
  public static DBConnection getConnection(String uri, String user, String pass) {
    Map dbConnMap = getDBConnectionMap();
    DBConnection dbconn = (DBConnection) dbConnMap.get(uri);
    if (dbconn == null) {
      if (RTConfig.isDebugMode()) {
        Print.logInfo("New Connection to " + uri);
      }
      dbconn = new DBConnection(uri, user, pass);
      dbConnMap.put(uri, dbconn);
    }
    return dbconn;
  }

  // ------------------------------------------------------------------------
  // Thread-Safety: Currently, each thread gets a new dedicated connection map

  /**
   * A Map that contain connections to the database.  Each thread get dedicated
   * connection map.
   */
  protected static Map dbConnectionsMap = new ThreadLocalMap(); // new Hashtable();

  /**
   * Gets the dbConnectionsMap which is a map that contain connections to the database.
   * @return The map that contain connections to the database.
   */
  protected static Map getDBConnectionMap() {
    return dbConnectionsMap;
  }

  // ------------------------------------------------------------------------

  /**
   * Gets the name of the database.
   * @return The name of the database.
   */
  public static String getDBName() {
    String dbName = RTConfig.getString(RTKey.DB_NAME);
    return dbName;
  }

  /**
   * Gets the URI of the database. This method may not work because 
   * there is no database name in the URI.
   * @return The URL of the server.
   */
  protected static String getDBUri() {
    return DBConnection.getDBUri(DBConnection.getDBHost(), DBConnection.getDBPort());
  }

  /**
   * Creates the complete URI of the database. The name of the database 
   * will be given.
   * @param host The host name of the database server.
   * @param port The port number through which a connection can be made to the 
   *             database server.
   * @return The URI of the database server.
   */
  protected static String getDBUri(String host, int port) {
    String dbName = DBConnection.getDBName();
    return DBConnection.getDBUri(host, port, dbName);
  }

  /**
   * Creates the complete URI of the database.
   * @param host - The hostname of the database.
   * @param port - The port number through which a connection can be made to the 
   *               database.
   * @param dbName - The name of the database.
   * @return The URI of the database.  If either of the given parameters is 
   *         null or "", it will be replaced with the default values.
   *      
   */
  public static String getDBUri(String host, int port, String dbName) {
    String h = ((host != null) && !host.equals("")) ? host : DBConnection.getDBHost();
    int p = (port > 0) ? port : DBConnection.getDBPort();
    // MySQL: jdbc:mysql://<Host>:<Port>/<DataBase>
    String uri = "jdbc:" + MYSQL_JDBC + "://" + h + ":" + p + "/";
    if ((dbName != null) && !dbName.equals("")) {
      uri += dbName;
    }
    return uri;
  }

  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

  /** The URI of the database. */
  private String dbUri = null;
  
  /** The username used to connect to the database. */
  private String userName = null;
  
  /** The password used to connect to the database. */
  private String password = null;
  
  /** The connection to the database. */
  private Connection dbConnection = null;
  
  /** The time at which the connection is made. */
  private long connectTime = 0L;
  
  /** The time at which the last connection was made. */
  private long lastUseTime = 0L;

  /**
   * Initializes all the fields needed to make a connection to the database.
   * @param uri The URI of the database.
   * @param user The username used to connect to the database.
   * @param pass The password used to connect to the database.
   */
  public DBConnection(String uri, String user, String pass) {
    this.dbUri = uri;
    this.userName = user;
    this.password = pass;
  }

  /**
   * Initializes all the field needed to make a connection to the database.
   * This contructor initializes username and password to null.
   * @param uri The URI of the database.
   */
  public DBConnection(String uri) {
    this(uri, null, null);
  }

  // ------------------------------------------------------------------------

  /**
   * Gets the URI of the database. 
   * @return The URI of the database. 
   */
  public String getUri() {
    return (this.dbUri != null) ? this.dbUri : DBConnection.getDBUri();
  }

  /**
   * Gets the username used to connect to the database.
   * @return The username used to connect to the database.
   */
  public String getUser() {
    return (this.userName != null) ? this.userName : DBConnection.getDBUsername();
  }

  /**
   * Gets the password used to connect to the database.
   * @return The password used to connect to the database.
   */
  public String getPassword() {
    return (this.password != null) ? this.password : DBConnection.getDBPassword();
  }

  // ------------------------------------------------------------------------

  /**
   * The name of loaded driver of the MySQL database. 
   */
  private static String loadedDriverName = null;

  /**
   * Makes a connection to the database.  If no driver found, an exception 
   * is thrown.
   * @return A connection to the database.
   * @throws SQLException The exception is thrown when no driver found.
   */
  public Connection getConnection() throws SQLException {
    if (this.isConnectionClosed()) {
      this.closeConnection(); // make sure it's closed
      if (loadedDriverName == null) {
        String driver[] = new String[] { MYSQL_DRIVER, MYSQL_DRIVER_ALT };
        for (int i = 0; i < driver.length; i++) {
          try {
            Class.forName(driver[i]);
            loadedDriverName = driver[i];
            break; // stop at the first one that works
          }
          catch (Throwable t) { // ClassNotFoundException
            Print.logError("JDBC driver not found: " + driver[i]);
            // continue
          }
        }
        if ((loadedDriverName != null) && RTConfig.isDebugMode()) {
          Print.logDebug("Loaded JDBC driver '" + loadedDriverName + "'");
        }
        else {
          // An exception should follow below
        }
      }
      String user = this.getUser();
      if ((user == null) || user.equals("")) {
        this.dbConnection = DriverManager.getConnection(this.getUri());
      }
      else {
        //Print.logDebug("User: " + user + " Password: " + this.getPassword());
        this.dbConnection = DriverManager.getConnection(this.getUri(), user, this.getPassword());
      }
      this.connectTime = DateTime.getCurrentTimeSec();
    }
    this.lastUseTime = DateTime.getCurrentTimeSec();
    return this.dbConnection;
  }

  /**
   * Closes a connection to the database. SQLException occurs when there are 
   * any errors.
   *
   */
  public void closeConnection() {
    if (this.dbConnection != null) {
      try {
        if (!this.dbConnection.isClosed()) {
          // try normal close
          try {
            this.dbConnection.close();
          }
          catch (SQLException sqe) {
          }
        }
      }
      catch (SQLException sqle) {
        // force close
        try {
          this.dbConnection.close();
        }
        catch (SQLException sqe) {
        }
      }
    }
  }

  /**
   * Checks if a connection to the database is closed. SQLException occurs when 
   * there are any errors. 
   * @return True if the connection is closed or if there are any errors. 
   *         Otherwise, returns false.
   */
  public boolean isConnectionClosed() {
    try {
      if (this.dbConnection == null) {
        return true;
      }
      else if (this.dbConnection.isClosed()) {
        return true;
      }
      else if (this.isConnectionTimeout()) {
        return true;
      }
      else {
        return ALWAYS_NEW_CONNECTION;
      }
    }
    catch (SQLException sqe) {
      return true;
    }
  }

  /** 
   * Checks if the connection is timeout.
   * @return True if connection is timeout. Otherwise returns false.
   */
  public boolean isConnectionTimeout() {
    long nowTime = DateTime.getCurrentTimeSec();
    return ((nowTime - this.lastUseTime) > CONNECTION_TIMEOUT);
  }

  // ------------------------------------------------------------------------

  /** 
   * Creates an SQL statement to send to the database to open a connection with 
   * the database.
   * @throws SQLException if any errors occur.
   * @return Statement from connection.
   */
  public Statement createStatement() throws SQLException {
    return this.getConnection().createStatement();
  }

  // ------------------------------------------------------------------------

  /**
   * Executes the SQL statement.  
   * @param sql SQL statement.
   * @return The result of the execution. You must then use the methods
   * getResultSet or getUpdateCount to retrieve the result, and getMoreResults 
   * to move to any subsequent result(s).
   * @throws SQLException If any database access errors occur.
   * @throws DBException If any dababase access errors occur. MySQl is
   *         not running or it can't find the MySQL server.
   */
  public Statement execute(String sql) throws SQLException, DBException {
    try {
      if (RTConfig.getBoolean(RTKey.DB_SHOW_SQL)) {
        Print.logInfo("SQL: " + sql);
      }
      return this._execute(sql);
    }
    catch (SQLException sqe) {
      // The most likely reason for an error here is a connection timeout on the MySQL server:
      //  v3.23.54 "Communication link failure: java.io.IOException"
      //  v4.0.18  "Communication link failure ... java.io.EOFException"
      String sqlMsg = sqe.getMessage();
      int errCode = sqe.getErrorCode();
      if ((sqlMsg.indexOf("IOException") >= 0) || (sqlMsg.indexOf("EOFException") >= 0)) {
        // close connection and retry with new connection
        this.closeConnection();
        return this._execute(sql);
      }
      else if ((errCode == DBFactory.SQLERR_SYNTAX_ERROR)
          || (errCode == DBFactory.SQLERR_UNKNOWN_COLUMN)) {
        // print sql statement for syntax errors
        Print.logError("SQL: " + sql);
        throw sqe;
      }
      else {
        throw sqe;
      }
    }
  }

  /**
   * Executes the SQL statement which may return multiple result sets.
   * @param sql SQL statement.
   * @return The result of the execution. You must then use the methods
   * getResultSet or getUpdateCount to retrieve the result, and getMoreResults 
   * to move to any subsequent result(s).
   * @throws SQLException If any database access errors occur.
   * @throws DBException If any dababase access errors occur. MySQl is
   *         not running or it can't find the MySQL server.
   */
  protected Statement _execute(String sql) throws SQLException, DBException {
    try {
      LastSQLExecuted = sql;
      Statement stmt = this.createStatement();
      stmt.execute(sql); // eg. "SELECT * FROM <db>"
      return stmt;
    }
    catch (com.mysql.jdbc.CommunicationsException ce) {
      // can occur if MySQL is not running, or it can't find the MySQL server
      throw new DBException("JDBC Error", ce);
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Executes the SQL statement which returns a single ResultSet object.
   * @param sql SQL statement.
   * @return The result of the execution.
   * @throws SQLException If the database access errors occur.
   * @throws DBException If the dababase access errors occur. MySQl is
   *         not running or it can't find the MySQL server.
   */
  public ResultSet executeQuery(String sql) throws SQLException, DBException {
    try {
      if (RTConfig.getBoolean(RTKey.DB_SHOW_SQL)) {
        Print.logInfo("SQL: " + sql);
      }
      return this._executeQuery(sql); // may throw DBException
    }
    catch (SQLException sqe) { // "Communication link failure: java.io.IOException"
      String sqlMsg = sqe.getMessage();
      int errCode = sqe.getErrorCode();
      if ((sqlMsg.indexOf("IOException") >= 0) || (sqlMsg.indexOf("EOFException") >= 0)) {
        this.closeConnection();
        return this._executeQuery(sql); // may throw DBException
      }
      else {
        throw sqe;
      }
    }
  }

  /**
   * Executes the SQL statement which returns a single ResultSet object.
   * @param sql Any SQL statement.
   * @return The result of the execution.
   * @throws SQLException If the database access errors occur.
   * @throws DBException If the dababase access errors occur. MySQl is
   *         not running or it can't find the MySQL server.
   */
  protected ResultSet _executeQuery(String sql) throws SQLException, DBException {
    try {
      LastSQLExecuted = sql;
      return this.createStatement().executeQuery(sql);
    }
    catch (com.mysql.jdbc.CommunicationsException ce) {
      // can occur if MySQL is not running, or it can't find the MySQL server
      throw new DBException("JDBC Error", ce);
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Executes the given SQL statement, which may be an INSERT, UPDATE, 
   * or DELETE or an SQL statement that returns nothing.
   * @param sql Any SQL statement
   * @throws SQLException If database access errors occurs or the given 
   *         SQL statement return a ResutlSet.
   * @throws DBException If MySQL is not runnig, or it can't find the 
   *         MySQL statement.
   */
  public void executeUpdate(String sql) throws SQLException, DBException {
    try {
      if (RTConfig.getBoolean(RTKey.DB_SHOW_SQL)) {
        Print.logInfo("SQL: " + sql);
      }
      this._executeUpdate(sql); // may throw DBException
      return;
    }
    catch (SQLException sqe) { // "Communication link failure: java.io.IOException"
      Print.logError("SQL: " + sql);
      String sqlMsg = sqe.getMessage();
      int errCode = sqe.getErrorCode();
      if ((sqlMsg.indexOf("IOException") >= 0) || (sqlMsg.indexOf("EOFException") >= 0)) {
        this.closeConnection();
        this._executeUpdate(sql); // may throw DBException
        return;
      }
      else {
        throw sqe;
      }
    }
  }

  /**
   * Executes the given SQL statement, which may be an INSERT, UPDATE, 
   * or DELETE or an SQL statement that returns nothing.
   * @param sql The SQL statement
   * @throws SQLException If database access errors occurs or the given 
   *         SQL statement return a ResutlSet.
   * @throws DBException If MySQL is not runnig, or it can't find the 
   *         MySQL statement.
   */
  protected void _executeUpdate(String sql) throws SQLException, DBException {
    try {
      LastSQLExecuted = sql;
      Statement stmt = this.createStatement();
      stmt.executeUpdate(sql);
      stmt.close();
    }
    catch (com.mysql.jdbc.CommunicationsException ce) {
      // can occur if MySQL is not running, or it can't find the MySQL server
      throw new DBException("JDBC Error", ce);
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Gets the username, password, and uri of the database. It seems like this 
   * method does nothing.
   */
  public static void main(String argv[]) {
    DBConnection.getDBUsername();
    DBConnection.getDBPassword();
    DBConnection.getDBUri();
  }

}
