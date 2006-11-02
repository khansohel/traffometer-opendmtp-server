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
// ----------------------------------------------------------------------------
package org.opendmtp.server_mysql.db;

import java.util.Vector;

import org.opendmtp.dbtools.DBException;
import org.opendmtp.dbtools.DBFactory;
import org.opendmtp.dbtools.DBField;
import org.opendmtp.dbtools.DBRecord;
import org.opendmtp.dbtools.DBRecordKey;
import org.opendmtp.server.db.PayloadTemplate;
import org.opendmtp.server_mysql.dbtypes.DTTemplate;

/**
 * Provides a way to handle storage of custom event templates in the database.
 * 
 * @author Martin D. Flynn
 * @author Robert S. Brewer
 */
public class EventTemplate extends DBRecord {

  // ------------------------------------------------------------------------

  /**
   * Name of the table in the database.
   */
  public static final String TABLE_NAME = "EventTemplate";

  /* field definition */
  /**
   * Name of database field holding account ID.
   */
  public static final String FLD_accountID = "accountID";
  /**
   * Name of database field holding device ID.
   */
  public static final String FLD_deviceID = "deviceID";
  /**
   * Name of database field holding custom type.
   */
  public static final String FLD_customType = "customType";
  /**
   * Name of database field holding repeat last.
   */
  public static final String FLD_repeatLast = "repeatLast";
  /**
   * Name of database field holding template.
   */
  public static final String FLD_template = "template";
  private static DBField FieldInfo[] = {
      new DBField(FLD_accountID, String.class, DBField.TYPE_STRING(32), "title=Account_ID key=true"),
      new DBField(FLD_deviceID, String.class, DBField.TYPE_STRING(32), "title=Device_ID key=true"),
      new DBField(FLD_customType, Integer.TYPE, DBField.TYPE_UINT8, "title=Custom_type key=true"),
      new DBField(FLD_repeatLast, Boolean.TYPE, DBField.TYPE_BOOLEAN, "title=Repeat_last"),
      new DBField(FLD_template, DTTemplate.class, DBField.TYPE_TEXT, "title=Template"), };

  /**
   * Specifies the keys used in the SQL database.
   * 
   * @author Martin D. Flynn
   * @author Robert S. Brewer
   */
  public static class Key extends DBRecordKey {
    
    /**
     * Basic constructor, just calls superclass constructor.
     */
    public Key() {
      super();
    }

    /**
     * Creates new record keys for GPS device events.
     *  
     * @param acctId account ID to add as key.
     * @param devId device ID to add as key.
     * @param customType custom type to add as key.
     */
    public Key(String acctId, String devId, int customType) {
      super.setFieldValue(FLD_accountID, acctId);
      super.setFieldValue(FLD_deviceID, devId);
      super.setFieldValue(FLD_customType, customType);
    }

    /**
     * Returns the factory associated with database.
     * 
     * @return The factory associated with database.
     * @see org.opendmtp.dbtools.DBRecordKey#getFactory()
     */
    public DBFactory getFactory() {
      return Device.getFactory();
    }
  }

  /* factory constructor */
  private static DBFactory factory = null;

  /**
   * Gets the DBFactory, creating a new factory if one does not already exist.
   * 
   * @return The DBFactory (possibly new).
   */
  public static DBFactory getFactory() {
    if (factory == null) {
      factory = new DBFactory(TABLE_NAME, FieldInfo, KEY_PRIMARY, EventTemplate.class,
          EventTemplate.Key.class);
    }
    return factory;
  }

  /* Bean instance */
  /**
   * Constructs a new instance.
   */
  public EventTemplate() {
    super();
  }

  /* database record */
  /**
   * Constructs a new instance using the supplied Key.
   * 
   * @param key Key to be used for new object.
   */
  public EventTemplate(EventTemplate.Key key) {
    super(key);
  }

  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

  /**
   * Searches database using provided parameters and returns any matching custom
   * payload template.  
   * 
   * @param acctId account ID to match.
   * @param devId device ID to match.
   * @param custType custom type to match.
   * @return PayloadTemplate created from database value.
   */
  public static PayloadTemplate GetPayloadTemplate(String acctId, String devId, int custType) {
    EventTemplate.Key tmpKey = new EventTemplate.Key(acctId, devId, custType);
    EventTemplate et = (EventTemplate) tmpKey.getDBRecord();
    if (et.reload() != null) {
      // Event template exists
      return et.createPayloadTemplate();
    }
    else {
      // Event template does not exist
      return null;
    }
  }

