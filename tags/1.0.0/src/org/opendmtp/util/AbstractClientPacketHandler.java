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
// Description:
//  Partial implementation of a ClientPacketHandler
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.net.InetAddress;

/**
 * Partial implementation of a ClientPacketHandler, to ease future implementations.
 * 
 * @author Martin D. Flynn
 * @author Robert S. Brewer
 */
public abstract class AbstractClientPacketHandler implements ClientPacketHandler {

  // ------------------------------------------------------------------------

  private InetAddress inetAddr = null;
  private boolean isTCP = true;
  private boolean isTextPackets = false;

  /**
   * Accessor for flag indicating packets are text.
   * 
   * @return true if packets are text, otherwise false.
   */
  protected boolean isTextPackets() {
    return this.isTextPackets;
  }

  // ------------------------------------------------------------------------

  /**
   * Called when new client session initiated, initializing fields with provided values.
   * 
   * @param inetAddr IP address of client.
   * @param isTCP flag indicating whether session is over TCP.
   * @param isText flag indicating whether session is text.
   */
  public void sessionStarted(InetAddress inetAddr, boolean isTCP, boolean isText) {
    this.inetAddr = inetAddr;
    this.isTCP = isTCP;
    this.isTextPackets = isText;
  }

  // ------------------------------------------------------------------------

  /**
   * Accessor for InetAddress field.
   * 
   * @return the InetAddress field value.
   */
  public InetAddress getInetAddress() {
    return this.inetAddr;
  }

  /**
   * Provides the raw IP address in text form.
   * 
   * @return the raw IP address in text form.
   */
  public String getHostAddress() {
    String ipAddr = (this.inetAddr != null) ? this.inetAddr.getHostAddress() : null;
    return ipAddr;
  }

  // ------------------------------------------------------------------------

  /**
   * Returns the actual length of a partial packet.
   *  
   * @param packet partial packet to be checked.
   * @param packetLen length of partial packet.
   * @return Actual packet length if packet is textual, otherwise -1.
   */
  public int getActualPacketLength(byte packet[], int packetLen) {
    return this.isTextPackets ? -1 : packetLen;
  }

  // ------------------------------------------------------------------------

  /**
   * Process packet and return response, the core of the packet handler.
   * 
   * @param cmd packet to be processed.
   * @return an array containing packet information for output.
   * @throws Exception if problem encountered while handling packet.
   */
  public abstract byte[] getHandlePacket(byte cmd[]) throws Exception;

  // ------------------------------------------------------------------------

  /**
   * Should return true to terminate session, and defaults to true.
   * 
   * @return true if session is to be terminated, otherwise false.
   */
  public boolean terminateSession() {
    return true;
  }

  /**
   * Called after client session is terminated.
   * 
   * @param err Possible error encountered in termination?
   * @param readCount Record of number of packets read?
   * @param writeCount Record of number of packets written?
   */
  public void sessionTerminated(Throwable err, long readCount, long writeCount) {
    // do nothing
  }

  // ------------------------------------------------------------------------

}
