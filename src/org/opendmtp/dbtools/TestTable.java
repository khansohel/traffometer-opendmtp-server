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

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opendmtp.util.*;

public class TestTable
    extends DBRecord
{
        
    // ------------------------------------------------------------------------
    // This table is used for debug purposes only

    /* table name */
    public static final String TABLE_NAME               = "TestTable";

    /* field definition */
    public static final String FLD_testKey              = "testKey";
    public static final String FLD_description          = "description";
    private static DBField FieldInfo[] = {
        new DBField(FLD_testKey     , String.class , DBField.TYPE_STRING(32) , "title=Test_Key key=true"),
        new DBField(FLD_description , String.class , DBField.TYPE_STRING(64) , "title=Description"),
    };

    /* key class */
    public static class Key
        extends DBRecordKey
    {
        public Key(Object key) {
            super.setFieldValue(FLD_testKey, key);
        }
        public DBFactory getFactory() {
            return TestTable.getFactory();
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
                TestTable.class, 
                TestTable.Key.class);
        }
        return factory;
    }

    /* database record */
    public TestTable(TestTable.Key key)
    {
        super(key);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        String key = RTConfig.getString("key","key1");
        try {
            TestTable.Key k = new TestTable.Key(key);
            TestTable t = (TestTable)k.getDBRecord();
            if (k.exists()) {
                t.reload();
                Print.logInfo("Key exists: " + key + " - " + t.getFieldValue(FLD_description));
            } else {
                Print.logInfo("Key is new: " + key);
            }
            t.setFieldValue(FLD_description, "Hello World [" + key + "]");
            t.save();
        } catch (DBException dbe) {
            Print.logException("TestTable", dbe);
        }
    }
    
}
