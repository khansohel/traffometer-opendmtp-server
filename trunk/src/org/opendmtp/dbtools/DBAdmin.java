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
//      Integrated 'DBException'
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.dbtools;

// import java.lang.*; // commented out by Kiet Huynh
// import java.util.*; // commented out by Kiet Huynh
// import java.io.*; // commented out by Kiet Huynh
// import java.sql.*; // commented out by Kiet Huynh

import java.io.File; // added by Kiet Huynh
import java.sql.SQLException; // added by Kiet Huynh
import java.util.Iterator; // added by Kiet Huynh
import org.opendmtp.util.DateTime; // added by Kiet Huynh
import org.opendmtp.util.MethodAction; // added by Kiet Huynh
import org.opendmtp.util.OrderedMap; // added by Kiet Huynh
import org.opendmtp.util.Print; // added by Kiet Huynh
import org.opendmtp.util.RTConfig; // added by Kiet Huynh
import org.opendmtp.util.RTKey; // added by Kiet Huynh

/**
 * Implementation of SQL database admistration.  Administration includes accessing to the 
 * database, creates and drop a database, a table in the database.
 * @author Martin D. Flynn
 * @author Kiet Huynh
 *
 */
public class DBAdmin {

  // ------------------------------------------------------------------------
  // Initalizing user/password/database:
  //   % su root
  //   % mysql
  //   Remove anonymous users:
  //     mysql> DELETE FROM mysql.user WHERE User='';
  //   Remove 'root' access from anywhere but 'localhost':
  //     mysql> DELETE FROM mysql.user WHERE User='root' AND Host!='localhost';
  //   Create 'dbtest' database:
  //     mysql> CREATE DATABASE dbtest;
  //   Grant priviledges:
  //     mysql> GRANT ALL ON dbtest.* TO userroot@localhost IDENTIFIED BY 'passwd' WITH GRANT OPTION;
  //     mysql> GRANT ALL ON dbtest.* TO userroot@"%" IDENTIFIED BY 'passwd' WITH GRANT OPTION;
  //   Flush priviledge changes:
  //     mysql> FLUSH PRIVILEGES;
  //
  // Create 'dbtest' database:
  //   % bin/DBAdmin -createdb
  // Create 'dbtest' tables:
  //   % bin/DBAdmin -tables
  //
  // Querying 'dbtest' tables:
  //   % mysql [-h <host>] -u userroot -p [dbtest]
  //   Enter Password: passwd
  //   mysql> [use dbtest]
  //
  // ------------------------------------------------------------------------

  /** An OrderedMap that stores SQL database tables. */
  private static OrderedMap tableFactoryMap = null;

  /**
   * Gets OrderedMap 'tableFactoryMap' that stores SQL database tables. If the
   * 'tableFactoryMap', an empty OrderedMap will be returned.
   * @return An empty OrderedMap that stores SQL database tables
   */
  private static OrderedMap getTableFactoryMap() {
    if (tableFactoryMap == null) {
      tableFactoryMap = new OrderedMap();
      //addTableFactory(TestTable.getFactory());
    }
    return tableFactoryMap;
  }

  /**
   * Adds a SQL database table to the OrderedMap 'tableFactoryMap'.
   * @param factory The table to be added. The table is an instance of the class 
   * DBFactory.
   */
  public static void addTableFactory(DBFactory factory) {
    String tableName = factory.getTableName().toLowerCase();
    DBAdmin.getTableFactoryMap().put(tableName, factory);
  }

  /**
   * Adds more than one table to the OrderedMap 'tableFactoryMap'. 
   * @param factory A DBFactory array that contains all the table will be added.
   */
  public static void addTableFactories(DBFactory factory[]) {
    if (factory != null) {
      for (int i = 0; i < factory.length; i++) {
        DBAdmin.addTableFactory(factory[i]);
      }
    }
  }

  // ------------------------------------------------------------------------

  /** The default table package. */
  private static String DefaultTablePackage = null;

  /**
   * Sets the default table package. A default table packet's name must end 
   * with a period ".". If the parameter is null or empty string, the default 
   * table package is set to null. If the parameter does not end with a period
   * ".", a period will be appended to the default table package.
   * @param dftTblPackage The name of the default package.
   */
  public static void setDefaultTablePackage(String dftTblPackage) {
    if ((dftTblPackage == null) || dftTblPackage.equals("")) {
      DefaultTablePackage = null;
    }
    else {
      DefaultTablePackage = dftTblPackage.trim();
      if (!DefaultTablePackage.endsWith(".")) {
        DefaultTablePackage += ".";
      }
    }
  }

