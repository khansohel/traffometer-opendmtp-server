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

import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.sql.*;

import org.opendmtp.util.*;

public class DBEdit
{

    // ------------------------------------------------------------------------

    private static final String LINE_SEPARATOR = "-----------------------------------------";
    
    // ------------------------------------------------------------------------

    private DBRecordKey recordKey = null;
    private DBField     editableFields[] = null;
    
    public DBEdit(DBRecordKey key)
    {
        this.recordKey = key;
        this.recordKey.getDBRecord(true);
    }
    
    public DBEdit(DBRecord rcd)
    {
        this(rcd.getRecordKey());
    }

    // ------------------------------------------------------------------------

    private DBField[] getEditableFields()
    {
        if (this.editableFields == null) {
            DBField fld[] = this.recordKey.getFields();
            java.util.List fldList = new Vector();
            for (int i = 0; i < fld.length; i++) {
                if (fld[i].isEditable(DBField.EDIT_ADMIN)) {
                    fldList.add(fld[i]);
                }
            }
            this.editableFields = (DBField[])fldList.toArray(new DBField[fldList.size()]);
        }
        return this.editableFields;
    }
    
    // ------------------------------------------------------------------------

    public void print() 
    {
        Print.logInfo("");
        Print.logInfo(LINE_SEPARATOR);
        DBField fld[] = this.getEditableFields();
        for (int i = 0; i < fld.length; i++) {
            String ndx   = StringTools.rightJustify(String.valueOf(i), 2);
            String title = StringTools.leftJustify(fld[i].getTitle(), 35);
            Object value = this.recordKey.getFieldValue(fld[i].getName());
            Print.logInfo(ndx + ") " + title + " : \"" + value + "\"");
        }
    }
    
    // ------------------------------------------------------------------------

    public boolean editField(DBField fld)
        throws IOException
    {
        String name = fld.getName();
        Class  type = fld.getTypeClass();
        
        /* header */
        Print.logInfo("");
        Print.logInfo(LINE_SEPARATOR);
        Print.logInfo("Field: " + name);
        Print.logInfo("Title: " + fld.getTitle());
        Print.logInfo("Type : " + type);
        Print.logInfo("Value: " + this.recordKey.getFieldValue(name));
        
        /* new value */
        for (;;) {
            Print.sysPrint("Enter new value: ");
            String line = FileTools.readLine_stdin().trim();
            if (line.equals("")) {
                return false;
            } else
            if (type == String.class) {
                String val = line;
                this.recordKey.setFieldValue(name, val);
                return true;
            } else
            if ((type == Integer.class) || (type == Integer.TYPE)) {
                int val = (int)StringTools.parseLong(line, Integer.MIN_VALUE);
                if (val != Integer.MIN_VALUE) {
                    this.recordKey.setFieldValue(name, val);
                    return true;
                }
            } else
            if ((type == Long.class) || (type == Long.TYPE)) {
                long val = StringTools.parseLong(line, Long.MIN_VALUE);
                if (val != Long.MIN_VALUE) {
                    this.recordKey.setFieldValue(name, val);
                    return true;
                }
            } else
            if ((type == Float.class) || (type == Float.TYPE)) {
                float val = (float)StringTools.parseDouble(line, Float.MIN_VALUE);
                if (val != Float.MIN_VALUE) {
                    this.recordKey.setFieldValue(name, val);
                    return true;
                }
            } else
            if ((type == Double.class) || (type == Double.TYPE)) {
                double val = StringTools.parseDouble(line, Double.MIN_VALUE);
                if (val != Double.MIN_VALUE) {
                    this.recordKey.setFieldValue(name, val);
                    return true;
                }
            } else
            if ((type == Boolean.class) || (type == Boolean.TYPE)) {
                if (StringTools.isBoolean(line)) {
                    boolean val = StringTools.parseBoolean(line, false);
                    this.recordKey.setFieldValue(name, val);
                    return true;
                }
            } else
            if (DBFieldType.class.isAssignableFrom(type)) {
                try {
                    Constructor typeConst = type.getConstructor(new Class[] { String.class });
                    Object val = typeConst.newInstance(new Object[] { line });
                    this.recordKey.setFieldValue(name, val);
                    return true;
                } catch (Throwable t) { // NoSuchMethodException
                    Print.logError("ERROR: Unable to parse this field type");
                    return false;
                }
            } else {
                Print.logError("ERROR: Unable to parse this field type");
                return false;
            }
            Print.logError("Entered value is improper type");
        }
        
    }
    
    public boolean edit()
        throws IOException
    {
        Print.logInfo("");
        Print.logInfo(LINE_SEPARATOR);
        Print.logInfo("Editing table: " + this.recordKey.getTableName());
        Print.logInfo("Record Key   : " + this.recordKey);
        Print.logInfo(LINE_SEPARATOR);
        Print.logInfo("Commands:");
        Print.logInfo("   ##   - Field number to edit");
        Print.logInfo("   save - Save changes and exit");
        Print.logInfo("   exit - Exit without saving changes");
        DBField fld[] = this.getEditableFields();
        for (;;) {
            
            /* field prompt */
            Print.logInfo("");
            this.print();
            Print.sysPrint("Enter field number [or 'save','exit']: ");
            String line = FileTools.readLine_stdin().trim();
            if (line.equals("")) {
                continue;
            }
            
            /* commands */
            if (line.equalsIgnoreCase("exit")) {
                Print.logInfo("\nExiting, record not saved");
                Print.logInfo("");
                return false;
            } else
            if (line.equalsIgnoreCase("save")) {
                DBRecord rcd = this.recordKey.getDBRecord();
                try {
                    rcd.save();
                    Print.logInfo("\nRecord saved");
                    Print.logInfo("");
                    return true;
                } catch (DBException dbe) {
                    Print.logError("\nERROR: Unable to save record!");
                    dbe.printException();
                    Print.logError("");
                    return false;
                }
            }
            
            /* selected field index */
            long fldNdx = StringTools.parseLong(line, -1L);
            if ((fldNdx < 0L) || (fldNdx >= fld.length)) {
                continue;
            }
            
            /* edit field */
            this.editField(fld[(int)fldNdx]);

        }
    }

    // ------------------------------------------------------------------------

}
