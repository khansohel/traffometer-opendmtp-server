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
package org.opendmtp.server.base;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import org.opendmtp.util.*;

public class FletcherChecksum
{
    
    // ------------------------------------------------------------------------

    private int C[] = { 0, 0 };
    
    public FletcherChecksum()
    {
        this.reset();
    }
    
    // ------------------------------------------------------------------------

    public void reset()
    {
        this.C[0] = 0;
        this.C[1] = 0;
    }
    
    // ------------------------------------------------------------------------

    public int[] getValues()
    {
        int F[] = new int[2];
        F[0] = C[0] & 0xFF;
        F[1] = C[1] & 0xFF;
        return F;
    }
    
    // ------------------------------------------------------------------------
    
    public boolean isValid()
    {
        byte F[] = this.getChecksum();
        //Print.logDebug("F0=" + (F[0]&0xFF) + ", F1=" + (F[1]&0xFF));
        return (F[0] == 0) && (F[1] == 0);
    }

    public byte[] getChecksum()
    {
        byte F[] = new byte[2];
        F[0] = (byte)((C[0] - C[1])      & 0xFF);
        F[1] = (byte)((C[1] - (C[0]<<1)) & 0xFF);
        return F;
    }

    public void runningChecksum(byte b[])
    {
        if (b != null) {
            for (int i = 0; i < b.length; i++) {
                C[0] = C[0] + ((int)b[i] & 0xFF);
                C[1] = C[1] + C[0];
            }
        }
    }
    
    // ------------------------------------------------------------------------

}