  /**
   * Saves the specified payload template into the database using the provided account ID
   * and device ID.
   * 
   * @param acctId account ID to use as key.
   * @param devId device ID to use as key.
   * @param pt PayloadTemplate to be added to database.
   * @return true if payload template could be written to database, false otherwise.
   */
  public static boolean SetPayloadTemplate(String acctId, String devId, PayloadTemplate pt) {
    EventTemplate.Key etKey = new EventTemplate.Key(acctId, devId, pt.getPacketType());
    EventTemplate et = (EventTemplate) etKey.getDBRecord();
    et.initFromPayloadTemplate(pt);
    try {
      et.save();
      return true;
    }
    catch (DBException dbe) {
      return false;
    }
  }

  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------
  // Bean access fields below

  /**
   * Returns account ID from database field.
   * 
   * @return account ID String, or empty String if value is null in database. 
   */
  public String getAccountID() {
    String v = (String) this.getFieldValue(FLD_accountID);
    return (v != null) ? v : "";
  }

  /**
   * Sets account ID in database field.
   * 
   * @param v account ID to store in database, if null then empty String will be stored
   * instead.
   */
  private void setAccountID(String v) {
    this.setFieldValue(FLD_accountID, ((v != null) ? v : ""));
  }

  // ------------------------------------------------------------------------

  /**
   * Returns device ID from database field.
   * 
   * @return device ID String, or empty String if value is null in database. 
   */
  public String getDeviceID() {
    String v = (String) this.getFieldValue(FLD_deviceID);
    return (v != null) ? v : "";
  }

  /**
   * Sets device ID in database field.
   * 
   * @param v device ID to store in database, if null then empty String will be stored
   * instead.
   */
  private void setDeviceID(String v) {
    this.setFieldValue(FLD_deviceID, ((v != null) ? v : ""));
  }

  // ------------------------------------------------------------------------

  /**
   * Returns custom type from database field.
   * 
   * @return custom type, or 0 if value is null in database. 
   */
  public int getCustomType() {
    Integer v = (Integer) this.getFieldValue(FLD_customType);
    return (v != null) ? v.intValue() : 0;
  }

  /**
   * Sets custom type in database field.
   * 
   * @param v custom type to store in database.
   */
  public void setCustomType(int v) {
    this.setFieldValue(FLD_customType, v);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns repeat last flag from database field.
   * 
   * @return repeat last flag value, or false if value is null in database. 
   */
  public boolean getRepeatLast() {
    Boolean v = (Boolean) this.getFieldValue(FLD_repeatLast);
    return (v != null) ? v.booleanValue() : false;
  }

  /**
   * Sets repeat last flag in database field.
   * 
   * @param v repeat last flag value to store in database.
   */
  public void setRepeatLast(boolean v) {
    this.setFieldValue(FLD_repeatLast, v);
  }

  // ------------------------------------------------------------------------

  /**
   * Returns template from database field.
   * 
   * @return template, or null if value is null in database. 
   */
  public DTTemplate getTemplate() {
    DTTemplate v = (DTTemplate) this.getFieldValue(FLD_template);
    return (v != null) ? v : null;
  }

  /**
   * Sets template in database field.
   * 
   * @param v template to store in database.
   */
  public void setTemplate(DTTemplate v) {
    this.setFieldValue(FLD_template, v);
  }

  // Bean access fields above
  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

  /**
   * Initializes custom template record given PayloadTemplate.
   * 
   * @param pt PayloadTemplate to start from.
   */
  public void initFromPayloadTemplate(PayloadTemplate pt) {
    this.setFieldValue(FLD_repeatLast, pt.getRepeatLast());
    PayloadTemplate.Field flds[] = pt.getFields();
    DTTemplate template = this.getTemplate();
    template.clearFields();
    for (int i = 0; i < flds.length; i++) {
      int type = flds[i].getType();
      boolean isHi = flds[i].isHiRes();
      int ndx = flds[i].getIndex();
      int len = flds[i].getLength();
      DTTemplate.Field dtf = new DTTemplate.Field(type, isHi, ndx, len);
      template.setField(i, dtf);
    }
    // put the value back in case in the future this is a clone of the actual template
    this.setFieldValue(FLD_template, template);
  }

  /**
   * Create a PayloadTemplate using template stored in database.
   * 
   * @return newly created PayloadTemplate.
   */
  public PayloadTemplate createPayloadTemplate() {

    /* assemble PayloadTemplate.Field array */
    DTTemplate t = this.getTemplate();
    java.util.List pfl = new Vector();
    for (int i = 0;; i++) {
      DTTemplate.Field tf = t.getField(i);
      if (tf == null) {
        break;
      }
      int type = tf.getType();
      boolean isHi = tf.isHiRes();
      int ndx = tf.getIndex();
      int len = tf.getLength();
      PayloadTemplate.Field pf = new PayloadTemplate.Field(type, isHi, ndx, len);
      pfl.add(pf);
    }
    PayloadTemplate.Field pfa[] = (PayloadTemplate.Field[]) pfl
        .toArray(new PayloadTemplate.Field[pfl.size()]);

    /* create/return new PayloadTemplate */
    return new PayloadTemplate(this.getCustomType(), pfa, this.getRepeatLast());

  }

}