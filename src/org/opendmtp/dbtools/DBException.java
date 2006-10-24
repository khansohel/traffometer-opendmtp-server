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
//  2006/04/08  Martin D. Flynn
//      Initial release
//  2006/04/23  Martin D. Flynn
//      Integrated logging changes made to Print
// ----------------------------------------------------------------------------
package org.opendmtp.dbtools;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.opendmtp.util.Print;

public class DBException
    extends Exception
{
    
    // ----------------------------------------------------------------------------

    public DBException(String msg)
    {
        super(msg);
    }

    public DBException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
    
    // ----------------------------------------------------------------------------

    public boolean isSQLException()
    {
        return (this.getCause() instanceof SQLException);
    }
    
    // ----------------------------------------------------------------------------

    public void printException()
    {
        Throwable cause = this.getCause();
        if (cause instanceof SQLException) {
            Print.logSQLError(1, this.getMessage(), (SQLException)cause);
        } else {
            Print.logException(this.getMessage(), this);
        }
    }
    
    // ----------------------------------------------------------------------------

    /*
    public void printStackTrace()
    {
        Throwable cause = this.getCause();
        if (cause instanceof SQLException) {
            Print.logStackTrace(cause);
        } else {
            super.printStackTrace();
        }
    }
    */
    
}
