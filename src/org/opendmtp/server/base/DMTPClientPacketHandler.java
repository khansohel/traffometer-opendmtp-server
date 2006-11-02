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

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.util.Vector;

import org.opendmtp.codes.ClientErrors;
import org.opendmtp.codes.Encoding;
import org.opendmtp.codes.ServerErrors;
import org.opendmtp.server.db.Payload;
import org.opendmtp.server.db.PayloadTemplate;
import org.opendmtp.util.AbstractClientPacketHandler;
import org.opendmtp.util.ListTools;
import org.opendmtp.util.Print;
import org.opendmtp.util.StringTools;



/**
 * Handles packet parsing, checking, loging, and handling of errors for packets and connections.
 * 
 * @author Martin D. Flynn
 * @author Brandon Lee
 */
public class DMTPClientPacketHandler extends AbstractClientPacketHandler {

  // ------------------------------------------------------------------------

  /**
   * not used.
   */
  private int dataNdx = 0; 
  /**
   * Specifieds session termination.
   */
  private boolean terminate = false;

  /**
   *  duplex/simplex.
   */
  private boolean isDuplex = true;

  /**
   *  session IP address and possibly host name.
   */
  private InetAddress inetAddress = null;
  /**
   * session's raw IP address.
   */
  private String ipAddress = null;

  /**
   * Represents a type of encoding.
   */
  private int encoding = Encoding.ENCODING_UNKNOWN;

  /** 
   * Fletcher checksum used to check packets.
   */
  private FletcherChecksum fletcher = null;

  /** 
   * The established identification of this account.
   */
  private AccountID accountId = null;
  /**
   * The ID of the device connected to.
   */
  private DeviceID deviceId = null;

  /**
   * total event count.
   */
  private int eventTotalCount = 0;
  /**
   * total block count.
   */
  private int eventBlockCount = 0;
  /**
   * last event that was valid.
   */
  private Event lastValidEvent = null;
  /**
   * event error packet.
   */
  private Packet eventErrorPacket = null;

  /**
   * Constructor. Calls super and create a new FletcherChecksum instance.
   */
  public DMTPClientPacketHandler() {
    super();
    this.fletcher = new FletcherChecksum();
  }

  /**
   * Logs Start of session, establishes IP address. 
   * Variable isText never used.
   * 
   * @param inetAddr the raw internet address with host.
   * @param isTCP true if using TCP.
   * @param isText never used
   */
  public void sessionStarted(InetAddress inetAddr, boolean isTCP, boolean isText) {
    
    this.inetAddress = inetAddr;
    this.ipAddress = (inetAddr != null) ? inetAddr.getHostAddress() : null;
    this.isDuplex = isTCP;
    
    if (this.isDuplex) {
      Print.logInfo("Begin Duplex communication: " + this.ipAddress);
    }
    else {
      Print.logInfo("Begin Simplex communication: " + this.ipAddress);
    } //end else
  }

  /**
   * Logs the end of a session. This method uses none of its parameters.
   * 
   * @param err doesn't use in method.
   * @param readCount doesn't used in method.
   * @param writeCount also not used.
   */
  public void sessionTerminated(Throwable err, long readCount, long writeCount) {
    
    // called before the socket is closed
    if (this.isDuplex()) {
      
      Print.logInfo("End Duplex communication: " + this.ipAddress);
      
      // short pause to 'help' make sure the pending outbound data is transmitted
      try {
        Thread.sleep(50L);
      }
      catch (Throwable t) {
      }
    }
    else {
      Print.logInfo("End Simplex communication: " + this.ipAddress);
    }
  }

  /**
   * Returns a if session is a duplex comunication or not.
   * 
   * @return true if it is a duplex connection else false.
   */
  public boolean isDuplex() {
    return this.isDuplex;
  }

  /**
   * Retrives the length of the packet with header.
   * 
   * @param packet an array containing the packet.
   * @param packetLen an int with packet size with out header.
   * @return -1, 0 if no good, or actual size.
   */
  public int getActualPacketLength(byte packet[], int packetLen) {
    
    if ((packetLen >= 1) && (packet[0] == Encoding.AsciiEncodingChar)) {
      // look for line terminator
      return -1;
    }
    else if (packetLen >= Packet.MIN_HEADER_LENGTH) {
      
      int payloadLen = (int) packet[2] & 0xFF;
      return Packet.MIN_HEADER_LENGTH + payloadLen;
    }
    else {
      
      // this should not occur, since minimum length has been set above
      return 0;
    }
  }

