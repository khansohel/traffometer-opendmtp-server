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
import org.opendmtp.codes.*;
import org.opendmtp.server.db.*;

public class PacketParseException
    extends Exception
{
    // ------------------------------------------------------------------------

    private Packet  packet      = null;
    private int     errorCode   = 0x0000;
    private byte    errorData[] = null;
    private boolean terminate   = false;

    // ------------------------------------------------------------------------

    public PacketParseException(int errCode, Packet packet) 
    {
        this(errCode, packet, null);
    }
    
    public PacketParseException(int errCode, Packet packet, byte errData[]) 
    {
        super();
        this.packet    = packet;
        this.errorCode = errCode;
        this.errorData = errData;
    }
    
    // ------------------------------------------------------------------------

    public Packet getPacket()
    {
        return this.packet;
    }

    public void setPacket(Packet pkt)
    {
        this.packet = pkt;
    }

    // ------------------------------------------------------------------------

    public int getErrorCode()
    {
        return this.errorCode;
    }
    
    public byte[] getErrorData() 
    {
        return this.errorData;
    }
    
    public Packet createServerErrorPacket()
    {
        int errCode    = this.getErrorCode();
        Packet cause   = this.getPacket();
        byte errData[] = this.getErrorData();
        Packet errPkt  = Packet.createServerErrorPacket(errCode, cause);
        if (errData != null) {
            // DO NOT RESET PAYLOAD INDEX!!!
            errPkt.getPayload(false).writeBytes(errData, errData.length);
        }
        return errPkt;
    }
    
    // ------------------------------------------------------------------------

    public void setTerminate()
    {
        this.terminate = true;
    }
    
    public boolean terminateSession()
    {
        return this.terminate;
    }
    
    // ------------------------------------------------------------------------

    public String toString() 
    {
        int errCode = this.getErrorCode();
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(StringTools.toHexString(errCode,16));
        sb.append("] ");
        sb.append(ServerErrors.getErrorDescription(errCode));
        return sb.toString();
    }
    
    // ------------------------------------------------------------------------

}
