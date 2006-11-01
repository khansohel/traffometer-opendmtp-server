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

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import org.opendmtp.util.DateTime;
import org.opendmtp.util.ListTools;
import org.opendmtp.util.MethodAction;
import org.opendmtp.util.Print;
import org.opendmtp.util.RTConfig;
import org.opendmtp.util.RTKey;
import org.opendmtp.util.StringTools;

//import javax.servlet.*;
//import javax.servlet.http.*;

/**
 * Defines a database record
 * value along with all necessary methods to keep track of
 * any changes to the record.  This includes metadata,
 * locking methods to ensure integrity of the data.
 * 
 * @author Martin D. Flynn, Joshua Stupplebeen
 */
public abstract class DBRecord
{

    // ------------------------------------------------------------------------
    // See DBFactory.java for list of MySQL error codes

    // ------------------------------------------------------------------------

    /**
     * Constant string value.
     */
    public  static final String FLD_count_  = "count(*)";
    
    // ------------------------------------------------------------------------

    /**
     * Integer constant Key value.
     */
    public static final int KEY_PRIMARY     = DBField.KEY_PRIMARY;
    
    /**
     * Integer constant unique Key value.
     */
    public static final int KEY_UNIQUE      = DBField.KEY_UNIQUE;
    
    /**
     * Integer constant Key index value.
     */
    public static final int KEY_INDEX       = DBField.KEY_INDEX;

    /**
     * Integer constant group value.
     */
    public static final int NOTIFY_GROUP    = 1;

    // ------------------------------------------------------------------------

    /**
     * String constant indicating the last update time.
     */
    public static final String FLD_lastUpdateTime   = "lastUpdateTime";
    
    /**
     * String constant indicating the last user who updated.
     */
    public static final String FLD_lastUpdateUser   = "lastUpdateUser";

    // ------------------------------------------------------------------------

    private   DBRecordKey   recordKey           = null;
    
    private   DBFieldValues fieldVals           = null;
    private   boolean       changed             = false;
    private   Vector        changeNotification  = null;
    
    /**
     * Boolean constant indicating if the Record is currently validating.
     */
    protected boolean       isValidating        = false;
    
    /**
     * SQLException indicating what the last error caught was.
     */
    protected SQLException  lastSQLException    = null;
    
    /**
     * Empty DBRecord constructor taking no method input values, and only
     * returning an instance of the superclass.
     */
    public DBRecord()
    {
        super();
    }

    /**
     * DBRecord constructor taking an instance of the
     * DBRecordKey class as a parameter.  Instantiates
     * the current record to the record passed.
     * 
     * @param key instance of the DBRecordKey class.
     */
    protected DBRecord(DBRecordKey key)
    {
        this();
        this.recordKey = key;
    }

    // ------------------------------------------------------------------------
    
    /**
     * Getter method to retrieve the private recordKey value.
     * If a record key does not exist, it creates an instance of
     * the table factory and calls the createKey method.
     * The createKey method may throw a DBException.
     * 
     * @return DBRecordKey
     */
    public DBRecordKey getRecordKey()
    {
        if (this.recordKey == null) {
            // we don't have a record key, so we need to get the table factory to
            // create a record key for us.
            try {
                DBFactory fact = this._getFactory();
                if (fact != null) {
                    this.recordKey = fact.createKey(); // may throw DBException
                } else {
                    throw new DBException("No Table Factory!");
                }
            } catch (DBException dbe) {
                // This should never occur, if it does, it's a programming error
                dbe.printException();
                return null;
            }
        }
        return this.recordKey;
    }

    // ------------------------------------------------------------------------

    /**
     * Retrieves an instance of the DBFactory class taking
     * a DBRecord as a parameter.  If the input value is
     * not null getFactory calls the overloaded getFactory
     * class with no input values.  If the input value is
     * null getFactory returns null.
     * 
     * @param dbr DBRecord to retrieve DBFactory.
     * @return returns an instance of the DBFactory class,
     * or null.
     */
    public static DBFactory getFactory(DBRecord dbr)
    {
        return (dbr != null)? dbr._getFactory() : null;
    }
    
    /**
     * Overloaded getFactory method which takes no input
     * parameters.  Creates a new thread to invoke the
     * getFactory method.
     * Returns an instance of the DBFactory class.
     * @return returns an instance of the DBFactory class.
     */
    protected DBFactory _getFactory()
    {
        if (this.recordKey != null) {
            return this.recordKey.getFactory();
        } else {
            try {
                MethodAction methGetFactory = new MethodAction(this.getClass(), "getFactory");
                DBFactory fact = (DBFactory)methGetFactory.invoke();
                return fact;
            } catch (Throwable t) { // MethodNotFOundException, ...
                Print.logException("Get table factory [via reflection]", t);
                return null;
            }
        }
    }