  /**
   * Returns encoded packet informations, response is discarded if simplex connection.
   * 
   * @param pktBytes the packet in a byte array.
   * @return byte[] an array containing packet info for output.
   */
  public byte[] getHandlePacket(byte pktBytes[]) {
    
    String ipAddr = this.getHostAddress();
    Packet resp[] = this._parsePacket(ipAddr, pktBytes);
    
    if ((resp == null) || (resp.length == 0)) {
      
      // Print.logWarn("<-- null (no response)");
      return null;
    }
    else if (this.isDuplex()) {
      
      if (resp.length == 1) {
        
        Print.logDebug("==> " + resp[0].toString(this.encoding));
        return resp[0].encode(this.encoding);
      }
      else {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        for (int i = 0; i < resp.length; i++) {
          
          Print.logDebug("==> " + resp[i].toString(this.encoding));
          byte b[] = resp[i].encode(this.encoding);
          baos.write(b, 0, b.length);
        }
        
        return baos.toByteArray();
      } //end if resp
    } //end if isDup
    else {
      Print.logError("Response discarded due to Simplex communication");
      return null;
    }
  }

  /**
   * Returns wheather session is terminated or not.
   * 
   * @return returns if session is terminated or not.
   */
  public boolean terminateSession() {
    return this.terminate;
  }

  /**
   * Loads Unique ID into DeviceID and calls _setAccountID and _setDeviceId to set account ID
   * and device ID.
   * 
   * @param id used to create a unique ID.
   * @throws PacketParseException if device is null or unique ID isn't valid.
   */
  private void loadUniqueID(byte id[]) throws PacketParseException {

    // device already defined? 
    if (this.deviceId != null) {
      Print.logError("Device ID already defined");
      throw new PacketParseException(ServerErrors.NAK_PROTOCOL_ERROR, null); // errData ok
    }

    // invalid id specified? 
    UniqueID uniqId = new UniqueID(id);
    if (!uniqId.isValid()) {
      throw new PacketParseException(ServerErrors.NAK_ID_INVALID, null); // errData ok
    }

    // load device throws PacketParseException if named DeviceID does not exist
    DeviceID devId = DeviceID.loadDeviceID(uniqId);

    // set account/device both can throw PacketParseExceptions
    this._setAccountId(devId.getAccountID());
    this._setDeviceId(devId);
  }

  /**
   * Checks on param input and if AccountID exists. If nothing is wrong calls _setAccountId with the
   * AccountID of the string given.
   * 
   * @param acctName The name of the account ID to load.
   * @throws PacketParseException Thrown if string given is null, empty, or account doesn't exist.
   */
  private void loadAccountId(String acctName) throws PacketParseException {

    // account already defined? 
    if (this.accountId != null) {
      Print.logError("Account ID already defined");
      throw new PacketParseException(ServerErrors.NAK_PROTOCOL_ERROR, null); // errData ok
    }
    else if ((acctName == null) || acctName.equals("")) {
      Print.logError("Account name is null/empty");
      throw new PacketParseException(ServerErrors.NAK_ACCOUNT_INVALID, null); // errData ok
    }

    // load account
    AccountID acctId = AccountID.loadAccountID(acctName.toLowerCase());
    // will throw PacketParseException if named AccountID does not exist

    // set account
    this._setAccountId(acctId);

  }

  /**
   * Sets the accountId to instance.
   * 
   * @param acctId the accountID to be set.
   * @throws PacketParseException if the account of acctId is null or if account isn't active.
   */
  private void _setAccountId(AccountID acctId) throws PacketParseException {

    if (acctId == null) {
      throw new PacketParseException(ServerErrors.NAK_ACCOUNT_INVALID, null); // errData ok
    }

    // validate account 
    if (!acctId.isActive()) {
      throw new PacketParseException(ServerErrors.NAK_ACCOUNT_INACTIVE, null); // errData ok
    }
    this.accountId = acctId;

  }

