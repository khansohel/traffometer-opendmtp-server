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
 * The <code>DBRecord</code> class defines a database record
 * value along with all necessary methods to keep track of
 * the any changes to the record.  This includes metadata,
 * locking methods to ensure integrity of the data.
 * 
 * @author Josh
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
     * String constant marking last update time.
     */
    public static final String FLD_lastUpdateTime   = "lastUpdateTime";
    
    /**
     * String constant denoting the last user who updated.
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
     * <code>DBRecord</code> constructor taking an instance of the
     * <code>DBRecordKey</code> class as a parameter.
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
     * @param dbr DBRecord
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
     * @param max int
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
     * @param fact DBFactory
     * @param where String
     * @return returns an array of DBRecords
     */
    protected static DBRecord[] select(DBFactory fact, String where)
        throws DBException
    {
        return DBRecord.select(fact, where, null);
    }
    
    /**
     * Selects an array of DBRecords, modeling the common SQL statement
     * SELECT, FROM, WHERE.  
     * @param fact
     * @param where
     * @param addtnlSel
     * @return returns an array of DBRecords
     * @throws DBException
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
     * @return boolean
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
     * @param fieldName
     * @param oldVal
     * @param newVal
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

    public void clearChanged()
    {
        this.changed = false;
    }

    public void addChangedNotification(DBChangeListener cl)
    {
        if (this.changeNotification == null) { this.changeNotification = new Vector(); }
        if ((cl != null) && !this.changeNotification.contains(cl)) {
            this.changeNotification.add(cl);
        }
    }

    public void removeChangedNotification(DBChangeListener cl)
    {
        if (this.changeNotification != null) {
            this.changeNotification.remove(cl);
        }
    }

    public void fireChangeNotification(String fieldName)
    {
        if (this.changeNotification != null) {
            for (Iterator i = this.changeNotification.iterator(); i.hasNext();) {
                DBChangeListener dbcr = (DBChangeListener)i.next();
                dbcr.fieldChanged(this, fieldName);
            }
        }
    }

    public static interface DBChangeListener
    {
        public void fieldChanged(DBRecord rcd, String fieldName);
    }

    // ------------------------------------------------------------------------

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

    public static DBField newField_lastUpdateTime()
    {
        return new DBField(FLD_lastUpdateTime, Long.TYPE, DBField.TYPE_UINT32, "title=Last_Update_Time");
    }

    public long getLastUpdateTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastUpdateTime);
        return (v != null)? v.longValue() : -1L;
    }

    protected void setLastUpdateTime(long time)
    {
        long t = (time >= 0L)? time : 0L;
        this.setFieldValue(FLD_lastUpdateTime, t);
    }

    // ------------------------------------------------------------------------

    public static DBField newField_lastUpdateUser()
    {
        return new DBField(FLD_lastUpdateUser, String.class, DBField.TYPE_STRING(32), "title=Last_Update_User");
    }

    public String getLastUpdateUser()
    {
        String v = (String)this.getFieldValue(FLD_lastUpdateUser);
        return (v != null)? v : null;
    }

    public void setLastUpdateUser(String user)
    {
        String u = (user != null)? user : "";
        this.setFieldValue(FLD_lastUpdateUser, u);
    }
    
    // ------------------------------------------------------------------------

    public void insert()
        throws DBException
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

    public void update()
        throws DBException
    {
        this.update(null);
    }

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

    public void updateField(String fldName, int value)
        throws DBException
    {
        this._updateField(fldName, String.valueOf(value));
    }

    public void updateField(String fldName, long value)
        throws DBException
    {
        this._updateField(fldName, String.valueOf(value));
    }

    public void updateField(String fldName, String value)
        throws DBException
    {
        this._updateField(fldName, StringTools.quoteString(value));
    }
    
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

    public static int getLockLevel()
    {
        return lockLevel.size();
    }

    public boolean lockWrite()
        throws DBException
    {
        return this.lock(new String[] { this._getFactory().getTableName() }, null);
    }

    public boolean lockRead()
        throws DBException
    {
        // write locks are always included
        return this.lock(null, new String[] { this._getFactory().getTableName() });
    }

    public boolean lock(String writeTables[], String readTables[])
        throws DBException
    {
        if (writeTables == null) {
            writeTables = new String[] { this._getFactory().getTableName() };
        }
        return DBRecord.lockTables(writeTables, readTables);
    }

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

    public boolean unlock()
        throws DBException
    {
        return DBRecord.unlockTables();
    }

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

    protected Statement execute(String sql)
        throws SQLException, DBException
    {
        return DBConnection.getDefaultConnection().execute(sql);
        // Note: this returned Statement must be closed when finished
    }

    protected void executeUpdate(String sql)
        throws SQLException, DBException
    {
        DBConnection.getDefaultConnection().executeUpdate(sql);
    }

    // ------------------------------------------------------------------------

    public Object getFieldValue(String fldName)
    {
        return this.getRecordKey().getFieldValues().getFieldValue(fldName);
    }

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

    public int getFieldValue(String fldName, int dft)
    {
        Object obj = this.getRecordKey().getFieldValues().getFieldValue(fldName);
        return (obj instanceof Number)? ((Number)obj).intValue() : dft;
    }

    public long getFieldValue(String fldName, long dft)
    {
        Object obj = this.getRecordKey().getFieldValues().getFieldValue(fldName);
        return (obj instanceof Number)? ((Number)obj).longValue() : dft;
    }

    public float getFieldValue(String fldName, float dft)
    {
        Object obj = this.getRecordKey().getFieldValues().getFieldValue(fldName);
        return (obj instanceof Number)? ((Number)obj).floatValue() : dft;
    }

    public double getFieldValue(String fldName, double dft)
    {
        Object obj = this.getRecordKey().getFieldValues().getFieldValue(fldName);
        return (obj instanceof Number)? ((Number)obj).doubleValue() : dft;
    }

    // ------------------------------------------------------------------------

    public void setFieldValue(String fldName, Object value)
    {
        this.getRecordKey().getFieldValues().setFieldValue(fldName, value);
    }

    public void setFieldValue(String fldName, int value)
    {
        this.getRecordKey().getFieldValues().setFieldValue(fldName, value);
    }

    public void setFieldValue(String fldName, long value)
    {
        this.getRecordKey().getFieldValues().setFieldValue(fldName, value);
    }

    public void setFieldValue(String fldName, double value)
    {
        this.getRecordKey().getFieldValues().setFieldValue(fldName, value);
    }

    public void setFieldValue(String fldName, boolean value)
    {
        this.getRecordKey().getFieldValues().setFieldValue(fldName, value);
    }
    
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
    
    protected void setLastCaughtSQLException(SQLException sqe)
    {
        this.lastSQLException = sqe;
    }
    
    public void clearLastCaughtSQLException()
    {
        this.setLastCaughtSQLException(null);
    }
    
    public SQLException getLastCaughtSQLException()
    {
        return this.lastSQLException;
    }
    
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

    protected void setValidating(boolean validate)
    {
        this.isValidating = validate;
    }
    
    protected boolean isValidating()
    {
        return this.isValidating;
    }
    
    // ------------------------------------------------------------------------

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

    public String toString() // what the user sees
    {
        return this.getRecordKey().toString();
    }

    // ------------------------------------------------------------------------

    private static String currentUser = "";

    public static String GetCurrentUser()
    {
        if (currentUser == null) { currentUser = ""; }
        return currentUser;
    }

    public static void SetCurrentUser(String user)
    {
        currentUser = (user != null)? user : "";
    }

    // ------------------------------------------------------------------------

    public static String FormatGPS(double location)
    {
        return StringTools.format(location, "#0.#####");
    }

    // ------------------------------------------------------------------------

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
