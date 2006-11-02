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


import org.opendmtp.dbtools.DBException;
import org.opendmtp.dbtools.DBFactory;
import org.opendmtp.dbtools.DBField;
import org.opendmtp.dbtools.DBRecord;
import org.opendmtp.dbtools.DBRecordKey;

/*
import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opendmtp.util.*;

import org.opendmtp.dbtools.*;*/

/**
 * Representation of a database account which also includes a SQL definition.
 * 
 * @author Martin D. Flynn
 * @author Elayne Man
 */
public class Account extends DBRecord {

  /** 
   * Table name. 
   */
  public static final String TABLE_NAME = "Account"; 

  /**
   * Account ID.  
   */
  public static final String FLD_accountID = "accountID";
  
  /**
   * Password. 
   */
  public static final String FLD_password = "password"; 
  
  /**
   * Description. 
   */
  public static final String FLD_description = "description";
  
  /**
   * Active. 
   */
  public static final String FLD_isActive = "isActive";
  
  /**
   *  Contact Email.
   */
  public static final String FLD_contactEmail = "contactEmail";
  
  /**
   *  Notification Email.
   */
  public static final String FLD_notifyEmail = "notifyEmail";
  
  /**
   * Field Information.
   */
  private static DBField FieldInfo[] = {
      new DBField(FLD_accountID, String.class, DBField.TYPE_STRING(32), "title=Account_ID key=true"),
      new DBField(FLD_password, String.class, DBField.TYPE_STRING(32), "title=Password"),
      new DBField(FLD_description, String.class, DBField.TYPE_STRING(128),
          "title=Description edit=2"),
      new DBField(FLD_isActive, Boolean.TYPE, DBField.TYPE_BOOLEAN, "title=Is_Active edit=2"),
      new DBField(FLD_contactEmail, String.class, DBField.TYPE_STRING(128),
          "title=Contact_EMail_Address edit=2"),
      new DBField(FLD_notifyEmail, String.class, DBField.TYPE_STRING(128),
          "title=Notification_EMail_Address edit=2"), newField_lastUpdateTime(), };

  /**
   * Representiation of a Database key.
   * 
   */
  public static class Key extends DBRecordKey {

    /**
     * Default constructor for the Key class.
     * 
     */
    public Key() {
      super();
    }

    /**
     * Constructor for the Key class.
     * 
     * @param acctId The account ID.
     */
    public Key(String acctId) {
      super.setFieldValue(FLD_accountID, acctId);
    }

    /**
     * Returns the Factory value from the account.
     * 
     * @return The Factory value.
     */
    public DBFactory getFactory() {
      return Account.getFactory();
    }
  }

  /**
   * Factory constructor.
   * 
   */
  private static DBFactory factory = null;

  /**
   * Creates a new factory if it does not exist, else it returns the current factory value.
   * 
   * @return The factory value.
   */
  public static DBFactory getFactory() {
    if (factory == null) {
      factory = new DBFactory(TABLE_NAME, FieldInfo, KEY_PRIMARY, Account.class, Account.Key.class);
    }
    return factory;
  }

  /**
   * The Bean instance.
   * 
   */
  public Account() {
    super();
  }

  /**
   * Creates a database key record.
   * 
   * @param key The database key record.
   */
  public Account(Account.Key key) {
    super(key);
  }

  // SQL table definition above
  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------
  // Bean access fields below

  /**
   * Returns the Account ID in String format.
   * 
   * @return The account ID.
   */
  public String getAccountID() {
    String v = (String) this.getFieldValue(FLD_accountID);
    return (v != null) ? v : "";
  }

  /**
   * Sets the Account ID given in String format.
   * 
   * @param v The String value of the account ID.
   */
  private void setAccountID(String v) {
    this.setFieldValue(FLD_accountID, ((v != null) ? v : ""));
  }

  /**
   * Returns the password.
   * 
   * @return The password.
   */
  public String getPassword() {
    String v = (String) this.getFieldValue(FLD_password);
    return (v != null) ? v : "";
  }

  /**
   * Sets the password.
   * 
   * @param v The password.
   */
  public void setPassword(String v) {
    this.setFieldValue(FLD_password, ((v != null) ? v : ""));
  }

  /**
   * Checks to see if the password is valid.
   * 
   * @param passwd The password.
   * @return True or false if the password is valid or not.
   */
  public boolean checkPassword(String passwd) {
    String v = (String) this.getFieldValue(FLD_password);
    if (v != null) {
      return v.equals(passwd);
    }
    else {
      // no password, no access
      return false;
    }
  }

  /**
   * Returns the description.
   * 
   * @return The description.
   */
  public String getDescription() {
    String v = (String) this.getFieldValue(FLD_description);
    return (v != null) ? v : "";
  }

  /**
   * Sets the description.
   * 
   * @param v The description.
   * 
   */
  public void setDescription(String v) {
    this.setFieldValue(FLD_description, ((v != null) ? v : ""));
  }

  /**
   * Returns a true or false depending if the field value is active.
   * 
   * @return True or false depending of the field value is active or not.
   */
  public boolean getIsActive() {
    Boolean v = (Boolean) this.getFieldValue(FLD_isActive);
    return (v != null) ? v.booleanValue() : false;
  }

  /**
   * Sets the active value of the field.
   * 
   * @param v The boolean value.
   */
  public void setIsActive(boolean v) {
    this.setFieldValue(FLD_isActive, v);
  }

  /**
   * Returns the contact email address.
   * 
   * @return The contact email address.
   */
  public String getContactEmail() {
    String v = (String) this.getFieldValue(FLD_contactEmail);
    return (v != null) ? v : "";
  }

  /**
   * Sets the contact email address.
   * 
   * @param v The contact email address.
   */
  public void setContactEmail(String v) {
    this.setFieldValue(FLD_contactEmail, ((v != null) ? v : ""));
  }

  /**
   * Returns the email address used for notification.
   * 
   * @return The notification email address.
   */
  public String getNotifyEmail() {
    String v = (String) this.getFieldValue(FLD_notifyEmail);
    return (v != null) ? v : "";
  }

  /**
   * Sets the email address used for notification.
   * 
   * @param v The notification email address.
   */
  public void setNotifyEmail(String v) {
    this.setFieldValue(FLD_notifyEmail, ((v != null) ? v : ""));
  }

  // Bean access fields above
  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

  /**
   * Overidden method that returns the account ID.
   * 
   * @return The account ID in String form.
   */
  public String toString() {
    return this.getAccountID();
  }

  /**
   * Determines if the given account exists given the account ID.
   * 
   * @param acctID The account ID.
   * @return True or fale if the account exists or not.
   * @throws DBException if error occurs while testing existance.
   */
  public static boolean exists(String acctID) throws DBException {
    if (acctID != null) {
      Account.Key actKey = new Account.Key(acctID);
      return actKey.exists();
    }
    return false;
  }

  /**
   * Returns the account given the account ID.
   * 
   * @param acctID The account ID.
   * @return the account.
   * @throws DBException if error occurs while retrieving record.
   */
  public static Account getAccount(String acctID) throws DBException {
    if (acctID != null) {
      Account.Key key = new Account.Key(acctID);
      if (key.exists()) {
        return (Account) key.getDBRecord(true);
      }
    }
    return null;
  }

}