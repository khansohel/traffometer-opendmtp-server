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

public class EventTemplate extends DBRecord {

  // ------------------------------------------------------------------------

  /* table name */
  public static final String TABLE_NAME = "EventTemplate";

  /* field definition */
  public static final String FLD_accountID = "accountID";
  public static final String FLD_deviceID = "deviceID";
  public static final String FLD_customType = "customType";
  public static final String FLD_repeatLast = "repeatLast";
  public static final String FLD_template = "template";
  private static DBField FieldInfo[] = {
      new DBField(FLD_accountID, String.class, DBField.TYPE_STRING(32), "title=Account_ID key=true"),
      new DBField(FLD_deviceID, String.class, DBField.TYPE_STRING(32), "title=Device_ID key=true"),
      new DBField(FLD_customType, Integer.TYPE, DBField.TYPE_UINT8, "title=Custom_type key=true"),
      new DBField(FLD_repeatLast, Boolean.TYPE, DBField.TYPE_BOOLEAN, "title=Repeat_last"),
      new DBField(FLD_template, DTTemplate.class, DBField.TYPE_TEXT, "title=Template"), };

  /* key class */
  public static class Key extends DBRecordKey {
    public Key() {
      super();
    }

    public Key(String acctId, String devId, int customType) {
      super.setFieldValue(FLD_accountID, acctId);
      super.setFieldValue(FLD_deviceID, devId);
      super.setFieldValue(FLD_customType, customType);
    }

    public DBFactory getFactory() {
      return Device.getFactory();
    }
  }

  /* factory constructor */
  private static DBFactory factory = null;

  public static DBFactory getFactory() {
    if (factory == null) {
      factory = new DBFactory(TABLE_NAME, FieldInfo, KEY_PRIMARY, EventTemplate.class,
          EventTemplate.Key.class);
    }
    return factory;
  }

  /* Bean instance */
  public EventTemplate() {
    super();
  }

  /* database record */
  public EventTemplate(EventTemplate.Key key) {
    super(key);
  }

  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

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

  public String getAccountID() {
    String v = (String) this.getFieldValue(FLD_accountID);
    return (v != null) ? v : "";
  }

  private void setAccountID(String v) {
    this.setFieldValue(FLD_accountID, ((v != null) ? v : ""));
  }

  // ------------------------------------------------------------------------

  public String getDeviceID() {
    String v = (String) this.getFieldValue(FLD_deviceID);
    return (v != null) ? v : "";
  }

  private void setDeviceID(String v) {
    this.setFieldValue(FLD_deviceID, ((v != null) ? v : ""));
  }

  // ------------------------------------------------------------------------

  public int getCustomType() {
    Integer v = (Integer) this.getFieldValue(FLD_customType);
    return (v != null) ? v.intValue() : 0;
  }

  public void setCustomType(int v) {
    this.setFieldValue(FLD_customType, v);
  }

  // ------------------------------------------------------------------------

  public boolean getRepeatLast() {
    Boolean v = (Boolean) this.getFieldValue(FLD_repeatLast);
    return (v != null) ? v.booleanValue() : false;
  }

  public void setRepeatLast(boolean v) {
    this.setFieldValue(FLD_repeatLast, v);
  }

  // ------------------------------------------------------------------------

  public DTTemplate getTemplate() {
    DTTemplate v = (DTTemplate) this.getFieldValue(FLD_template);
    return (v != null) ? v : null;
  }

  public void setTemplate(DTTemplate v) {
    this.setFieldValue(FLD_template, v);
  }

  // Bean access fields above
  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

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