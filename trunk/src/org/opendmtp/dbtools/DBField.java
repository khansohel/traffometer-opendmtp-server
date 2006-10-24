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
//  2006/04/02  Martin D. Flynn
//      Added 'format' attribute
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.dbtools;

import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import java.sql.*;

import org.opendmtp.util.*;

public class DBField
{

    // ------------------------------------------------------------------------

    public static final int KEY_PRIMARY = 1;
    public static final int KEY_UNIQUE  = 2;
    public static final int KEY_INDEX   = 3;

    // ------------------------------------------------------------------------
    // EDIT_NEVER   : Never editable (maintained by system)
    // EDIT_NEW     : Only editable when new records are created
    // EDIT_ADMIN   : Editable by admin only
    // EDIT_PUBLIC  : Editable by anyone having access to the data

    public static final int EDIT_NEVER  = -1;
    public static final int EDIT_NEW    = 0;
    public static final int EDIT_ADMIN  = 1;
    public static final int EDIT_PUBLIC = 2;

    public static final int EDIT_RDONLY = 9999;

    // ------------------------------------------------------------------------
    // field attributes
    
    public static final String ATTR_KEY     = "key";        // [true/false]
    public static final String ATTR_ALTKEY  = "altkey";     // [true/false]
    public static final String ATTR_EDIT    = "edit";       // [0/1/2] editable mode
    public static final String ATTR_TITLE   = "title";      // title
    public static final String ATTR_FORMAT  = "format";     // format

    // ------------------------------------------------------------------------

    private String          name            = null;
    private Class           javaClass       = null;
    private Constructor     javaClassConst  = null;
    private String          dataType        = null;
    private boolean         isPriKey        = false;
    private boolean         isAltKey        = false;
    private RTProperties    attr            = null;
    private DBFactory       factory         = null;

    // ------------------------------------------------------------------------

    public DBField(DBField other)
    {
        this.name       = other.name;
        this.javaClass  = other.javaClass;
        this.dataType   = other.dataType;
        this.attr       = other.attr;
        this.isPriKey   = other.isPriKey;
        this.isAltKey   = other.isAltKey;
    }

    public DBField(String name, String dataType, boolean isPriKey)
    {
        // used by DBFactory.getTableColumns
        this.name       = name;
        this.javaClass  = null;
        this.dataType   = dataType;
        this.attr       = new RTProperties("");
        this.isPriKey   = isPriKey;
        this.isAltKey   = false;
    }

    public DBField(String name, Class javaClass, String dataType, String attr)
    {
        this.name       = name;
        this.javaClass  = javaClass;
        this.dataType   = dataType;
        this.attr       = new RTProperties((attr != null)? attr : "");
        this.isPriKey   = this.getBooleanAttribute(ATTR_KEY);
        this.isAltKey   = this.getBooleanAttribute(ATTR_ALTKEY);
    }

    // ------------------------------------------------------------------------

    public void setFactory(DBFactory factory)
    {
        this.factory = factory;
    }
    
    public DBFactory getFactory()
    {
        return this.factory;
    }
    
    // ------------------------------------------------------------------------

    public String getName()
    {
        return this.name;
    }

    public Class getTypeClass()
    {
        return this.javaClass;
    }

    public boolean isPriKey()
    {
        return this.isPriKey;
    }

    public boolean isAltKey()
    {
        return this.isAltKey;
    }

    // ------------------------------------------------------------------------

    public static final String TYPE_BOOLEAN     = "BOOLEAN";
    public static final String TYPE_INT8        = "INT8";
    public static final String TYPE_UINT8       = "UINT8";
    public static final String TYPE_INT16       = "INT16";
    public static final String TYPE_UINT16      = "UINT16";
    public static final String TYPE_INT32       = "INT32";
    public static final String TYPE_UINT32      = "UINT32";
    public static final String TYPE_INT64       = "INT64";
    public static final String TYPE_UINT64      = "UINT64";
    public static final String TYPE_FLOAT       = "FLOAT";
    public static final String TYPE_DOUBLE      = "DOUBLE";
    public static final String TYPE_BINARY      = "BINARY";
    public static final String TYPE_TEXT        = "TEXT";
    public static final String TYPE_STRING      = "STRING";
    public static String TYPE_STRING(int size) { return TYPE_STRING + "[" + size + "]"; }
    