  /**
   * Returns the accountId.  If null creates a new exception setting the exception terminate boolean
   * then throw it and returns null.
   * 
   * @return AccountID the accountId of this instance or null.
   * @throws PacketParseException if account ID is null.
   */
  private AccountID getAccountId() throws PacketParseException {
    
    if (this.accountId == null) {
      // throw termination error if the account hasn't been defined
      PacketParseException ppe = new PacketParseException(ServerErrors.NAK_ACCOUNT_INVALID, null); 
      
      ppe.setTerminate();
      throw ppe;
    }
    return this.accountId;
  }

  /**
   * Checks on param input and if device exists. If nothing is wrong calls _setDeviceId with the 
   * device. This method is almost exactly like loadAccountID.
   * 
   * @param devName contains the devices name.
   * @throws PacketParseException if device is defined or given string is null or empty.
   */
  private void loadDeviceId(String devName) throws PacketParseException {

    // device already defined? 
    if (this.deviceId != null) {
      
      Print.logError("Device ID already defined");
      throw new PacketParseException(ServerErrors.NAK_PROTOCOL_ERROR, null); // errData ok
    }
    else if ((devName == null) || devName.equals("")) {
      
      Print.logError("Device name is null/empty");
      throw new PacketParseException(ServerErrors.NAK_DEVICE_INVALID, null); // errData ok
    }

    // load device, throws PacketParseException if named DeviceID does not exist
    DeviceID devId = DeviceID.loadDeviceID(this.getAccountId(), devName.toLowerCase());


    // set device
    this._setDeviceId(devId);
  }

  /**
   * Validates DeviceID given, if ok sets DeviceID to instance. 
   * 
   * @param devId the device ID canidate.
   * @throws PacketParseException throws if device ID is null, inActive or is a duplex connection.
   */
  private void _setDeviceId(DeviceID devId) throws PacketParseException {


    if (devId == null) {
      Print.logError("Device ID is null");
      throw new PacketParseException(ServerErrors.NAK_DEVICE_INVALID, null); // errData ok
    }

    // validate device 
    if (!devId.isActive()) {
      // device is not active
      Print.logError("Device is inactive: " + devId.getDeviceName());
      throw new PacketParseException(ServerErrors.NAK_DEVICE_INACTIVE, null); // errData ok
    }
    else if (!devId.markAndValidateConnection(this.isDuplex())) {
      // DMT service provider refuses connection
      Print.logError("Excessive connections: " + devId.getDeviceName());
      throw new PacketParseException(ServerErrors.NAK_EXCESSIVE_CONNECTIONS, null); // errData ok
    }
    
    this.deviceId = devId;
    // this.deviceId.save();

  }

  /**
   * Returns a DeviceID or null.
   * @return this.deviceId the device connected to.
   * @throws PacketParseException if deviceId is null.
   */
  private DeviceID getDeviceId() throws PacketParseException {
    
    if (this.deviceId == null) {
      // throw termination error if the device hasn't been defined
      PacketParseException ppe = new PacketParseException(ServerErrors.NAK_DEVICE_INVALID, null); 
                                                                                                  
      ppe.setTerminate();
      throw ppe;
    }
    return this.deviceId;
  }

