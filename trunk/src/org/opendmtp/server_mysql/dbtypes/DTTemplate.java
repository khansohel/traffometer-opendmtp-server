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
package org.opendmtp.server_mysql.dbtypes;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opendmtp.util.*;

import org.opendmtp.dbtools.*;

public class DTTemplate
    extends DBFieldType
{

    // ------------------------------------------------------------------------
    //  boolean hiRes   = false; // 0..1
    //  int     fldType = -1;    // 0..128
    //  int     fldNdx  = 0;     // 0..255
    //  int     fldLen  = 0;     // 0..255
    // "0=H|23|0|3

    // ------------------------------------------------------------------------
    
    private RTProperties templateProps = null;
    
    public DTTemplate()
    {
        super();
        this.templateProps = new RTProperties("");
    }
    
    public DTTemplate(String template)
    {
        super(template);
        this.templateProps = new RTProperties((template != null)? template : "");
    }

    public DTTemplate(ResultSet rs, String fldName)
        throws SQLException
    {
        super(rs, fldName);
        // set to default value if 'rs' is null
        this.templateProps = new RTProperties((rs != null)? rs.getString(fldName) : "");
    }

    public String toString()
    {
        return this.templateProps.toString();
    }

    // ------------------------------------------------------------------------
    // "#=<type>|[H|L]|<index>|<length>"

    private static final char FIELD_VALUE_SEPARATOR = '|';
    
    public void clearFields()
    {
        this.templateProps.clearProperties();
    }
    
    public Field getField(int ndx)
    {
        String name = String.valueOf(ndx);
        String ftmp = this.templateProps.getString(name, null);
        return ((ftmp != null) && !ftmp.equals(""))? new Field(ftmp) : null;
    }
    
    public void setField(int ndx, Field fld)
    {
        String name = String.valueOf(ndx);
        this.templateProps.setString(name, fld.toString());
    }
    
    public static class Field
    {
        private int     type    = -1;
        private boolean isHiRes = false;
        private int     index   = 0;
        private int     length  = 0;
        public Field(int type, boolean hiRes, int ndx, int len) {
            this.type    = type;
            this.isHiRes = hiRes;
            this.index   = ndx;
            this.length  = len;
        }
        public Field(String s) {
            String f[] = StringTools.parseString(s,FIELD_VALUE_SEPARATOR);
            this.type    = (f.length > 0)? StringTools.parseInt(f[1],-1) : -1;
            this.isHiRes = (f.length > 1)? f[0].equalsIgnoreCase("H") : false;
            this.index   = (f.length > 2)? StringTools.parseInt(f[2], 0) :  0;
            this.length  = (f.length > 3)? StringTools.parseInt(f[3], 0) :  0;
        }
        public boolean isHiRes() {
            return this.isHiRes;
        }
        public int getType() {
            return this.type;
        }
        public int getIndex() {
            return this.index;
        }
        public int getLength() {
            return this.length;
        }
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(this.isHiRes()?"H":"L");
            sb.append(FIELD_VALUE_SEPARATOR);
            sb.append(this.getType());
            sb.append(FIELD_VALUE_SEPARATOR);
            sb.append(this.getIndex());
            sb.append(FIELD_VALUE_SEPARATOR);
            sb.append(this.getLength());
            return sb.toString();
        }
        public boolean equals(Object other) {
            if (other instanceof Field) {
                return this.toString().equals(other.toString());
            } else {
                return false;
            }
        }
    }
    
    // ------------------------------------------------------------------------

}