  /**
   * Gets the default table package.  A default table packet's name must end 
   * with a period ".". If the default table package is null or empty string,
   * the name of the package of this class will be the default table package.
   * If the parameter does not end with a period
   * ".", a period will be appended to the default table package.
   * @return The name of the default table package.
   */
  public static String getDefaultTablePackage() {
    if ((DefaultTablePackage == null) || DefaultTablePackage.equals("")) {
      DefaultTablePackage = DBAdmin.class.getPackage().getName();
    }
    if (!DefaultTablePackage.endsWith(".")) {
      DefaultTablePackage += ".";
    }
    return DefaultTablePackage;
  }

  /**
   * Gets a specified table.
   * @param tableName The name of the table.
   * @return The table desired as an DBFactory object if the table exists.
   * Otherwise, returns null.
   */
  private static DBFactory _getTableFactory(String tableName) {
    if (tableName != null) {
      String tn = tableName.toLowerCase();
      return (DBFactory) DBAdmin.getTableFactoryMap().get(tn);
    }
    else {
      return null;
    }
  }

  /**
   * Gets a specified table. Table name is case-sensitive since we
   * are looking for a class name that matches the tableName.
   * @param tableName The name of the table.
   * @return The table desired as an DBFactory object if the table exists.
   * Otherwise, returns null.
   */
  public static DBFactory getTableFactory(String tableName) {
    DBFactory fact = DBAdmin._getTableFactory(tableName);
    if ((fact == null) && (tableName != null)) {
      try {
        // Note: At this point the tableName is case-sensitive, since we are
        // looking for a class name that matches the tableName.
        String tableClassName = DBAdmin.getDefaultTablePackage() + tableName;
        MethodAction action = new MethodAction(tableClassName, "getFactory");
        fact = (DBFactory) action.invoke();
        if (fact == null) {
          throw new DBException("Factory is null");
        }
      }
      catch (Throwable t) { // ClassNotFoundException, ...
        // Implementation error (this should never occur)
        Print.logStackTrace("Getting table factory", t);
      }
    }
    return fact;
  }

  // ------------------------------------------------------------------------

  /**
   * Validate a tables if it exists. If the table does not exists, the method creates the table
   * and returns true.  
   * @return True if validation passed. Otherwise, returns false.
   */
  private static boolean resetTables() {
    boolean validated = true;

    OrderedMap factMap = getTableFactoryMap();
    for (Iterator i = factMap.keys(); i.hasNext();) {
      String tn = (String) i.next();
      DBFactory f = (DBFactory) factMap.get(tn);
      try {
        if (!f.tableExists()) {
          Print.logInfo("  Creating table '" + f.getTableName() + "' ...");
          f.createTable();
        }
        else {
          Print.logInfo("  Validating table '" + f.getTableName() + "' ...");
          validated = f.validateColumns() && validated;
        }
      }
      catch (DBException dbe) {
        Print.logError("    ERROR: Unable to create/validate table '" + f.getTableName() + "'");
        dbe.printException();
      }
    }

    /* check validation */
    if (!validated) {
      Print.logError("Fix validation errors");
      return false;
    }

    /* success */
    return true;

  }

  /** 
   * Prints all the columns names of a table. Prints "[Unable to get columns]"
   * if any errors occur.
   * @param fact The table whose column will be printed. The table is an DBFactory 
   * object.
   */
  private static void printColumns(DBFactory fact) {
    Print.logInfo("DB columns for " + fact.getTableName());
    try {
      DBField dbf[] = fact.getTableColumns();
      for (int i = 0; i < dbf.length; i++) {
        Print.logInfo("  " + dbf[i].toString());
      }
    }
    catch (DBException dbe) {
      Print.logError("[Unable to get columns]");
      dbe.printException();
    }
  }

  // ------------------------------------------------------------------------

  /**
   * Validates a table.  
   * @param tableName The name of the table.
   * @param inclWarn If true, warnings, if there is one, will be added to the log file.
   */
  private static void validateTables(String tableName, boolean inclWarn) {
    // 'tableName' is non-null if the caller want to validate a specific table
    boolean tableFound = false;
    OrderedMap factMap = getTableFactoryMap();
    for (Iterator i = factMap.keys(); i.hasNext();) {
      String tn = (String) i.next();
      DBFactory f = (DBFactory) factMap.get(tn);
      try {
        if ((tableName == null) || tableName.equals("") || tn.equalsIgnoreCase(tableName)) {
          if (!f.tableExists()) {
            Print.logError("Table does not exist '" + f.getTableName() + "'");
          }
          else {
            f.validateTable(inclWarn);
            tableFound = true;
          }
        }
      }
      catch (DBException dbe) {
        Print.logError("Unable to validate table '" + f.getTableName() + "'");
      }
    }
    if (!tableFound) {
      Print.logError("Table not found '" + tableName + "'");
    }
    Print.logInfo("");
  }

