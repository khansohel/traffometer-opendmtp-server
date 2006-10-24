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
import org.opendmtp.server.db.*;

public class DMTPServer
{
     
    // ------------------------------------------------------------------------
    
    private static DMTPServer trackTcpInstance = null;
    
    public static DMTPServer createTrackSocketHandler(int port)
        throws Throwable
    {
        if (trackTcpInstance == null) {
            trackTcpInstance = new DMTPServer(port);
        }
        return trackTcpInstance;
    }
    
    // ------------------------------------------------------------------------
    
    private static DMTPServer.DBFactory dbFactory = null;

    public interface DBFactory
    {
        AccountDB getAccountDB(String acctName);
        DeviceDB  getDeviceDB(long uniqId);
        DeviceDB  getDeviceDB(String acctId, String devName);
    }
    
    public static void setDBFactory(DMTPServer.DBFactory factory)
    {
        DMTPServer.dbFactory = factory;
    }
    
    public static DMTPServer.DBFactory getDBFactory()
    {
        return DMTPServer.dbFactory;
    }

    // ------------------------------------------------------------------------

    private ServerSocketThread tcpThread = null;
    private ServerSocketThread udpThread = null;

    private DMTPServer(int port)
        throws Throwable
    {
        this.startTCP(port);
        this.startUDP(port);
    }
    
    private void startTCP(int port)
        throws Throwable
    {
        ServerSocketThread sst = null;
        
        /* create server socket */
        try {
            sst = new ServerSocketThread(port);
        } catch (Throwable t) { // trap any server exception
            Print.logException("ServerSocket error", t);
            throw t;
        }
        
        /* initialize */
        sst.setTextPackets(false);
        sst.setBackspaceChar(null); // no backspaces allowed
        sst.setLineTerminatorChar(new int[] { '\r' });
        sst.setMaximumPacketLength(600);
        sst.setMinimumPacketLength(Packet.MIN_HEADER_LENGTH);
        sst.setIdleTimeout(4000L);
        sst.setPacketTimeout(1000L);
        sst.setSessionTimeout(5000L);
        sst.setLingerTimeoutSec(5);
        sst.setTerminateOnTimeout(true);
        sst.setClientPacketHandlerClass(DMTPClientPacketHandler.class);

        /* start thread */
        Print.logInfo("DMTP: Starting TCP listener thread on port " + port + " ...");
        sst.start();
        this.tcpThread = sst;

    }

    private void startUDP(int port)
        throws Throwable
    {
        ServerSocketThread sst = null;

        /* create server socket */
        try {
            sst = new ServerSocketThread(new DatagramSocket(port));
        } catch (Throwable t) { // trap any server exception
            Print.logException("ServerSocket error", t);
            throw t;
        }
        
        /* initialize */
        sst.setTextPackets(false);
        sst.setBackspaceChar(null); // no backspaces allowed
        sst.setLineTerminatorChar(new int[] { '\r' });
        sst.setMaximumPacketLength(600);
        sst.setMinimumPacketLength(Packet.MIN_HEADER_LENGTH);
        sst.setIdleTimeout(4000L);
        sst.setPacketTimeout(1000L);
        sst.setSessionTimeout(60000L);
        sst.setTerminateOnTimeout(true);
        
        /* session timeout */
        // This should be AccountID dependent
        sst.setClientPacketHandlerClass(DMTPClientPacketHandler.class);

        /* start thread */
        Print.logInfo("DMTP: Starting UDP listener thread on port " + port + " ...");
        sst.start();
        this.udpThread = sst;

    }
    
    // ------------------------------------------------------------------------
        
}
