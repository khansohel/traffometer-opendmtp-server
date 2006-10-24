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
import java.util.*;
import java.io.*;
import java.sql.*;

import org.opendmtp.util.*;

public class DBConnection
{

    // ------------------------------------------------------------------------

    protected static final String MYSQL_JDBC        = "mysql";

    protected static final String MYSQL_DRIVER      = "com.mysql.jdbc.Driver";
    protected static final String MYSQL_DRIVER_ALT  = "org.gjt.mm.mysql.Driver"; // obsolete
    
    // ------------------------------------------------------------------------

    // 'true' will cause table locking problems
    // (see DBRecord.TABLE_LOCKING_ENABLED)
    public  static boolean ALWAYS_NEW_CONNECTION    = false;
    
    /* this connection timeout must be less than what is configured in MySQL */
    private static long    CONNECTION_TIMEOUT       = 6L * 3600L; // 6 hours

    // ------------------------------------------------------------------------

    private static String LastSQLExecuted = null;
    
    // ------------------------------------------------------------------------

    protected static String getDBHost()
    {
        return RTConfig.getString(RTKey.DB_HOST);
    }

    protected static int getDBPort()
    {
        return RTConfig.getInt(RTKey.DB_PORT);
    }

    protected static String getDBUsername()
    {
        String user = RTConfig.getString(RTKey.DB_USER);
        return (user != null)? user : "";
    }

    protected static String getDBPassword()
    {
        String pass = RTConfig.getString(RTKey.DB_PASS);
        return (pass != null)? pass : "";
    }

    // ------------------------------------------------------------------------

    public static DBConnection getDefaultConnection()
    {
        String uri = DBConnection.getDBUri();
        String usr = DBConnection.getDBUsername();
        String pwd = DBConnection.getDBPassword();
        return DBConnection.getConnection(uri, usr, pwd);
    }