  // ------------------------------------------------------------------------

  /**
   * Checks if tables in the 'tableFactoryMap' exist in the database.
   * @return True if all tables exists in the database. Otherwise, returns false.
   */
  public static boolean verifyTablesExist() {
    boolean allOK = true;
    OrderedMap factMap = getTableFactoryMap();
    for (Iterator i = factMap.keys(); i.hasNext();) {
      String tn = (String) i.next();
      DBFactory f = (DBFactory) factMap.get(tn);
      try {
        if (!f.tableExists()) {
          Print.logError("Missing table '" + f.getTableName() + "'");
          allOK = false;
        }
      }
      catch (DBException dbe) {
        Print.logError("Error checking for table '" + f.getTableName() + "'");
        dbe.printException();
        allOK = false;
      }
    }
    return allOK;
  }

  // ------------------------------------------------------------------------

  /** Default directory. */
  private static String DEFAULT_DIRECTORY = File.separator + "tmp";

  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

  /**
   * Executes commands to manipulate the database. These command will be executed in 
   * order:
   * <p> -createdb -rootUser=<Root_User> -rootPass=<Root_Pass> </p>
   * <p> -grant -rootUser=<Root_User> -rootPass=<Root_Pass> -user=<Grant_User> -pass=<Grant_Pass> </p>
   * <p> -tables </p>
   * <p> -dump=<table> -dir=<Destination_Dir> </p>
   * <p> -load=<table> -dir=<Source_Dir> </p>
   * <p> -drop=<table> </p>
   * @return True if all commands were executed successfully. Otherwise, returns false.
   */
  public static boolean execCommands() {
    RTConfig.setBoolean(RTKey.LOG_EMAIL_EXCEPTIONS, false);
    // -createdb -rootUser=<Root_User> -rootPass=<Root_Pass>
    // -grant -rootUser=<Root_User> -rootPass=<Root_Pass> -user=<Grant_User> -pass=<Grant_Pass>
    // -tables
    // -dump=<table> -dir=<Destination_Dir>
    // -load=<table> -dir=<Source_Dir>
    // -drop=<table>

    /* creatdb */
    // bin/exe DBAdmin -createdb
    //    -rootUser=<Root_User>
    //    -rootPass=<Root_Pass>
    //    [-db.sql.name=<DataBase_Name>]
    if (RTConfig.getBoolean("createdb", false)) {
      String rootUser = RTConfig.getString("rootUser", null);
      String rootPass = RTConfig.getString("rootPass", null);
      String dbName = DBConnection.getDBName();
      if ((rootUser == null) || rootUser.equals("")) {
        Print.logError("Root user not specified");
        return false;
      }
      else if ((dbName == null) || dbName.equals("")) {
        Print.logError("No database name specified");
        return false;
      }
      String dbUri = DBConnection.getDBUri(null, -1, null);
      try {
        DBConnection dbc = DBConnection.getConnection(dbUri, rootUser, rootPass);
        // MySQL: create database <DataBase>;
        dbc.executeUpdate("CREATE DATABASE " + dbName + ";");
        Print.logInfo("Database created: " + dbName);
      }
      catch (SQLException sqe) {
        int errCode = sqe.getErrorCode();
        if (errCode == DBFactory.SQLERR_DATABASE_EXISTS) {
          Print.logWarn("Database already exists: " + dbName);
        }
        else {
          Print.logSQLError("DB create error [" + dbUri + "]", sqe);
          return false;
        }
      }
      catch (DBException dbe) {
        Print.logException("DB create error [" + dbUri + "]", dbe);
      }
      // To drop a database: (not implemented here)
      //    "drop database <DataBase_Name>;"
    }

    /* grant */
    // bin/exe DBAdmin -grant
    //    -rootUser=<Root_User>
    //    -rootPass=<Root_Pass>
    //    [-db.sql.user=<Grant_User>]
    //    [-db.sql.pass=<Grant_Pass>]
    //    [-db.sql.name=<DataBase_Name>]
    if (RTConfig.getBoolean("grant", false)) {
      // This will execute the following grants:
      //    "GRANT ALL ON <DataBase_Name>.* TO <Grant_User>@localhost IDENTIFIED BY '<Grant_Pass>' WITH GRANT OPTION;"
      //    "GRANT ALL ON <DataBase_Name>.* TO <Grant_User>@"%" IDENTIFIED BY '<Grant_Pass>' WITH GRANT OPTION;"
      //    "FLUSH PRIVILEGES;"
      String rootUser = RTConfig.getString("rootUser", null);
      String rootPass = RTConfig.getString("rootPass", null);
      String grantUser = DBConnection.getDBUsername();
      String grantPass = DBConnection.getDBPassword();
      String dbName = DBConnection.getDBName();
      if ((rootUser == null) || rootUser.equals("")) {
        Print.logError("Root user not specified");
        return false;
      }
      else if ((grantUser == null) || grantUser.equals("")) {
        Print.logError("User not specified for GRANT access");
        return false;
      }
      else if (grantUser.equalsIgnoreCase("root")) {
        Print.logError("Refusing to change privileges for 'root'");
        return false;
      }
      else if ((dbName == null) || dbName.equals("")) {
        Print.logError("No database name specified");
        return false;
      }
      String dbUri = DBConnection.getDBUri(null, -1, null);
      try {
        DBConnection dbc = DBConnection.getConnection(dbUri, rootUser, rootPass);
        String grantS = "GRANT ALL ON " + dbName + ".* TO " + grantUser + "@";
        String grantE = " IDENTIFIED BY '" + grantPass + "' WITH GRANT OPTION;";
        // MySQL: grant all on <DataBase>.* to <User>@localhost identified by '<Password>' with grant option;
        dbc.executeUpdate(grantS + "localhost" + grantE);
        // MySQL: grant all on <DataBase>.* to <User>@"%" identified by '<Password>' with grant option;
        dbc.executeUpdate(grantS + "\"%\"" + grantE);
        // MySQL: flush privileges;
        dbc.executeUpdate("FLUSH PRIVILEGES;");
        Print.logInfo("Privileges granted to user: " + grantUser);
      }
      catch (SQLException sqe) {
        Print.logSQLError("DB grant error [" + dbUri + "]", sqe);
        return false;
      }
      catch (DBException dbe) {
        Print.logException("DB grant error [" + dbUri + "]", dbe);
      }
    }

    // -----------------------------------
    // Options beyond this point
    //   -load
    //   -drop
    //   -tables
    //   -dump
    //   -validate
    //   -last
    //   -bean
    //   -hibxml

    /* connect to db */
    try {
      DBConnection dbc = DBConnection.getDefaultConnection();
      dbc.getConnection();
    }
    catch (SQLException sqe) {
      String uri = DBConnection.getDBUri();
      Print.logSQLError("Connection error [" + uri + "]", sqe);
      return false;
    }

    /* input/output directory */
    File dir = RTConfig.getFile("dir", new File(DEFAULT_DIRECTORY));
    //Print.logInfo("Output dir: " + dir);

    /* pre-check 'load' file */
    // bin/exe DBAdmin -load=<Table>
    //    -dir=<Source_Dir>
    File loadFile = null;
    String loadTable = RTConfig.getString("load", null);
    if (loadTable != null) {
      loadFile = new File(dir, loadTable + ".dump");
      if (!loadFile.isFile()) {
        loadFile = new File(dir, loadTable + ".sql");
      }
      if (!loadFile.isFile()) {
        String f = dir + File.separator + loadTable + ".[dump|sql]";
        Print.logError("'Load' file not found: " + f);
        return false;
      }
    }

    /* drop: drop tables */
    // bin/exe DBAdmin -drop=<Table>
    if (RTConfig.hasProperty("drop")) {
      String dropTbl = RTConfig.getString("drop", null);
      if ((dropTbl != null) && !dropTbl.equals("")) {
        Print.logInfo("Deleting table: " + dropTbl);
        DBFactory dbf = DBAdmin.getTableFactory(dropTbl);
        if (dbf != null) {
          try {
            dbf.dropTable();
            Print.logInfo("Table dropped: " + dropTbl);
          }
          catch (DBException dbe) {
            Print.logError("Unable to drop table");
            dbe.printException();
          }
        }
        else {
          Print.logError("Unable to determine table factory");
        }
      }
      else {
        Print.logError("Missing table name");
      }
    }

    /* tables: clear/create tables */
    // bin/exe DBAdmin -tables
    if (RTConfig.hasProperty("tables")) {
      //String tables = RTConfig.getString("tables", null);
      Print.logInfo("Creating/Validating tables ...");
      if (!DBAdmin.resetTables()) {
        return false;
      }
    }

    /* dump: dump table to flatfile */
    // bin/exe DBAdmin -dump=<Table>
    //    -dir=<Destination_Dir>
    String dumpTable = RTConfig.getString("dump", null);
    if (dumpTable != null) {
      File dumpFile = new File(dir, dumpTable + ".dump");
      Print.logInfo("Dumping '" + dumpTable + "' to file: " + dumpFile);
      DBFactory fact = DBAdmin.getTableFactory(dumpTable);
      if (fact != null) {
        try {
          fact.dumpTable(dumpFile);
        }
        catch (DBException dbe) {
          Print.logError("Error dumping table: " + dumpTable);
          System.exit(99);
        }
      }
    }

    /* load: load table data from flatfile */
    // bin/exe DBAdmin -load=<Table>
    //    -dir=<Source_Dir>
    if (loadFile != null) {
      if (loadFile.isFile()) {
        DBFactory fact = DBAdmin.getTableFactory(loadTable);
        if (fact != null) {
          try {
            if (!fact.tableExists()) {
              Print.logInfo("Creating table '" + loadTable + "'");
              fact.createTable();
            }
            Print.logInfo("Loading '" + loadTable + "' from file: " + loadFile);
            fact.loadTable(loadFile);
          }
          catch (DBException dbe) {
            Print.logError("ERROR: Unable to create/load table '" + loadTable + "'");
            dbe.printException();
          }
        }
        else {
          Print.logError("No DBFactory for table '" + loadTable + "'");
        }
      }
      else {
        Print.logError("File not found: " + dir + File.separator + loadTable + ".[dump|sql]");
      }
    }

    /* validate: validate tables */
    // bin/exe DBAdmin -validate=[/]<Table>
    if (RTConfig.hasProperty("validate")) {
      String validate = RTConfig.getString("validate", null);
      if (validate != null) {
        boolean inclWarn = true;
        if (validate.startsWith("/")) {
          inclWarn = false;
          validate = validate.substring(1);
        }
        Print.logInfo("Validating tables");
        validateTables(validate, inclWarn);
      }
    }

    /* last: show lst update time */
    // bin/exe DBAdmin -last
    if (RTConfig.hasProperty("last")) {
      //String last = RTConfig.getString("last", null);
      Print.logInfo("Table last update time:");
      OrderedMap factMap = getTableFactoryMap();
      for (Iterator i = factMap.keys(); i.hasNext();) {
        String tn = (String) i.next();
        DBFactory f = (DBFactory) factMap.get(tn);
        try {
          long lut = DBRecord.getLastUpdateTime(f);
          if (lut < 0L) {
            Print.logInfo("  Last Table Update: " + tn + " - Not Available");
          }
          else if (lut == 0L) {
            Print.logInfo("  Last Table Update: " + tn + " - No Data");
          }
          else {
            Print.logInfo("  Last Table Update: " + tn + " - " + (new DateTime(lut)));
            //Print.logInfo("   => " + DBRecord.getRecordsSince(f, lut)[0]);
          }
        }
        catch (DBException dbe) {
          Print.logError("  Last Table Update: " + tn + " - DB Error [" + dbe + "]");
        }
      }
    }

    /* bean: validate bean access methods for specified table */
    // bin/exe DBAdmin -bean=table
    if (RTConfig.hasProperty("bean")) {
      String table = RTConfig.getString("bean", null);
      if (table != null) {
        OrderedMap factMap = getTableFactoryMap();
        DBFactory f = DBAdmin._getTableFactory(table);
        if (f != null) {
          f.validateTableBeanMethods();
        }
        else {
          Print.logError("Table not found: " + table);
        }
      }
    }

    /* hibxml: print Hibernate XML for specified table */
    // bin/exe DBAdmin -hibxml=table
    // [This option is currently experimental]
    if (RTConfig.hasProperty("hibxml")) {
      String table = RTConfig.getString("hibxml", null);
      if (table != null) {
        OrderedMap factMap = getTableFactoryMap();
        DBFactory f = DBAdmin._getTableFactory(table);
        if (f != null) {
          f.createHibernateXML();
        }
        else {
          Print.logError("Table not found: " + table);
        }
      }
    }

    return true;

  }

  /**
   * Calls the execCommands method. Exits the program if the execCommands method
   * returns false.
   * @param argv Command line arguments.
   */
  public static void main(String argv[]) {
    RTConfig.setCommandLineArgs(argv);
    if (!DBAdmin.execCommands()) {
      System.exit(1);
    }
  }

  // ------------------------------------------------------------------------

}