  /**
   * Handles Packet errors.  This method looks like it could alter the encoding but will
   * always return null.
   * 
   * @param packet the packet to handle errors.
   * @return Packet will always be null.
   * @throws PacketParseException Don't know how its thrown.
   */
  private Packet[] _handleError(Packet packet) throws PacketParseException {
    
    Payload payload = packet.getPayload(true);
    int errCode = (int) payload.readULong(2, 0L);

    // handle specific error 
    switch (errCode) {

      case ClientErrors.ERROR_PACKET_HEADER:
      case ClientErrors.ERROR_PACKET_TYPE:
      case ClientErrors.ERROR_PACKET_LENGTH: {
        // we must have sent the client an invalid packet
        return null;
      }
  
      case ClientErrors.ERROR_PACKET_ENCODING: {
        // only occurs if client does not support CSV encoding
        this.getDeviceId().removeEncoding(this.encoding);
        
        if (Encoding.IsEncodingAscii(this.encoding)) {
         this.encoding = Encoding.IsEncodingChecksum(this.encoding) ? Encoding.ENCODING_BASE64_CKSUM
              : Encoding.ENCODING_BASE64;
        }
        else {
          // ignore this
        }
        return null;
      }
  
      case ClientErrors.ERROR_PACKET_PAYLOAD: {
        // we must have sent the client an invalid packet
        return null;
      }
  
      case ClientErrors.ERROR_PACKET_CHECKSUM: {
        // should try again?
        return null;
      }
  
      case ClientErrors.ERROR_PACKET_ACK: {
        // we must have sent the client an ack for a sequence it doesn't have
        return null;
      }
  
      case ClientErrors.ERROR_PROTOCOL_ERROR: {
        // we must have sent the client an invalid packet
        return null;
      }
  
      case ClientErrors.ERROR_PROPERTY_READ_ONLY: {
        // just note this somewhere
        return null;
      }
  
      case ClientErrors.ERROR_PROPERTY_WRITE_ONLY: {
        // just note this somewhere
        return null;
      }
  
      case ClientErrors.ERROR_PROPERTY_INVALID_ID: {
        // just note this somewhere
        return null;
      }
  
      case ClientErrors.ERROR_PROPERTY_INVALID_VALUE: {
        // just note this somewhere
        return null;
      }
  
      case ClientErrors.ERROR_PROPERTY_UNKNOWN_ERROR: {
        // ignore this, since we don't know what to do about this anyway
        return null;
      }
  
      case ClientErrors.ERROR_COMMAND_INVALID:
      case ClientErrors.ERROR_COMMAND_ERROR: {
        // ignore this, since we don't know what to do about this anyway
        return null;
      }
  
      case ClientErrors.ERROR_UPLOAD_TYPE:
      case ClientErrors.ERROR_UPLOAD_LENGTH:
      case ClientErrors.ERROR_UPLOAD_OFFSET_OVERLAP:
      case ClientErrors.ERROR_UPLOAD_OFFSET_GAP:
      case ClientErrors.ERROR_UPLOAD_OFFSET_OVERFLOW:
      case ClientErrors.ERROR_UPLOAD_FILE_NAME:
      case ClientErrors.ERROR_UPLOAD_CHECKSUM:
      case ClientErrors.ERROR_UPLOAD_SAVE: {
        // we should abort the upload here
        return null;
      }
  
      case ClientErrors.ERROR_GPS_EXPIRED:
      case ClientErrors.ERROR_GPS_FAILURE: {
        // pass these along to account owner
        return null;
      }
  
      default: {
        // pass these along to account owner
        return null;
      }
    } //end switch
  }

  /**
   * Logs event and saves it in DeviceID.
   * 
   * @param event the event object to save.
   * @return int returns the number of events that occured.
   * @throws PacketParseException if deviceID is null.
   */
  private int _handleEvent(Event event) throws PacketParseException {
    
    Print.logDebug(event.toString());
    return this.getDeviceId().saveEvent(event);
  }