    public static DBConnection getConnection(String uri, String user, String pass)
    {
        Map dbConnMap = getDBConnectionMap();
        DBConnection dbconn = (DBConnection)dbConnMap.get(uri);
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
        
    protected static Map dbConnectionsMap = new ThreadLocalMap(); // new Hashtable();
    protected static Map getDBConnectionMap()
    {
        return dbConnectionsMap;
    }

    // ------------------------------------------------------------------------

    public static String getDBName()
    {
        String dbName = RTConfig.getString(RTKey.DB_NAME);
        return dbName;
    }
    
    protected static String getDBUri()
    {
        return DBConnection.getDBUri(DBConnection.getDBHost(), DBConnection.getDBPort());
    }

    protected static String getDBUri(String host, int port)
    {
        String dbName = DBConnection.getDBName();
        return DBConnection.getDBUri(host, port, dbName);
    }

    public static String getDBUri(String host, int port, String dbName)
    {
        String h = ((host != null) && !host.equals(""))? host : DBConnection.getDBHost();
        int    p = (port > 0)? port : DBConnection.getDBPort();
        // MySQL: jdbc:mysql://<Host>:<Port>/<DataBase>
        String uri = "jdbc:" + MYSQL_JDBC + "://" + h + ":" + p + "/";
        if ((dbName != null) && !dbName.equals("")) { uri += dbName; }
        return uri;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private String     dbUri        = null;
    private String     userName     = null;
    private String     password     = null;
    private Connection dbConnection = null;
    private long       connectTime  = 0L;
    private long       lastUseTime  = 0L;

    public DBConnection(String uri, String user, String pass)
    {
        this.dbUri    = uri;
        this.userName = user;
        this.password = pass;
    }

    public DBConnection(String uri)
    {
        this(uri, null, null);
    }
    
    // ------------------------------------------------------------------------

    public String getUri()
    {
        return (this.dbUri != null)? this.dbUri : DBConnection.getDBUri();
    }

    public String getUser()
    {
        return (this.userName != null)? this.userName : DBConnection.getDBUsername();
    }

    public String getPassword()
    {
        return (this.password != null)? this.password : DBConnection.getDBPassword();
    }

    // ------------------------------------------------------------------------

    private static String loadedDriverName = null;
    
    public Connection getConnection()
        throws SQLException
    {
        if (this.isConnectionClosed()) {
            this.closeConnection(); // make sure it's closed
            if (loadedDriverName == null) {
                String driver[] = new String[] { MYSQL_DRIVER, MYSQL_DRIVER_ALT };
                for (int i = 0; i < driver.length; i++) {
                    try {
                        Class.forName(driver[i]);
                        loadedDriverName = driver[i];
                        break; // stop at the first one that works
                    } catch (Throwable t) { // ClassNotFoundException
                        Print.logError("JDBC driver not found: " + driver[i]);
                        // continue
                    }
                }
                if ((loadedDriverName != null) && RTConfig.isDebugMode()) {
                    Print.logDebug("Loaded JDBC driver '" + loadedDriverName + "'");
                } else {
                    // An exception should follow below
                }
            }
            String user = this.getUser();
            if ((user == null) || user.equals("")) {
                this.dbConnection = DriverManager.getConnection(this.getUri());
            } else {
                //Print.logDebug("User: " + user + " Password: " + this.getPassword());
                this.dbConnection = DriverManager.getConnection(this.getUri(), user, this.getPassword());
            }
            this.connectTime = DateTime.getCurrentTimeSec();
        }
        this.lastUseTime = DateTime.getCurrentTimeSec();
        return this.dbConnection;
    }

    public void closeConnection()
    {
        if (this.dbConnection != null) {
            try {
                if (!this.dbConnection.isClosed()) {
                    // try normal close
                    try { this.dbConnection.close(); } catch (SQLException sqe) {}
                }
            } catch (SQLException sqle) {
                // force close
                try { this.dbConnection.close(); } catch (SQLException sqe) {}
            }
        }
    }
    
    public boolean isConnectionClosed()
    {
        try {
            if (this.dbConnection == null) {
                return true;
            } else
            if (this.dbConnection.isClosed()) {
                return true;
            } else
            if (this.isConnectionTimeout()) {
                return true;
            } else {
                return ALWAYS_NEW_CONNECTION;
            }
        } catch (SQLException sqe) {
            return true;
        }
    }
    
    public boolean isConnectionTimeout()
    {
        long nowTime = DateTime.getCurrentTimeSec();
        return ((nowTime - this.lastUseTime) > CONNECTION_TIMEOUT);
    }

    // ------------------------------------------------------------------------

    public Statement createStatement()
        throws SQLException
    {
        return this.getConnection().createStatement();
    }

    // ------------------------------------------------------------------------

    public Statement execute(String sql)
        throws SQLException, DBException
    {
        try {
            if (RTConfig.getBoolean(RTKey.DB_SHOW_SQL)) { Print.logInfo("SQL: " + sql); }
            return this._execute(sql);
        } catch (SQLException sqe) { 
            // The most likely reason for an error here is a connection timeout on the MySQL server:
            //  v3.23.54 "Communication link failure: java.io.IOException"
            //  v4.0.18  "Communication link failure ... java.io.EOFException"
            String sqlMsg = sqe.getMessage();
            int errCode   = sqe.getErrorCode();
            if ((sqlMsg.indexOf("IOException" ) >= 0) || 
                (sqlMsg.indexOf("EOFException") >= 0)   ) {
                // close connection and retry with new connection
                this.closeConnection();
                return this._execute(sql);
            } else
            if ((errCode == DBFactory.SQLERR_SYNTAX_ERROR)  ||
                (errCode == DBFactory.SQLERR_UNKNOWN_COLUMN)  ) {
                // print sql statement for syntax errors
                Print.logError("SQL: " + sql);
                throw sqe;
            } else {
                throw sqe;
            }
        }
    }

    protected Statement _execute(String sql)
        throws SQLException, DBException
    {
        try {
            LastSQLExecuted = sql;
            Statement stmt = this.createStatement();
            stmt.execute(sql); // eg. "SELECT * FROM <db>"
            return stmt;
        } catch (com.mysql.jdbc.CommunicationsException ce) {
            // can occur if MySQL is not running, or it can't find the MySQL server
            throw new DBException("JDBC Error", ce);
        }
    }

    // ------------------------------------------------------------------------

    public ResultSet executeQuery(String sql)
        throws SQLException, DBException
    {
        try {
            if (RTConfig.getBoolean(RTKey.DB_SHOW_SQL)) { Print.logInfo("SQL: " + sql); }
            return this._executeQuery(sql); // may throw DBException
        } catch (SQLException sqe) { // "Communication link failure: java.io.IOException"
            String sqlMsg = sqe.getMessage();
            int errCode   = sqe.getErrorCode();
            if ((sqlMsg.indexOf("IOException" ) >= 0) || 
                (sqlMsg.indexOf("EOFException") >= 0)   ) {
                this.closeConnection();
                return this._executeQuery(sql); // may throw DBException
            } else {
                throw sqe;
            }
        }
    }

    protected ResultSet _executeQuery(String sql)
        throws SQLException, DBException
    {
        try {
            LastSQLExecuted = sql;
            return this.createStatement().executeQuery(sql);
        } catch (com.mysql.jdbc.CommunicationsException ce) {
            // can occur if MySQL is not running, or it can't find the MySQL server
            throw new DBException("JDBC Error", ce);
        }
    }

    // ------------------------------------------------------------------------

    public void executeUpdate(String sql)
        throws SQLException, DBException
    {
        try {
            if (RTConfig.getBoolean(RTKey.DB_SHOW_SQL)) { Print.logInfo("SQL: " + sql); }
            this._executeUpdate(sql); // may throw DBException
            return;
        } catch (SQLException sqe) { // "Communication link failure: java.io.IOException"
            Print.logError("SQL: " + sql);
            String sqlMsg = sqe.getMessage();
            int errCode   = sqe.getErrorCode();
            if ((sqlMsg.indexOf("IOException" ) >= 0) || 
                (sqlMsg.indexOf("EOFException") >= 0)   ) {
                this.closeConnection();
                this._executeUpdate(sql); // may throw DBException
                return;
            } else {
                throw sqe;
            }
        }
    }

    protected void _executeUpdate(String sql)
        throws SQLException, DBException
    {
        try {
            LastSQLExecuted = sql;
            Statement stmt = this.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (com.mysql.jdbc.CommunicationsException ce) {
            // can occur if MySQL is not running, or it can't find the MySQL server
            throw new DBException("JDBC Error", ce);
        }
    }

    // ------------------------------------------------------------------------

    public static void main(String argv[])
    {
        DBConnection.getDBUsername();
        DBConnection.getDBPassword();
        DBConnection.getDBUri();
    }
    
}
