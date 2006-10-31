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
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.dbtools;

import org.opendmtp.util.Print;
import org.opendmtp.util.RTConfig;

/**
 * TestTable class used for debug purposes only of the DBtable.
 * It contains a DBFactory instance, a class key that extends DBRecordKey
 * and a main method to execute.
 * 
 * @author Martin D. Flynn
 * @author Brandon Lee
 */
public class TestTable extends DBRecord {

  /**
   * String value containing the string "TestTable".
   * It is used as a param to create new DBFactory.
   */
  public static final String TABLE_NAME = "TestTable";
  
  /**
   * String value containing the string "testKey".
   * It is used as a param to create new DBFactory.
   */
  public static final String FLD_testKey = "testKey";
  
  /**
   * String value containing the string "description".
   * It is used as a param to create new DBFactory.
   */
  public static final String FLD_description = "description";
  
  private static DBField FieldInfo[] = {
      new DBField(FLD_testKey, String.class, DBField.TYPE_STRING(32), "title=Test_Key key=true"),
      new DBField(FLD_description, String.class, DBField.TYPE_STRING(64), "title=Description"), 
  };

  /**
   *  key class, extends DBRecordKey probably for testing purposes.  Adds method getFactory.
   */
  public static class Key extends DBRecordKey {
    
    /**
     * Constructor which calls super.setFieldValue().
     *  
     * @param key Used to call super.setFieldValue with FLD_testKey.
     */
    public Key(Object key) {
      super.setFieldValue(FLD_testKey, key);
    }
    
    /**
     * Returns DBFactory of TestTable by calling getFactory().
     * @return DBFactory which is in the TestTable.
     */
    public DBFactory getFactory() {
      return TestTable.getFactory();
    }
  }

  // factory constructor
  private static DBFactory factory = null;

  /**
   * Returns a DBFactory if factory is present if not it creates a new DBFactory using 
   * TABLE_NAME, KEY_PRIMARY, FieldInfo, TestTable.class, and TestTable.Key.class.
   * 
   * @return factory the DBFactory of TestTable
   */
  public static DBFactory getFactory() {
    if (factory == null) {
      factory = new DBFactory(TABLE_NAME, FieldInfo, KEY_PRIMARY, TestTable.class,
          TestTable.Key.class);
    }
    return factory;
  }

  /**
   * Constructor.
   * 
   * @param key Used to call the super of DBRecord.
   */
  public TestTable(TestTable.Key key) {
    super(key);
  }

  /**
   * Runs the test. 
   * 
   * @param argv an array of arguments taken from the comanline.
   */
  public static void main(String argv[]) {
    RTConfig.setCommandLineArgs(argv);
    String key = RTConfig.getString("key", "key1");
    try {
      TestTable.Key k = new TestTable.Key(key);
      TestTable t = (TestTable) k.getDBRecord();
      if (k.exists()) {
        t.reload();
        Print.logInfo("Key exists: " + key + " - " + t.getFieldValue(FLD_description));
      }
      else {
        Print.logInfo("Key is new: " + key);
      }
      t.setFieldValue(FLD_description, "Hello World [" + key + "]");
      t.save();
    }
    catch (DBException dbe) {
      Print.logException("TestTable", dbe);
    }
  }

}