  /**
   * Not sure but I think its checking packets for errors and handles the different kinds of 
   * packets accordingly then if all is ok returns an array of packet events. Can return null.
   * 
   * Probably should be broken into smaller methods for clarity.
   * 
   * @param packet the packet to check.
   * @return Packet an array of packet events. Also can be null.
   * @throws PacketParseException if packet length or checksum proves invalid.
   */
  private Packet[] _handlePacket(Packet packet) throws PacketParseException {

    // make sure we have a defined device
    if (!packet.isIdentType()) {
      // we must have a Device ID for anything other that an identification
      // type packet. The following attempts to get the device id and throws
      // an exception if the device has not yet been defined.
      this.getDeviceId();
    }

    // handle event packets separately 
    if (packet.isEventType()) {
      
      Event evData = new Event(packet);
      this.eventTotalCount++;
      this.eventBlockCount++;
      
      if (this.eventErrorPacket == null) {
        
        // no errors received during this block, so far
        int err = this._handleEvent(evData);
        
        if (err == ServerErrors.NAK_OK) {
          // this event insertion was successful
          this.lastValidEvent = evData;
        }
        else if (err == ServerErrors.NAK_DUPLICATE_EVENT) {
          // this record already exists (not a critical error)
          // duplicate events are quietly ignored
          this.lastValidEvent = evData;
        }
        else {
          // A critical error occurred inserting this event.
          // One of the following:
          // ServerErrors.NAK_EXCESSIVE_EVENTS
          // ServerErrors.NAK_EVENT_ERROR
          Print.logError("Event insertion [" + StringTools.toHexString(err, 16) + "] "
              + ServerErrors.getErrorDescription(err));
          long seq = evData.getSequence();
          int seqLen = evData.getSequenceLength();
          PacketParseException ppe = null;
          
          if ((seq >= 0L) && (seqLen > 0)) {
            
            Payload p = new Payload();
            p.writeULong(seq, seqLen);
            byte errData[] = p.getBytes();
            ppe = new PacketParseException(err, packet, errData); // sequence
          }
          else {
            ppe = new PacketParseException(err, packet); // errData ok
          }
          this.eventErrorPacket = ppe.createServerErrorPacket();
        }
      }
      else {
        // ignore this event
      }
      return null;
    }

    // handle specific packet type
    Payload payload = packet.getPayload(true);
    
    switch (packet.getPacketType()) {
  
      case Packet.PKT_CLIENT_EOB_DONE:
      case Packet.PKT_CLIENT_EOB_MORE: {
        // send responses, or close
        boolean hasMore = (packet.getPacketType() == Packet.PKT_CLIENT_EOB_MORE);
        
        java.util.List resp = new Vector();
        // check checksum (if binary encoding)
        if (this.encoding == Encoding.ENCODING_BINARY) {
          
          if (packet.getPayloadLength() == 0) {
            // checksum not specified
            // Print.logInfo("Client checksum not specified");
          }
          else if (packet.getPayloadLength() == 2) {
            
            if (!this.fletcher.isValid()) {
              
              Print.logError("Fletcher checksum is INVALID!!");
              throw new PacketParseException(ServerErrors.NAK_BLOCK_CHECKSUM, packet); // errData ok
            }
          }
          else {
            throw new PacketParseException(ServerErrors.NAK_PACKET_PAYLOAD, packet); // errData ok
          }
        }
        this.fletcher.reset();
        // acknowledge sent events
        if (this.lastValidEvent != null) {
          // at least 1 event has been received
          Packet ackPkt = Packet.createServerPacket(Packet.PKT_SERVER_ACK);
          int seqLen = this.lastValidEvent.getSequenceLength();
          
          if (seqLen > 0) {
            long seq = this.lastValidEvent.getSequence();
            ackPkt.getPayload(true).writeLong(seq, seqLen);
          }
          resp.add(ackPkt);
          this.eventBlockCount = 0;
          this.lastValidEvent = null;
        }
        // send any event parsing error packet
        if (this.eventErrorPacket != null) {
          resp.add(this.eventErrorPacket);
          this.eventErrorPacket = null;
        }
        // end-of-block / end-of-transmission
        if (hasMore) {
          Packet eobPkt = Packet.createServerPacket(Packet.PKT_SERVER_EOB_DONE);
          resp.add(eobPkt);
        }
        else {
          Packet eotPkt = Packet.createServerPacket(Packet.PKT_SERVER_EOT);
          resp.add(eotPkt);
          this.terminate = true;
        }
        return (Packet[]) ListTools.toArray(resp, Packet.class);
      }
  
      case Packet.PKT_CLIENT_UNIQUE_ID: {
        // lookup unique id
        try {
          byte id[] = payload.readBytes(6);
          this.loadUniqueID(id);
        }
        catch (PacketParseException ppe) {
          ppe.setTerminate();
          throw ppe;
        }
        break;
      }
  
      case Packet.PKT_CLIENT_ACCOUNT_ID: {
        // lookup account
        try {
          String acctId = payload.readString(20);
          this.loadAccountId(acctId);
        }
        catch (PacketParseException ppe) {
          ppe.setTerminate();
          throw ppe;
        }
        break;
      }
  
      case Packet.PKT_CLIENT_DEVICE_ID: {
        // lookup account/device
        try {
          String devId = payload.readString(20);
          this.loadDeviceId(devId);
        }
        catch (PacketParseException ppe) {
          ppe.setTerminate();
          throw ppe;
        }
        break;
      }
  
      case Packet.PKT_CLIENT_PROPERTY_VALUE: {
        int propKey = (int) payload.readULong(2);
        byte propVal[] = payload.readBytes(255);
        this.getDeviceId().logProperty(propKey, propVal);
        break;
      }
  
      case Packet.PKT_CLIENT_FORMAT_DEF_24: {
        // validate type
        int custType = (int) payload.readULong(1);
        Payload p = new Payload();
        p.writeULong(custType, 1);
        if (!Packet.isCustomEventType(custType)) {
          byte errData[] = p.getBytes();
          throw new PacketParseException(ServerErrors.NAK_FORMAT_DEFINITION_INVALID, packet, errData); // formatType
        }
        // validate that the payload size can accomodate the specified number of fields
        int numFlds = (int) payload.readULong(1);
        if (!payload.isValidLength(numFlds * 3)) {
          // not enough data to fill all specified field templates
          byte errData[] = p.getBytes();
          throw new PacketParseException(ServerErrors.NAK_FORMAT_DEFINITION_INVALID, packet, errData); // formatType
        }
        // parse field templates
        PayloadTemplate.Field field[] = new PayloadTemplate.Field[numFlds];
        int accumLen = 0;
        for (int i = 0; i < field.length; i++) {
          long fldMask = payload.readULong(3);
          field[i] = new PayloadTemplate.Field(fldMask);
          if (!field[i].isValidType()) {
            byte errData[] = p.getBytes();
            throw new PacketParseException(ServerErrors.NAK_FORMAT_DEFINITION_INVALID, packet,
                errData); // formatType
          }
          accumLen += field[i].getLength();
          if (accumLen > Packet.MAX_PAYLOAD_LENGTH) {
            byte errData[] = p.getBytes();
            throw new PacketParseException(ServerErrors.NAK_FORMAT_DEFINITION_INVALID, packet,
                errData); // formatType
          }
        }
        PayloadTemplate payloadTemp = new PayloadTemplate(custType, field);
        this.getDeviceId().addClientPayloadTemplate(payloadTemp);
        break;
      }
  
      case Packet.PKT_CLIENT_DIAGNOSTIC: {
        // log diagnostic
        int diagCode = (int) payload.readULong(2);
        byte diagData[] = payload.readBytes(255);
        this.getDeviceId().logDiagnostic(diagCode, diagData);
        break;
      }
  
      case Packet.PKT_CLIENT_ERROR: {
        // handle error
        int errCode = (int) payload.readULong(2);
        byte errData[] = payload.readBytes(255);
        this.getDeviceId().logError(errCode, errData);
        return this._handleError(packet);
      }
  
      default: {
        // generate error
        throw new PacketParseException(ServerErrors.NAK_PACKET_TYPE, packet); // errData ok
      }
    }//end switch

    return null;
  }