    // ------------------------------------------------------------------------
    // Example:
    //  Statement stmt = Account.getStatement(Owner.getFactory(), "WHERE (accountID LIKE '%smith%')");
    //  ResultSet rs = stmt.getResultSet();
    //  while (true) {
    //     Account list[] = (Account[])Account.getNextGroup(Account.getFactory(), rs, 10);
    //     if (list.length == 0) { break; }
    //     // do something with 'list'
    //  }
    //  rs.close();
    //  stmt.close();

    /**
     * Creates a dynamic list of DBRecords using the input parameters;
     * fact to create the Record instances and rs to populate the Record
     * data fields.  The maximum size of the array is the integer max.
     * @param fact DBFactory
     * @param rs ResultSet
     * @param max Max size of the array.
     * @return returns an array of DBRecords
     * @throws MethodNotFoundException
     */
    public static DBRecord[] getNextGroup(DBFactory fact, ResultSet rs, int max)
        throws DBException
    {
        Vector rcdList = new Vector();

        /* get result set */
        try {
            int cnt = 0;                                   
            while (((max < 0) || (cnt++ < max)) && rs.next()) {
                DBRecordKey rcdKey = fact.createKey(rs); // may throw DBException
                if (rcdKey != null) {
                    DBRecord rcd = rcdKey.getDBRecord();
                    rcd.setFieldValues(rs);
                    rcdList.add(rcd);
                } else {
                    Print.logError("Unable to create key: " + fact.getTableName());
                }
            }
        } catch (SQLException sqe) {
            //this.setLastCaughtSQLException(sqe);
            throw new DBException("Read next record group", sqe);
        }

        /* convert to array */
        try {
            Class rcdClass = fact.getRecordClass();
            DBRecord ra[] = (DBRecord[])java.lang.reflect.Array.newInstance(rcdClass, rcdList.size());
            return (DBRecord[])rcdList.toArray(ra);
        } catch (Throwable t) { // MethodNotFoundException, ...
            throw new DBException("Array conversion", t);
        }
        
    }

    // ------------------------------------------------------------------------

    /**
     * Selects an array of DBRecords by calling the DBRecord select
     * method, passing the parameters:  (DBFactory,String,null).
     * @param fact DBFactory.
     * @param where Table name.
     * @return returns an array of DBRecords.
     */
    protected static DBRecord[] select(DBFactory fact, String where)
        throws DBException
    {
        return DBRecord.select(fact, where, null);
    }
    
