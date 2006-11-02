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

import java.net.DatagramSocket;

import org.opendmtp.server.db.AccountDB;
import org.opendmtp.server.db.DeviceDB;
import org.opendmtp.util.Print;
import org.opendmtp.util.ServerSocketThread;

/**
 * DMTPServer stores threads of UDP and TCP from a given port. Also contains the DBFactory 
 * interface.
 * 
 * @author Martin D. Flynn
 * @author Brandon Lee
 */
public class DMTPServer {

  /**
   * Private static variable for a singleton pattern.
   */
  private static DMTPServer trackTcpInstance = null;

  /** 
   * Creates a new DMTPServer from trackTcpInstance if not null.
   * 
   * @param port contains the port number.
   * @return trackTcpInstace a new DMTPSever intance of the port.
   * @throws Throwable if there are errors in creating threads.
   */
  public static DMTPServer createTrackSocketHandler(int port) throws Throwable {
    
    if (trackTcpInstance == null) {
      
      trackTcpInstance = new DMTPServer(port);
    }
    return trackTcpInstance;
  }

  
  /** 
   * The DBFactory instance.
   */
  private static DMTPServer.DBFactory dbFactory = null;

  /** 
   * The DBFactory interface.
   */
  public interface DBFactory {
    
    /**
     * Gets accountDB specified.
     * 
     * @param acctName the string that contains the accountDB.
     * @return the accountDB of account name.
     */
    AccountDB getAccountDB(String acctName);

    /**
     * Returns the deviceDB.
     * 
     * @param uniqId the ID of the device.
     * @return the Device specified.
     */
    DeviceDB getDeviceDB(long uniqId);

    /**
     * Returns the DeviceDB of the accountId and device name.
     * 
     * @param acctId the accounts ID.
     * @param devName the name of the device.
     * @return the deviceDB.
     */
    DeviceDB getDeviceDB(String acctId, String devName);
  }

  /** 
   * Sets the DBFactory for the server.
   * 
   * @param factory the factory to add.
   */
  public static void setDBFactory(DMTPServer.DBFactory factory) {
    DMTPServer.dbFactory = factory;
  }

  /** 
   * Returns the DBFactory.
   * 
   * @return the current DBFactory.
   */
  public static DMTPServer.DBFactory getDBFactory() {
    return DMTPServer.dbFactory;
  }

  // ------------------------------------------------------------------------

  /** 
   * The tcp thread.
   */
  private ServerSocketThread tcpThread = null;
  /** 
   * The udp thread.
   */
  private ServerSocketThread udpThread = null;

  /** 
   * Constructor.  Takes a port calls the startups of TCP and UDP.
   * 
   * @param port the port number.
   * @throws Throwable if error from methods.
   */
  private DMTPServer(int port) throws Throwable {
    this.startTCP(port);
    this.startUDP(port);
  }

  /** 
   * Creates a new thread with port number, intilizes it, starts it up and assigns to instance.
   * 
   * @param port and int with the port number.
   * @throws Throwable if unable to create thread from port.
   */
  private void startTCP(int port) throws Throwable {
    ServerSocketThread sst = null;

    // create server socket 
    try {
      sst = new ServerSocketThread(port);
    }
    catch (Throwable t) { // trap any server exception
      Print.logException("ServerSocket error", t);
      throw t;
    }

    // initialize
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

    // start thread 
    Print.logInfo("DMTP: Starting TCP listener thread on port " + port + " ...");
    sst.start();
    this.tcpThread = sst;
  }

  /** 
   * Startup of a thread with datagramSocket of specified port, and assigns it to instance.
   * 
   * @param port the port number.
   * @throws Throwable if error occurs in creating a thread.
   */
  private void startUDP(int port) throws Throwable {
    ServerSocketThread sst = null;

    // create server socket 
    try {
      sst = new ServerSocketThread(new DatagramSocket(port));
    }
    catch (Throwable t) { // trap any server exception
      Print.logException("ServerSocket error", t);
      throw t;
    }

    // initialize 
    sst.setTextPackets(false);
    sst.setBackspaceChar(null); // no backspaces allowed
    sst.setLineTerminatorChar(new int[] { '\r' });
    sst.setMaximumPacketLength(600);
    sst.setMinimumPacketLength(Packet.MIN_HEADER_LENGTH);
    sst.setIdleTimeout(4000L);
    sst.setPacketTimeout(1000L);
    sst.setSessionTimeout(60000L);
    sst.setTerminateOnTimeout(true);

    // session timeout 
    // This should be AccountID dependent
    sst.setClientPacketHandlerClass(DMTPClientPacketHandler.class);

    // start thread 
    Print.logInfo("DMTP: Starting UDP listener thread on port " + port + " ...");
    sst.start();
    this.udpThread = sst;

  }
}