  /**
   * Logs for debuging, then parse the packet, records errors to log and returns a Packet[]. 
   * Return Packet[] can be null.
   * 
   * @param ipAddr the ipaddress of the packet.
   * @param pkt a packet representation.
   * @return Packet[] the parsed packet or null.
   */
  private Packet[] _parsePacket(String ipAddr, byte pkt[]) {
    // 'pkt' always represents a single packet

    // Running Fletcher checksum 
    this.fletcher.runningChecksum(pkt);

    // print packet 
    if ((pkt != null) && (pkt.length > 0)) {
      if (pkt[0] == Encoding.AsciiEncodingChar) {
        int len = (pkt[pkt.length - 1] == Encoding.AsciiEndOfLineChar) ? (pkt.length - 1)
            : pkt.length;
        Print.logDebug("<== " + StringTools.toStringValue(pkt, 0, len));
      }
      else {
        String encPkt = StringTools.toHexString(pkt);
        Print.logDebug("<== 0x" + encPkt);
      }
    }

    // parse packet
    Packet packet = null;
    try {

      // parse packet
      // Note: 'this.deviceId' may be null here (eg. before device is defined)
      // The device id is only needed for custom payload templates when parsing
      // custom events.
      packet = new Packet(this.deviceId, true, pkt); // client packet
      if (this.encoding == Encoding.ENCODING_UNKNOWN) {
        // The first received packet establishes the encoding
        this.encoding = packet.getEncoding();
      }

      /* handle client packet, return response packets */
      Packet p[] = this._handlePacket(packet);
      return p;

    }
    
    catch (PacketParseException ppe) {

      // add packet if null 
      if (ppe.getPacket() == null) {
        ppe.setPacket(packet);
      }

      // terminate? 
      if (ppe.terminateSession()) {
        this.terminate = true;
      }

      // return error packet 
      Print.logException("Error: ", ppe);
      return new Packet[] { ppe.createServerErrorPacket() };
    } //end catch
  }

}