    /**
     * Selects an array of DBRecords, modeling the common SQL statement
     * SELECT, FROM, WHERE.  
     * @param fact DBFactory.
     * @param where Table name.
     * @param addtnlSel String.
     * @return returns an array of DBRecords
     * @throws DBException Throws a DBException if a database error occurs.
     */
    protected static DBRecord[] select(DBFactory fact, String where, String addtnlSel)
        throws DBException
    {
        // MySQL: SELECT * FROM <TableName> <SQLWhere> <AndSQLWhere>

        /* select statement */
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM ").append(fact.getTableName());
        sb.append(" ").append(where);
        if (addtnlSel != null) {
            sb.append(" ").append(addtnlSel);
        }

        /* get result set */
        Statement stmt = null;
        ResultSet rs = null;
        Vector rcdList = new Vector();

        try {
            stmt = DBConnection.getDefaultConnection().execute(sb.toString());
            rs = stmt.getResultSet();
            /* extract records from result set */
            while (rs.next()) {
                DBRecordKey rcdKey = fact.createKey(rs); // may throw DBException
                if (rcdKey != null) {
                    DBRecord rcd = rcdKey.getDBRecord();
                    rcd.setFieldValues(rs);
                    rcdList.add(rcd);
                }
            }
        } catch (SQLException sqe) {
            //this.setLastCaughtSQLException(sqe);
            throw new DBException("Record Selection", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
        }

        /* convert to array */
        try {
            Class rcdClass = fact.getRecordClass();
            DBRecord ra[] = (DBRecord[])java.lang.reflect.Array.newInstance(rcdClass, rcdList.size());
            return (DBRecord[])rcdList.toArray(ra);
        } catch (Throwable t) { // MethodNotFoundException, ...
            // Implementation error (should never occur)
            throw new DBException("Array conversion", t);
        }

    }

    // ------------------------------------------------------------------------

    /**
     * Overloaded getRecordCount method which takes in a StringBuffer
     * as a parameter and converts it to a string, in order to call the
     * getRecordCount method.
     * @return long RecordCount
     */
    protected static long getRecordCount(DBFactory fact, StringBuffer where)
        throws SQLException, DBException
    {
        return DBRecord.getRecordCount(fact, where.toString());
    }
    
    protected static long getRecordCount(DBFactory fact, String where)
        throws SQLException, DBException
    {
        // MySQL: SELECT count(*) FROM <TableName> <SQLWhere>

        /* select statement */
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ").append(FLD_count_).append(" FROM ").append(fact.getTableName()).append(" ");
        sb.append(where);

        /* get result set */
        Statement stmt = null;
        ResultSet rs = null;
        long count = 0L;

        try {
            stmt = DBConnection.getDefaultConnection().execute(sb.toString());
            rs = stmt.getResultSet();
            if (rs.next()) {
                count = rs.getLong(FLD_count_);
            }
        } finally {
            if (rs != null) { rs.close(); }
            if (stmt != null) { stmt.close(); }
        }

        /* return count */
        return count;

    }

    // ------------------------------------------------------------------------
    
    /**
     * Returns the boolean value indicating whether the current
     * record has been changed since the last iteration.
     * @return boolean Determines if the current record has changed.
     */
    public boolean hasChanged()
    {
        return this.changed;
    }

    /**
     * Sets the changed flag to true and calls the fireChangeNotification
     * method which notifies that the field has been changed.
     * Takes the fieldName that has changed as a parameter.
     * @param fieldName
     */
    public void setChanged(String fieldName)
    {
        this.changed = true;
        this.fireChangeNotification(fieldName);
    }

    /**
     * Sets the changed flag to true for the fieldName passed as a parameter.
     * 
     * @param fieldName Field name.
     * @param oldVal Old field value.
     * @param newVal New field value.
     */
    public void setChanged(String fieldName, Object oldVal, Object newVal)
    {
        if (oldVal == newVal) {
            // ignore
        } else
        if ((oldVal == null) || (newVal == null)) {
            this.setChanged(fieldName);
        } else
        if (!oldVal.equals(newVal)) {
            this.setChanged(fieldName);
        }  else {
            Print.logDebug("Field did not change: " + fieldName);
        }
    }

    /**
     * Sets the changed flag to false.
     */
    public void clearChanged()
    {
        this.changed = false;
    }

    /**
     * Adds the c1 DBChangeListener parameter to the
     * Vector of changeNotifications.
     * @param cl DBChangeListener
     */
    public void addChangedNotification(DBChangeListener cl)
    {
        if (this.changeNotification == null) { this.changeNotification = new Vector(); }
        if ((cl != null) && !this.changeNotification.contains(cl)) {
            this.changeNotification.add(cl);
        }
    }

    /**
     * Removes the c1 DBChangeListener from the Vector of
     * changeNotifications.  Does not check to identify if
     * the List contains c1 before calling the removal method.
     * @param cl DBChangeListener.
     */
    public void removeChangedNotification(DBChangeListener cl)
    {
        if (this.changeNotification != null) {
            this.changeNotification.remove(cl);
        }
    }

    /**
     * Iterates through the changedNotifications Vector firing a
     * change notification for the fieldName passed.
     * @param fieldName Field name.
     */
    public void fireChangeNotification(String fieldName)
    {
        if (this.changeNotification != null) {
            for (Iterator i = this.changeNotification.iterator(); i.hasNext();) {
                DBChangeListener dbcr = (DBChangeListener)i.next();
                dbcr.fieldChanged(this, fieldName);
            }
        }
    }

    /**
     * Interface implementing the public fieldChanged method.
     */
    public static interface DBChangeListener
    {
        public void fieldChanged(DBRecord rcd, String fieldName);
    }

    // ------------------------------------------------------------------------

    /**
     * Calls the _reload method to reload the record data from the database.
     * Returns the reloaded DBRecord.
     * Returns null if the key is not found or if a database error occurs.
     * @return DBRecord The reloaded record data.
     */
    public DBRecord reload()
    {
        // returns 'null' if key does not exist, or if a DB error occurred
        try {
            return this._reload();
        } catch (DBException dbe) {
            dbe.printException();
            return null;
        }
    }
    
    /**
     * Automates the SQL code generation to reload the record data from the
     * database.  Generates the SELECT->FROM->WHERE, SQL code to reload the
     * record data.  Throws a DBException if the key is not found or if
     * a database error occurs.  Returns the reloaded DBRecord.
     * @return DBRecord The reloaded record data.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    protected DBRecord _reload()
        throws DBException
    {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            DBRecordKey recKey = this.getRecordKey();
            StringBuffer sb = new StringBuffer();
            // MySQL: SELECT * FROM <TableName> <KeyWhere>
            sb.append("SELECT * FROM ").append(recKey.getTableName()).append(" ");
            sb.append(recKey.getWhereClause());
            stmt = this.execute(sb.toString());
            rs = stmt.getResultSet();
            if (rs.next()) {
                this.setFieldValues(rs);
                this.clearChanged();
                return this;
            } else {
                // not a fatal error
                Print.logWarn("Key not found: [" + recKey.getTableName() + "] " + recKey.getWhereClause());
                return null;
            }
        } catch (SQLException sqe) {
            this.setLastCaughtSQLException(sqe);
            throw new DBException("Reload", sqe);
        } finally {
            if (rs   != null) { try{ rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try{ stmt.close(); } catch (Throwable t) {} }
        }
    }

    // ------------------------------------------------------------------------

    /**
     * Returns a long representing the last timestamp at which the record 
     * was updated.
     * Generates the SQL code "ORDER BY" to order the records contained
     * in the table by the latest timestamp.
     * @param factory DBFactory to retrieve update time.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public static long getLastUpdateTime(DBFactory factory)
        throws DBException
    {
        
        /* invalid factory? */
        if (factory == null) {
            Print.logStackTrace("NULL DBFactory specified");
            return -1L;
        }
        
        /* check for field "lastUpdateTime" in this factory */
        String fldUpdTime = FLD_lastUpdateTime;
        if (factory.getField(fldUpdTime) == null) {
            //Print.logWarn("Table doesn't contain field '" + fldUpdTime + "': " + factory.getTableName());
            return -1L;
        }
        
        /* order by lastUpdateTime descending */
        StringBuffer wh = new StringBuffer();
        wh.append("ORDER BY ").append(fldUpdTime).append(" DESC LIMIT 1");
        
        /* read last record */
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = DBRecord.getStatement(factory, wh.toString());
            rs = stmt.getResultSet();
            if (rs.next()) {
                return rs.getLong(fldUpdTime);
            }
        } catch (SQLException sqe) {
            //this.setLastCaughtSQLException(sqe);
            Print.logSQLError("Unable to get '" + fldUpdTime + "': " + factory.getTableName(), sqe);
            return -1L;
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
        }
        
