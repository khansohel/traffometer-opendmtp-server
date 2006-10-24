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
// ----------------------------------------------------------------------------
package org.opendmtp.server_mysql.db;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opendmtp.util.*;

import org.opendmtp.dbtools.*;

public class Account
    extends DBRecord
{
        
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    public static final String  TABLE_NAME              = "Account";

    /* field definition */
    public static final String FLD_accountID            = "accountID";
    public static final String FLD_password             = "password";
    public static final String FLD_description          = "description";
    public static final String FLD_isActive             = "isActive";
    public static final String FLD_contactEmail         = "contactEmail";
    public static final String FLD_notifyEmail          = "notifyEmail";
    private static DBField FieldInfo[] = {
        new DBField(FLD_accountID     , String.class  , DBField.TYPE_STRING(32)  , "title=Account_ID key=true"),
        new DBField(FLD_password      , String.class  , DBField.TYPE_STRING(32)  , "title=Password"),
        new DBField(FLD_description   , String.class  , DBField.TYPE_STRING(128) , "title=Description edit=2"),
        new DBField(FLD_isActive      , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "title=Is_Active edit=2"),
        new DBField(FLD_contactEmail  , String.class  , DBField.TYPE_STRING(128) , "title=Contact_EMail_Address edit=2"),
        new DBField(FLD_notifyEmail   , String.class  , DBField.TYPE_STRING(128) , "title=Notification_EMail_Address edit=2"),
        newField_lastUpdateTime(),
    };

    /* key class */
    public static class Key
        extends DBRecordKey
    {
        public Key() {
            super();
        }
        public Key(String acctId) {
            super.setFieldValue(FLD_accountID, acctId);
        }
        public DBFactory getFactory() {
            return Account.getFactory();
        }
    }

    /* factory constructor */
    private static DBFactory factory = null;
    public static DBFactory getFactory()
    {
        if (factory == null) {
            factory = new DBFactory(TABLE_NAME, 
                FieldInfo, 
                KEY_PRIMARY,
                Account.class, 
                Account.Key.class);
        }
        return factory;
    }

    /* Bean instance */
    public Account()
    {
        super();
    }

    /* database record */
    public Account(Account.Key key)
    {
        super(key);
    }
    
    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below
    
    public String getAccountID()
    {
        String v = (String)this.getFieldValue(FLD_accountID);
        return (v != null)? v : "";
    }
    
    private void setAccountID(String v)
    {
        this.setFieldValue(FLD_accountID, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------
    
    public String getPassword()
    {
        String v = (String)this.getFieldValue(FLD_password);
        return (v != null)? v : "";
    }

    public void setPassword(String v)
    {
        this.setFieldValue(FLD_password, ((v != null)? v : ""));
    }

    public boolean checkPassword(String passwd)
    {
        String v = (String)this.getFieldValue(FLD_password);
        if (v != null) {
            return v.equals(passwd);
        } else {
            // no password, no access
            return false;
        }
    }

    // ------------------------------------------------------------------------

    public String getDescription()
    {
        String v = (String)this.getFieldValue(FLD_description);
        return (v != null)? v : "";
    }

    public void setDescription(String v)
    {
        this.setFieldValue(FLD_description, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    public boolean getIsActive()
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_isActive);
        return (v != null)? v.booleanValue() : false;
    }

    public void setIsActive(boolean v)
    {
        this.setFieldValue(FLD_isActive, v);
    }

    // ------------------------------------------------------------------------

    public String getContactEmail()
    {
        String v = (String)this.getFieldValue(FLD_contactEmail);
        return (v != null)? v : "";
    }

    public void setContactEmail(String v)
    {
        this.setFieldValue(FLD_contactEmail, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    public String getNotifyEmail()
    {
        String v = (String)this.getFieldValue(FLD_notifyEmail);
        return (v != null)? v : "";
    }

    public void setNotifyEmail(String v)
    {
        this.setFieldValue(FLD_notifyEmail, ((v != null)? v : ""));
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public String toString()
    {
        return this.getAccountID();
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static boolean exists(String acctID)
        throws DBException // if error occurs while testing existance
    {
        if (acctID != null) {
            Account.Key actKey = new Account.Key(acctID);
            return actKey.exists();
        }
        return false;
    }

    public static Account getAccount(String acctID)
        throws DBException // if error occurs while getting record
    {
        if (acctID != null) {
            Account.Key key = new Account.Key(acctID);
            if (key.exists()) {
                return (Account)key.getDBRecord(true);
            }
        }
        return null;
    }
    
}