    public String getDataType()
    {
        return this.dataType;
    }

    // ------------------------------------------------------------------------

    public static final String HIB_BOOLEAN      = "boolean";           //  8bit          Java 'boolean'
    public static final String HIB_INT8         = "byte";              //  8bit (signed) Java 'byte'
    public static final String HIB_UINT8        = "unsigned byte";     //  8bit          Java 'byte'
    public static final String HIB_INT16        = "short";             // 16bit (signed)
    public static final String HIB_UINT16       = "unsigned short";    // 16bit (signed)
    public static final String HIB_INT32        = "integer";           // 32bit (signed) Java 'int'
    public static final String HIB_UINT32       = "unsigned integer";  // 32bit          Java 'int'
    public static final String HIB_INT64        = "long";              // 64bit (signed) Java 'long'
    public static final String HIB_UINT64       = "unsigned long";     // 64bit          Java 'long'
    public static final String HIB_FLOAT        = "float";
    public static final String HIB_DOUBLE       = "double";
    public static final String HIB_BINARY       = "binary";            // max (2^16 - 1) bytes
    public static final String HIB_TEXT         = "text";              // max (2^16 - 1) bytes
    public static final String HIB_STRING       = "string";
    public static String HIB_STRING(int size) { return HIB_STRING + "(" + size + ")"; }

    private String _getHibernateType()
    {
        String dt = this.dataType.toUpperCase();
        if (dt.equals(TYPE_BOOLEAN)) {
            return HIB_BOOLEAN;
        } else
        if (dt.equals(TYPE_INT8)) {
            return HIB_INT8;
        } else
        if (dt.equals(TYPE_UINT8)) {
            return HIB_UINT8;
        } else
        if (dt.equals(TYPE_INT16)) {
            return HIB_INT16;
        } else
        if (dt.equals(TYPE_UINT16)) {
            return HIB_UINT16;
        } else
        if (dt.equals(TYPE_INT32)){
            return HIB_INT32;
        } else
        if (dt.equals(TYPE_UINT32)) {
            return HIB_UINT32;
        } else
        if (dt.equals(TYPE_INT64)) {
            return HIB_INT64;
        } else
        if (dt.equals(TYPE_UINT64)) {
            return HIB_UINT64;
        } else
        if (dt.equals(TYPE_FLOAT)) {
            return HIB_FLOAT;
        } else
        if (dt.equals(TYPE_DOUBLE)) {
            return HIB_DOUBLE;
        } else
        if (dt.equals(TYPE_BINARY)) {
            return HIB_BINARY;
        } else
        if (dt.equals(TYPE_TEXT)) {
            return HIB_TEXT;
        } else
        if (dt.startsWith(TYPE_STRING + "[")) {
            //String x = dt.substring(TYPE_STRING.length() + 1);
            //int len = StringTools.parseInt(x, 32);
            //return HIB_STRING(len);
            return HIB_STRING;
        } else {
            Print.logError("Unrecognized type: " + dt);
            return HIB_STRING(32);
        }
    }
    
    public String getHibernateType()
    {
        String hibType = _getHibernateType();
        return hibType;
    }

    // ------------------------------------------------------------------------

    public static final String SQL_NOT_NULL     = "NOT NULL";

    public static final String SQL_BOOLEAN      = "TINYINT";           //  8bit          Java 'boolean'
    public static final String SQL_INT8         = "TINYINT";           //  8bit (signed) Java 'byte'
    public static final String SQL_UINT8        = "TINYINT UNSIGNED";  //  8bit          Java 'byte'
    public static final String SQL_INT16        = "SMALLINT";          // 16bit (signed)
    public static final String SQL_UINT16       = "SMALLINT UNSIGNED"; // 16bit (signed)
    public static final String SQL_INT32        = "INT";               // 32bit (signed) Java 'int'
    public static final String SQL_UINT32       = "INT UNSIGNED";      // 32bit          Java 'int'
    public static final String SQL_INT64        = "BIGINT";            // 64bit (signed) Java 'long'
    public static final String SQL_UINT64       = "BIGINT UNSIGNED";   // 64bit          Java 'long'
    public static final String SQL_FLOAT        = "FLOAT";
    public static final String SQL_DOUBLE       = "DOUBLE";
    public static final String SQL_BINARY       = "BLOB";              // max (2^16 - 1) bytes
    public static final String SQL_TEXT         = "TEXT";
    public static final String SQL_VARCHAR      = "VARCHAR";
    public static String SQL_VARCHAR(int size) { return SQL_VARCHAR + "(" + size + ")"; }