        return 0L; // no records

    }

    // ------------------------------------------------------------------------

    /**
     * Returns the field which was last updated.
     * @return DBField Field that was last updated.
     */
    public static DBField newField_lastUpdateTime()
    {
        return new DBField(FLD_lastUpdateTime, Long.TYPE, DBField.TYPE_UINT32, "title=Last_Update_Time");
    }

    /**
     * Returns a long representing the last time the field was updated.
     * @return long The last update time.
     */
    public long getLastUpdateTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastUpdateTime);
        return (v != null)? v.longValue() : -1L;
    }

    /**
     * Sets the current Field's lastUpdateTime to the
     * long passed as a parameter.
     * @param time Current time.
     */
    protected void setLastUpdateTime(long time)
    {
        long t = (time >= 0L)? time : 0L;
        this.setFieldValue(FLD_lastUpdateTime, t);
    }

    // ------------------------------------------------------------------------

    /**
     * Returns a copy of the last DBField to be updated by a user.
     * @return DBField Updated DBField parameters.
     */
    public static DBField newField_lastUpdateUser()
    {
        return new DBField(FLD_lastUpdateUser, String.class, DBField.TYPE_STRING(32), "title=Last_Update_User");
    }

    /**
     * Returns the last user who updated the current field.
     * If the field has not been updated then the method returns null.
     * @return String Last user to update.
     */
    public String getLastUpdateUser()
    {
        String v = (String)this.getFieldValue(FLD_lastUpdateUser);
        return (v != null)? v : null;
    }

    /**
     * Sets the last user to update the field to the String passed
     * as a parameter.
     * @param user Last user to update.
     */
    public void setLastUpdateUser(String user)
    {
        String u = (user != null)? user : "";
        this.setFieldValue(FLD_lastUpdateUser, u);
    }
    
    // ------------------------------------------------------------------------

    /**
     * Attempts to insert the current Field into the Database table
     * by calling the _insert method.
     * 
     * @throws DBException Throws a DBException if there is a duplicate
     * key present or if a database error occurs.
     */
    public void insert() throws DBException
    {
        try {
            this._insert();
        } catch (SQLException sqe) {
            this.setLastCaughtSQLException(sqe);
            DBRecordKey dbKey = this.getRecordKey();
            if (this.isLastCaughtSQLExceptionErrorCode(DBFactory.SQLERR_DUPLICATE_KEY)) {
                //throw new DBException("Duplicate Key '" + dbKey + "'", sqe);
                Print.logInfo("Duplicate Key Skipped: " + dbKey);
            } else {
                throw new DBException("Unable to insert record '" + dbKey + "'", sqe);
            }
        }
    }
    
    /**
     * Generates SQL code to insert the current Field into the
     * database table.
     * 
     * @throws SQLException Throws an SQLException if an sql error occurs.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    protected void _insert()
        throws SQLException, DBException
    {
        DBRecordKey recKey = this.getRecordKey();
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO ").append(recKey.getTableName());
        
        /* last update time */
        this.setLastUpdateTime(DateTime.getCurrentTimeSec());
        this.setLastUpdateUser(DBRecord.GetCurrentUser());

        /* set */
        sb.append(" SET ");
        DBField field[] = recKey.getFields();
        DBFieldValues fieldValues = recKey.getFieldValues();
        boolean addedField = false;
        for (int i = 0; i < field.length; i++) {
            String fldName = field[i].getName();
            if (addedField) { sb.append(", "); }
            String fldVal = fieldValues.toString(fldName);
            String dbVal  = field[i].getQValue(fldVal);
            sb.append(fldName).append("=").append(dbVal);
            addedField = true;
        }
        
        /* execute */
        this.executeUpdate(sb.toString());
        this.clearChanged();

    }

    // ------------------------------------------------------------------------

    /**
     * Calls the overridden update(String updFlds[]) method
     * passing a null parameter.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public void update()
        throws DBException
    {
        this.update(null);
    }

    /**
     * Attempts to update the current Field by calling the _update method.
     * 
     * @param updFlds Array of fields to update.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public void update(String updFlds[])
        throws DBException
    {
        DBRecordKey dbKey = this.getRecordKey();
        try {
            this._update(updFlds);
        } catch (SQLException sqe) {
            this.setLastCaughtSQLException(sqe);
            throw new DBException("Update record '" + dbKey + "'", sqe);
        }
    }
    
    /**
     * Attempts to update the current Field values to the current
     * user and the current timestamp.
     * @param updFlds Array of fields to update.
     * @throws SQLException Throws an SQLException if an sql error occurs.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    protected void _update(String updFlds[])
        throws SQLException, DBException
    {
        java.util.List fldList = (updFlds != null)? ListTools.toList(updFlds) : null;
        DBRecordKey recKey = this.getRecordKey();
        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE ").append(recKey.getTableName());
        
        /* last update time */
        this.setLastUpdateTime(DateTime.getCurrentTimeSec());
        this.setLastUpdateUser(DBRecord.GetCurrentUser());
        
        /* set */
        sb.append(" SET ");
        DBFieldValues fieldValues = recKey.getFieldValues();
        DBField field[] = recKey.getFields();
        boolean addedField = false;
        for (int i = 0; i < field.length; i++) {
            String fldName = field[i].getName();
            if (!field[i].isPriKey() && ((fldList == null) || fldList.contains(fldName))) {
                if (addedField) { sb.append(", "); }
                String fldVal = fieldValues.toString(fldName);
                String dbVal  = field[i].getQValue(fldVal);
                sb.append(fldName).append("=").append(dbVal);
                addedField = true;
            }
        }
        
        /* where */
        sb.append(recKey.getWhereClause());
        
        /* execute */
        if (addedField) {
            this.executeUpdate(sb.toString());
        } else {
            Print.logInfo("Nothing was updated!!!!!!");
        }
        this.clearChanged();
        
    }
    
    // ------------------------------------------------------------------------

    /**
     * Saves the current key and Field configuration by calling the update function
     * if the field exists in the database table, or inserts the field into the
     * table if it does not.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public void save()
        throws DBException
    {
        DBRecordKey dbKey = this.getRecordKey();
        if (dbKey.exists()) {   // may throw DBException
            this.update();      // may throw DBException
        } else {
            this.insert();      // may throw DBException
        }
    }
    
    // ------------------------------------------------------------------------

    /**
     * Updates the fieldName with the passed String fieldName
     * and the integer value.
     * 
     * @param fldName fieldName
     * @param value int value to update field.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public void updateField(String fldName, int value)
        throws DBException
    {
        this._updateField(fldName, String.valueOf(value));
    }

    /**
     * Updates the fieldName with the passed String fieldName
     * and long value.
     * 
     * @param fldName Field name.
     * @param value Long value to update field.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public void updateField(String fldName, long value)
        throws DBException
    {
        this._updateField(fldName, String.valueOf(value));
    }

    /**
     * Updates the fieldName with the passed String fldName
     * and String value.
     * 
     * @param fldName Field name.
     * @param value String value to update field.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public void updateField(String fldName, String value)
        throws DBException
    {
        this._updateField(fldName, StringTools.quoteString(value));
    }
    
    /**
     * Updates the fieldName with the passed String fldname
     * and String value.
     * 
     * @param fldName Field name.
     * @param value String value to update field.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    protected void _updateField(String fldName, String value)
        throws DBException
    {
        DBRecordKey recKey = this.getRecordKey();
        StringBuffer sb = new StringBuffer();
        try {
            // MySQL: 
            String tableName = recKey.getTableName();
            sb.append("UPDATE ").append(tableName).append(" SET ");
            sb.append(fldName).append("=").append(value);
            sb.append(recKey.getWhereClause());
            this.executeUpdate(sb.toString());
        } catch (SQLException sqe) {
            this.setLastCaughtSQLException(sqe);
            Print.logError(sb.toString()); // print SQL statement
            String msg = "Error updating field: '" + fldName + "' " + recKey;
            Print.logSQLError(msg, sqe);
            throw new DBException(msg, sqe);
        }
    }
    
    // ------------------------------------------------------------------------

    private static Boolean  lockingEnabled = null;
    private static Stack    lockLevel      = new Stack();
    private static int      lockSeq        = 0;
    private static int      unlockSeq      = 0;

    /**
     * Returns a boolean value indicating if the tables are able
     * to be locked.
     * @return boolean Determines if table locking is enabled.
     */
    public static boolean isTableLockingEnabled()
    {
        if (DBConnection.ALWAYS_NEW_CONNECTION) {
            // If DBConnection.ALWAYS_NEW_CONNECTION is 'true', then
            // return 'false'. Otherwise table deadlocks _will_ occur.
            return false;
        } else {
            if (lockingEnabled == null) { 
                lockingEnabled = new Boolean(RTConfig.getBoolean(RTKey.DB_TABLE_LOCKING));
            }
            return lockingEnabled.booleanValue();
        }
    }

    /**
     * Returns an int indicating the locklevel.
     * @return int Locklevel.
     */
    public static int getLockLevel()
    {
        return lockLevel.size();
    }

    /**
     * Returns a boolean value indicating if the Record is locked for writing.
     * @return boolean Determines if the table has been write locked.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public boolean lockWrite()
        throws DBException
    {
        return this.lock(new String[] { this._getFactory().getTableName() }, null);
    }

    /**
     * Returns a boolean value indicating if the Record is locked for reading.
     * @return boolean Determines if the table has been read locked.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public boolean lockRead()
        throws DBException
    {
        // write locks are always included
        return this.lock(null, new String[] { this._getFactory().getTableName() });
    }

    /**
     * Attempts to lock the database table from both reading and writing.
     * 
     * @param writeTables Which tables to write lock.
     * @param readTables Which tables to read lock.
     * @return boolean Determines if the tables have been locked.
     * @throws DBException Throws a DBException if there is a database error.
     */
    public boolean lock(String writeTables[], String readTables[])
        throws DBException
    {
        if (writeTables == null) {
            writeTables = new String[] { this._getFactory().getTableName() };
        }
        return DBRecord.lockTables(writeTables, readTables);
    }

    /**
     * Returns a boolean indicating if the locking of the database table
     * was successful.
     * @param writeTables Which tables to write lock.
     * @param readTables Which tables to read lock.
     * @return boolean Determines if the tables have been locked.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public static boolean lockTables(String writeTables[], String readTables[])
        throws DBException
    {
        ++lockSeq;
        //Print.logDebug("Lock Sequence: " + lockSeq);
        
        /* nothing to lock */
        if ((writeTables == null) && (readTables == null)) { 
            return false; 
        }
        
        /* check recursive locking */
        if (!DBRecord.lockLevel.empty()) {
            // You amy get this message when this section is acessed by multiple threads
            Print.logStackTrace("Locking: Lock-Level is not empty!");
            Print.logStackTrace("Location of prior table locking", (Throwable)DBRecord.lockLevel.peek());
        }
        
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("LOCK TABLES ");
            
            /* lock writes */
            if (writeTables != null) {
                for (int w = 0; w < writeTables.length; w++) {
                    if (w > 0) { sb.append(","); }
                    sb.append(writeTables[w]).append(" WRITE");
                }
            }
            
            /* lock reads */
            if (readTables != null) {
                if (writeTables != null) { sb.append(","); }
                for (int r = 0; r < readTables.length; r++) {
                    if (r > 0) { sb.append(","); }
                    sb.append(readTables[r]).append(" READ");
                }
            }
            
            /* execute locking */
            if (isTableLockingEnabled()) {
                Throwable t = new Throwable();
                t.fillInStackTrace();
                DBConnection.getDefaultConnection().executeUpdate(sb.toString());
                DBRecord.lockLevel.push(t);
            }
            
            return true;
            
        } catch (SQLException sqe) {
            //this.setLastCaughtSQLException(sqe);
            Print.logSQLError("lock " + writeTables[0], sqe);
            return false;
        }
        
    }

    /**
     * Attempts to unlock the database Field for reading and writing
     * by calling the unlockTables method.
     * 
     * @return boolean Determines if the tables have been unlocked.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public boolean unlock()
        throws DBException
    {
        return DBRecord.unlockTables();
    }

    /**
     * Attempts to unlock the database tables for reading and writing.
     * 
     * @return boolean Determines if the tables have been unlocked.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public static boolean unlockTables()
        throws DBException
    {
        ++unlockSeq;
        //Print.logDebug("UnLock Sequence: " + unlockSeq);
        
        try {
            
            String unlockSql = "UNLOCK TABLES";
            if (isTableLockingEnabled()) {
                DBConnection.getDefaultConnection().executeUpdate(unlockSql);
                if (!DBRecord.lockLevel.empty()) {
                    DBRecord.lockLevel.pop();
                } else {
                    Print.logStackTrace("Lock-Level stack is empty");
                }
            }
            if (!DBRecord.lockLevel.empty()) { 
                Print.logStackTrace("Unlock: Lock-Level is not empty!"); 
            }
            return true;
            
        } catch (SQLException sqe) {
            //this.setLastCaughtSQLException(sqe);
            Print.logSQLError("unlock tables", sqe);
            return false;
        }
        
    }

    // ------------------------------------------------------------------------

    /**
     * Generates SQL code to extract the Statement field from the database
     * table.
     * 
     * @param fact DBFactory.
     * @param where Table name.
     * @return Statement SQL statement.
     * @throws SQLException Throws an SQLException if an sql error occurs.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    protected static Statement getStatement(DBFactory fact, String where)
        throws SQLException, DBException
    {
        StringBuffer sb = new StringBuffer();
        // MySQL: SELECT * FROM <TableName> <SQLWhere>
        sb.append("SELECT * FROM ").append(fact.getTableName()).append(" ");
        if (where != null) { sb.append(where); }
        return DBConnection.getDefaultConnection().execute(sb.toString());
        // Note: this returned Statement must be closed when finished
    }

    /**
     * Executes the SQL statement passed as a string parameter.
     * Establishes a connection with the server utilizing the
     * DBConnection class.
     * 
     * @param sql
     * @return Statement
     * @throws SQLException Throws an SQLException if an sql error occurs.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    protected Statement execute(String sql)
        throws SQLException, DBException
    {
        return DBConnection.getDefaultConnection().execute(sql);
        // Note: this returned Statement must be closed when finished
    }

    /**
     * Executes the SQL update passed as a String parameter.
     * Establishes a connection with the database utilizing the
     * DBConnection class.
     * @param sql SQL statement.
     * @throws SQLException Throws an SQLException if an sql error occurs.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    protected void executeUpdate(String sql)
        throws SQLException, DBException
    {
        DBConnection.getDefaultConnection().executeUpdate(sql);
    }

    // ------------------------------------------------------------------------

    /**
     * Gets the field value from an OrderedMap utilizing the (fldName, value)
     * mapping scheme.
     */
    public Object getFieldValue(String fldName)
    {
        return this.getRecordKey().getFieldValues().getFieldValue(fldName);
    }

    /**
     * If the field object is of boolean type its value is returned,
     * if it is of number type the method returns true if the number
     * does not equal zero and false if it does.  If the object is
     * neither of these types the boolean value dft is returned (default).
     * @param fldName Field name.
     * @param dft Default boolean value.
     * @return boolean Field boolean value.
     */
    public boolean getFieldValue(String fldName, boolean dft)
    {
        Object obj = this.getRecordKey().getFieldValues().getFieldValue(fldName);
        if (obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue();
        } else 
        if (obj instanceof Number) {
            return (((Number)obj).intValue() != 0)? true : false;
        } else {
            return dft;
        }
    }

    /**
     * Attempts to return the int value of the field object if it is a number,
     * else the method returns the int dft (default).
     * @param fldName Field name.
     * @param dft Default int value.
     * @return int Field int value.
     */
    public int getFieldValue(String fldName, int dft)
    {
        Object obj = this.getRecordKey().getFieldValues().getFieldValue(fldName);
        return (obj instanceof Number)? ((Number)obj).intValue() : dft;
    }

    /**
     * Attempts to return the value of the field object if it is a long, else the
     * method returns the default long value.
     * @param fldName Field name.
     * @param dft Default long value.
     * @return long Field long value.
     */
    public long getFieldValue(String fldName, long dft)
    {
        Object obj = this.getRecordKey().getFieldValues().getFieldValue(fldName);
        return (obj instanceof Number)? ((Number)obj).longValue() : dft;
    }

    /**
     * Attempts to return the value of the field object if it is a float,
     * else the method returns the default float value.
     * @param fldName Field name.
     * @param dft Default float value.
     * @return float Field float value.
     */
    public float getFieldValue(String fldName, float dft)
    {
        Object obj = this.getRecordKey().getFieldValues().getFieldValue(fldName);
        return (obj instanceof Number)? ((Number)obj).floatValue() : dft;
    }

    /**
     * Attempts to return the value of the field object if it is a double,
     * else the method returns the default double value.
     * @param fldName Field name.
     * @param dft Default double value.
     * @return double Field double value.
     */
    public double getFieldValue(String fldName, double dft)
    {
        Object obj = this.getRecordKey().getFieldValues().getFieldValue(fldName);
        return (obj instanceof Number)? ((Number)obj).doubleValue() : dft;
    }

    // ------------------------------------------------------------------------

    /**
     * Sets the field's value to the typeless value passed.
     * @param fldName Field name.
     * @param value Object value.
     */
    public void setFieldValue(String fldName, Object value)
    {
        this.getRecordKey().getFieldValues().setFieldValue(fldName, value);
    }

    /**
     * Sets the field's value to the int value passed.
     * @param fldName Field name.
     * @param value int value.
     */
    public void setFieldValue(String fldName, int value)
    {
        this.getRecordKey().getFieldValues().setFieldValue(fldName, value);
    }

    /**
     * Sets the field's value to the long value passed.
     * @param fldName Field name.
     * @param value long value.
     */
    public void setFieldValue(String fldName, long value)
    {
        this.getRecordKey().getFieldValues().setFieldValue(fldName, value);
    }

    /**
     * Sets the field's value to the double value passed.
     * @param fldName Field name.
     * @param value Double value.
     */
    public void setFieldValue(String fldName, double value)
    {
        this.getRecordKey().getFieldValues().setFieldValue(fldName, value);
    }

    /**
     * Sets the field's value to the boolean value passed.
     * @param fldName Field Name.
     * @param value Boolean value.
     */
    public void setFieldValue(String fldName, boolean value)
    {
        this.getRecordKey().getFieldValues().setFieldValue(fldName, value);
    }

    /**
     * Sets the field values to the set of field values passed as
     * a parameter.
     * @param rs ResultSet containing Record field data.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    public void setFieldValues(ResultSet rs) 
        throws DBException
    {
        try {
            DBFieldValues fldVals = this.getRecordKey().getFieldValues();
            fldVals.clearFieldValues();
            fldVals.setFieldValues(rs);
        } catch (SQLException sqe) {
            this.setLastCaughtSQLException(sqe);
            throw new DBException("Setting field values", sqe);
        }
    }

    // ------------------------------------------------------------------------
    
    /**
     * Sets the lastCaughtSQLException to the exception passed.
     */
    protected void setLastCaughtSQLException(SQLException sqe)
    {
        this.lastSQLException = sqe;
    }
    
    /**
     * Clears the lastCaughtSQLException.
     *
     */
    public void clearLastCaughtSQLException()
    {
        this.setLastCaughtSQLException(null);
    }
    
    /**
     * Returns the lastCaughtSQLException.
     * @return SQLException lastCaughtSQLException.
     */
    public SQLException getLastCaughtSQLException()
    {
        return this.lastSQLException;
    }
    
    /**
     * Attempts to determine whether the last SQLException was caught.
     * @param code Integer Error code.
     * @return boolean Determines whether the last SQLException was caught.
     */
    public boolean isLastCaughtSQLExceptionErrorCode(int code)
    {
        SQLException sqe = this.getLastCaughtSQLException();
        if (sqe == null) {
            return false;
        } else
        if (sqe.getErrorCode() == code) {
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
     * Sets the isValidating flag to the boolean value passed as a parameter.
     */
    protected void setValidating(boolean validate)
    {
        this.isValidating = validate;
    }
    
    /**
     * Returns the validating flag indicating whether the field is in the
     * process of validating.
     * @return boolean isValidating flag.
     */
    protected boolean isValidating()
    {
        return this.isValidating;
    }
    
    // ------------------------------------------------------------------------

    /**
     * Attempts to determine if the current recordKey is equal to the
     * key of the object passed as a parameter.
     * If the object is a DBRecord or if the object is not a subclass
     * or interface of the current class, the method returns false.
     * If the current class is a superclass or interface of the object
     * passed then the keys are compared.
     * @param obj Object's key compared to the current Record key.
     * @return boolean Determines if the current key is equal to the object's
     * key.
     */
    public boolean equals(Object obj)
    {
        if (!(obj instanceof DBRecord)) {
            return false;
        } else
        if (this.getClass().isAssignableFrom(obj.getClass())) {
            return ((DBRecord)obj).getRecordKey().equals(this.getRecordKey());
        } else {
            return false;
        }
    }

    /**
     * Converts the current record key to a String.
     * @return String The current Record Key.
     */
    public String toString() // what the user sees
    {
        return this.getRecordKey().toString();
    }

    // ------------------------------------------------------------------------

    private static String currentUser = "";

    /**
     * Determines the name of the current user.
     * @return String Current user's name.
     */
    public static String GetCurrentUser()
    {
        if (currentUser == null) { currentUser = ""; }
        return currentUser;
    }

    /**
     * Sets the current user's name.
     * @param user Current user's name.
     */
    public static void SetCurrentUser(String user)
    {
        currentUser = (user != null)? user : "";
    }

    // ------------------------------------------------------------------------

    /**
     * Formats GPS location data returning it as a String.
     */
    public static String FormatGPS(double location)
    {
        return StringTools.format(location, "#0.#####");
    }

    // ------------------------------------------------------------------------

    /**
     * Creates an instance of the DBRecord class setting the fields according
     * to the DBRecordKey passed.
     * @throws DBException Throws a DBException if a database error occurs.
     */
    /* package */ static DBRecord _createDBRecord(DBRecordKey rcdKey)
        throws DBException
    {
        DBFactory factory = rcdKey.getFactory();
        Class dbrClass = factory.getRecordClass();
        Class dbkClass = factory.getKeyClass();
        try {
            Constructor con = dbrClass.getConstructor(new Class[] { dbkClass });
            return (DBRecord)con.newInstance(new Object[] { rcdKey });
        } catch (Throwable t) { // NoSuchMethodException, ...
            // Implementation error (this should never occur)
            throw new DBException("Unable to create DBRecord", t);
        }
    }
    
    // ------------------------------------------------------------------------

}
