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
import java.lang.reflect.*;
import java.util.*;
import java.sql.*;

import org.opendmtp.util.*;

public abstract class DBFieldType
{

    // ------------------------------------------------------------------------

    public DBFieldType()
    {
        super();
    }

    public DBFieldType(String val)
    {
        this();
    }

    public DBFieldType(ResultSet rs, String fldName)
        throws SQLException
    {
        this();
        // override (NOTE: 'rs' may be null!)
    }
    
    // ------------------------------------------------------------------------

    public abstract String toString();
    
}