    private String _getSqlType()
    {
        String dt = this.dataType.toUpperCase();
        if (dt.equals(TYPE_BOOLEAN)) {
            return SQL_BOOLEAN;
        } else
        if (dt.equals(TYPE_INT8)) {
            return SQL_INT8;
        } else
        if (dt.equals(TYPE_UINT8)) {
            return SQL_UINT8;
        } else
        if (dt.equals(TYPE_INT16)) {
            return SQL_INT16;
        } else
        if (dt.equals(TYPE_UINT16)) {
            return SQL_UINT16;
        } else
        if (dt.equals(TYPE_INT32)){
            return SQL_INT32;
        } else
        if (dt.equals(TYPE_UINT32)) {
            return SQL_UINT32;
        } else
        if (dt.equals(TYPE_INT64)) {
            return SQL_INT64;
        } else
        if (dt.equals(TYPE_UINT64)) {
            return SQL_UINT64;
        } else
        if (dt.equals(TYPE_FLOAT)) {
            return SQL_FLOAT;
        } else
        if (dt.equals(TYPE_DOUBLE)) {
            return SQL_DOUBLE;
        } else
        if (dt.equals(TYPE_BINARY)) {
            return SQL_BINARY;  // BLOB
        } else
        if (dt.equals(TYPE_TEXT)) {
            return SQL_TEXT;    // CLOB
        } else
        if (dt.startsWith(TYPE_STRING + "[")) {
            String x = dt.substring(TYPE_STRING.length() + 1);
            int len = StringTools.parseInt(x, 32);
            return SQL_VARCHAR(len);
        } else {
            Print.logError("Unrecognized type: " + dt);
            return SQL_VARCHAR(32);
        }
    }
    
    public String getSqlType()
    {
        String sqlType = _getSqlType();
        if (this.isPriKey()) {
            if (sqlType.toUpperCase().endsWith(SQL_NOT_NULL.toUpperCase())) {
                return sqlType;
            } else {
                return sqlType + " " + SQL_NOT_NULL;
            }
        } else {
            return sqlType;
        }
    }

    // ------------------------------------------------------------------------

    public int getLength()
    {
        String dt = this.dataType.toUpperCase();
        if (dt.startsWith(TYPE_STRING + "[")) {
            String x = dt.substring(TYPE_STRING.length() + 1);
            int len = StringTools.parseInt(x, 32);
            return len;
        } else {
            return 0;
        }
    }
    
    // ------------------------------------------------------------------------

    public boolean isCLOB()
    {
        return (this.dataType.toUpperCase().indexOf(TYPE_TEXT) >= 0);
    }
    
    public boolean isBLOB()
    {
        return (this.dataType.toUpperCase().indexOf(TYPE_BINARY) >= 0);
    }

    // ------------------------------------------------------------------------

    public boolean hasAttribute(String key)
    {
        return this.attr.hasProperty(key);
    }

    public boolean getBooleanAttribute(String key)
    {
        return this.attr.hasProperty(key)? this.attr.getBoolean(key,true) : false;
    }

    public String getStringAttribute(String key)
    {
        return this.attr.getString(key, null);
    }

    public String getTitle()
    {
        return this.attr.getString(ATTR_TITLE, this.getName()).replace('_', ' ');
    }

    public String getFormat()
    {
        return this.attr.getString(ATTR_FORMAT, null);
    }

    public boolean isEditable(int mode)
    {
        int edit = this.attr.getInt(ATTR_EDIT, EDIT_NEVER);
        return (mode <= edit);
    }

    // ------------------------------------------------------------------------

    public Object getResultSetValue(ResultSet rs)
        throws SQLException
    {
        String n = this.getName();
        Class jvc = this.getTypeClass();
        if (jvc == String.class) {
            return (rs != null)? rs.getString(n) : "";
        } else
        if ((jvc == Integer.class) || (jvc == Integer.TYPE)) {
            return new Integer((rs != null)? rs.getInt(n) : 0);
        } else
        if ((jvc == Long.class) || (jvc == Long.TYPE)) {
            return new Long((rs != null)? rs.getLong(n) : 0L);
        } else
        if ((jvc == Float.class) || (jvc == Float.TYPE)) {
            return new Float((rs != null)? rs.getFloat(n) : 0.0F);
        } else
        if ((jvc == Double.class) || (jvc == Double.TYPE)) {
            return new Double((rs != null)? rs.getDouble(n) : 0.0);
        } else
        if ((jvc == Boolean.class) || (jvc == Boolean.TYPE)) {
            return new Boolean((rs != null)? (rs.getInt(n) != 0) : false);
        } else
        if (DBFieldType.class.isAssignableFrom(jvc)) {
            if (this.javaClassConst == null) {
                try {
                    this.javaClassConst = jvc.getConstructor(new Class[] { ResultSet.class, String.class });
                } catch (Throwable t) { // NoSuchMethodException
                    Print.logError("Unable to obtain proper constructor: " + t);
                    return null;
                }
            }
            try {
                return this.javaClassConst.newInstance(new Object[] { rs, n });
            } catch (Throwable t) { // InstantiationException, etc.
                if (t instanceof SQLException) {
                    throw (SQLException)t; // re-throw SQLExceptions
                }
                Print.logError("Unable to instantiate: " + t);
                return null;
            }
        } else {
            Print.logError("Unsupported Java class: " + StringTools.className(jvc));
            return null;
        }
    }

    // ------------------------------------------------------------------------

    public Object getDefaultValue()
    {
        try {
            return this.getResultSetValue(null);
        } catch (SQLException sqe) {
            // this will(should) never occur
            return null;
        }
    }
    
    // ------------------------------------------------------------------------

    public boolean quoteValue()
    {
        if (this.isCLOB()) {
            return true;
        } else
        if (this.isBLOB()) {
            // This assumes that the value is presented in raw hex form.
            // If the value is presented in hex, quoting will procude invalid results.
            return true;
        } else {
            String t = this.getDataType().toUpperCase();
            return t.startsWith(TYPE_STRING);
        }
    }

    public String getQValue(Object v)
    {
        if (this.isBLOB()) {
            byte data[] = null;
            if (v == null) {
                data = new byte[0];
            } else
            if (v instanceof byte[]) {
                // this is the preferred Object type
                data = (byte[])v;
            } else
            if (v instanceof String) {
                String vs = (String)v;
                if (vs.equals("")) {
                    data = new byte[0];
                } else
                if (vs.startsWith("0x")) {
                    data = StringTools.parseHex(vs, null);
                    if (data == null) {
                        data = StringTools.getBytes(vs.toCharArray());
                    }
                } else {
                    data = StringTools.getBytes(vs.toCharArray());
                }
            } else {
                Print.logError("Unsupported BLOB object type: " + StringTools.className(v));
                String vs = v.toString(); // no trimming
                //Notes: Because of the current character encoding, using 'vs.getBytes()'
                //  may create a byte array with more elements that the original character 
                //  array.  Instead, we need to convert to a byte array char by char.
                //data = StringTools.getBytes(vs);
                data = StringTools.getBytes(vs.toCharArray());
            }
            return (data.length > 0)? ("0x" + StringTools.toHexString(data)) : "\"\"";
        } else {
            String vs = (v != null)? v.toString().trim() : "";
            return this.quoteValue() || vs.equals("")? StringTools.quoteString(vs) : vs;
        }
    }

    // ------------------------------------------------------------------------

    public boolean equals(Object other)
    {
        if (!(other instanceof DBField)) { return false; }
        DBField fld = (DBField)other;
        if (!this.getName().equals(fld.getName())) {
            return false;
        } else
        if (!this.getSqlType().equals(fld.getSqlType())) {
            return false;
        } else {
            return true;
        }
    }
    
    // ------------------------------------------------------------------------

    public String getFieldDefinition()
    {
        return this.name + " " + this.getSqlType();
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer(this.getFieldDefinition());
        if (this.isPriKey) { sb.append(" key"); }
        if (this.isAltKey) { sb.append(" altkey"); }
        return sb.toString();
    }

    // ------------------------------------------------------------------------

}
