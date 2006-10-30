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
package org.opendmtp.codes;

import org.opendmtp.util.*;

/**
 * This class defines the client errors.  The types of errors include protocol/
 * packet errors, property errors, command errors, upload errors, GPS errors, 
 * and internal errors.
 * <p>Javadoc created by Kiet Huynh on 10/29/2006.</p>
 */
public class ClientErrors {

  // ----------------------------------------------------------------------------
  // Protocol/Packet errors (data provides specifics):

  /**
   * Invalid packet header.
   * <p>Payload:</p> 
   *      <ul><li>0:2 - This error code</li>
   *      <li>2:1 - Packet header causing error (if available)</li>
   *      <li>3:1 - Packet type causing error (if available)</li></ul>
   * <p>Notes: Sent to server when the packet is not recognize.</p>
   */
  public static final int ERROR_PACKET_HEADER = 0xF111;
  // Description:
  //      Invalid packet header
  // Payload:
  //      0:2 - This error code
  //      2:1 - Packet header causing error (if available)
  //      3:1 - Packet type causing error (if available)
  // Notes:
  //      Sent to server when the packet header is not recognized.

  /**
   * Invalid packet type.
   * <p>Payload:</p>
   *      <ul><li>0:2 - This error code</li>
   *          <li>2:1 - Packet header causing error</li>
   *          <li>3:1 - Packet type causing erro</li></ul>
   * <p>Notes: Sent to server when the packet type is not recognized.</p>
   */
  public static final int ERROR_PACKET_TYPE = 0xF112;
  // Description:
  //      Invalid packet type
  // Payload:
  //      0:2 - This error code
  //      2:1 - Packet header causing error
  //      3:1 - Packet type causing error
  // Notes:
  //      Sent to server when the packet type is not recognized.

  /**
   * Invalid packet length.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code</li>
   *     <li>2:1 - Packet header causing error</li>
   *     <li>3:1 - Packet type causing erro</li></ul>
   * <p>Notes: Sent to server when the packet length is invalid</p>
   */
  public static final int ERROR_PACKET_LENGTH = 0xF113;
  // Description:
  //      Invalid packet length
  // Payload:
  //      0:2 - This error code
  //      2:1 - Packet header causing error (if available)
  //      3:1 - Packet type causing error (if available)
  // Notes:
  //      Sent to server when the packet length is invalid.

  /**
   * Invalid/unsupported packet encoding.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code</li>
   *     <li>2:1 - Packet header causing error</li>
   *     <li>3:1 - Packet type causing erro</li></ul>
   * <p>Notes: Sent to server when the packet encoding is not supported 
   *           by the client.</p>
   */
  public static final int ERROR_PACKET_ENCODING = 0xF114;
  // Description:
  //      Invalid/unsupported packet encoding
  // Payload:
  //      0:2 - This error code
  //      2:1 - Packet header causing error
  //      3:1 - Packet type causing error
  // Notes:
  //      Sent to server when the packet encoding is not supported by the client.

  /**
   * Invalid packet payload.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code</li>
   *     <li>2:1 - Packet header causing error</li>
   *     <li>3:1 - Packet type causing erro</li></ul>
   * <p>Notes: Sent to server when the packet payload is invalid.</p>
   */
  public static final int ERROR_PACKET_PAYLOAD = 0xF115;
  // Description:
  //      Invalid packet payload
  // Payload:
  //      0:2 - This error code
  //      2:1 - Packet header causing error
  //      3:1 - Packet type causing error
  // Notes:
  //      Sent to server when the packet payload is invalid.

  /**
   * Invalid packet checksum (ASCII encoding only).
   * <p>Payload</p>
   * <ul><li>0:2 - This error code</li>
   *     <li>2:1 - Packet header causing error</li>
   *     <li>3:1 - Packet type causing erro</li></ul>
   * <p>Notes: Sent to server when the packet checksum appears to be invalid. 
   *           This can only occur for ASCII encoded packets.</p>
   */
  public static final int ERROR_PACKET_CHECKSUM = 0xF116;
  // Description:
  //      Invalid packet checksum (ASCII encoding only)
  // Payload:
  //      0:2 - This error code
  //      2:1 - Packet header causing error
  //      3:1 - Packet type causing error
  // Notes:
  //      Sent to server when the packet checksum appears to be invalid.
  //      This can only occur for ASCII encoded packets.

  /**
   * Packet ACK sequence invalid.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code</li>
   *     <li>2:1 - Packet header causing error</li>
   *     <li>3:1 - Packet type causing erro</li></ul>
   * <p>Notes: Sent to the server when the packet ACK sequence number was not 
   *           found in the list of sent/unacknowledged events packets.</p>
   */
  public static final int ERROR_PACKET_ACK = 0xF117;
  // Description:
  //      Packet ACK sequence invalid
  // Payload:
  //      0:2 - This error code
  //      2:1 - Packet header causing error
  //      3:1 - Packet type causing error
  // Notes:
  //      Sent to server when the packet ACK sequence number was not found
  //      in the list of sent/unacknowledged event packets.

  /**
   * Protocol error.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code</li>
   *     <li>2:1 - Packet header causing error</li>
   *     <li>3:1 - Packet type causing erro</li></ul>
   * <p>Notes: Sent to the server when the client does not receive an expected
   *           response from the server.</p>
   */
  public static final int ERROR_PROTOCOL_ERROR = 0xF121;
  // Description:
  //      Protocol error
  // Payload:
  //      0:2 - This error code
  //      2:1 - Packet header causing error
  //      3:1 - Packet type causing error
  // Notes:
  //      Sent to server when the client does not receive an expected
  //      response from the server.

  // ----------------------------------------------------------------------------
  // Property errors (data provides specifics):

  /**
   * Property is read-only.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code</li>
   *     <li>2:2 - The id of the read-only property</li></ul>
   * <p>Notes: Sent to the server when an attempt is made to set a read-only 
   *           property.</p>
   */
  public static final int ERROR_PROPERTY_READ_ONLY = 0xF201;
  // Description:
  //      Property is read-only
  // Payload:
  //      0:2 - This error code
  //      2:2 - the id of the read-only property
  // Notes:
  //      Sent to server when an attempt is made to set a read-only property

  /**
   * Property is write-only.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code</li>
   *     <li>2:2 - The id of the write-only property</li></ul>
   * <p>Notes: Sent to the server when an attempt is made to read a write-only
   *           property.</p>
   */
  public static final int ERROR_PROPERTY_WRITE_ONLY = 0xF202;
  // Description:
  //      Property is write-only
  // Payload:
  //      0:2 - This error code
  //      2:2 - the id of the write-only property
  // Notes:
  //      Sent to server when an attempt is made to read a write-only property

  /**
   * Invalid/unsupported property ID.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code</li>
   *     <li>2:2 - The id of the unrecognized property</li></ul>
   * <p>Notes: Sent to the server when an attempt is made to get/set an 
   *           unrecognized property id.</p>
   */
  public static final int ERROR_PROPERTY_INVALID_ID = 0xF211;
  // Description:
  //      Invalid/unsupported property ID
  // Payload:
  //      0:2 - This error code
  //      2:2 - the id of the unrecognized property
  // Notes:
  //      Sent to server when an attempt is made to get/set an unrecognized property id

  /**
   * Invalid property value.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code</li>
   *     <li>2:2 - The id of the property which is attempting to be get/set</li></ul>
   * <p>Notes: Sent to server when a specified value is invalid for the property type.</p>
   */
  public static final int ERROR_PROPERTY_INVALID_VALUE = 0xF212;
  // Description:
  //      Invalid property value
  // Payload:
  //      0:2 - This error code
  //      2:2 - the id of the property which is attempting to be get/set
  // Notes:
  //      Sent to server when a specified value is invalid for the property type

  /**
   * Invalid property value.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code</li>
   *     <li>2:2 - The id of the property which has the error.</li></ul>
   * <p>Notes: Sent to the server a specified value is invalid for the property type.</p>
   */
  public static final int ERROR_PROPERTY_UNKNOWN_ERROR = 0xF213;
  // Description:
  //      Invalid property value
  // Payload:
  //      0:2 - This error code
  //      2:2 - the id of the property which has the error
  // Notes:
  //      Sent to server when a specified value is invalid for the property type

  // ----------------------------------------------------------------------------
  // Command errors (data provides specifics):

  /**
   * The specified command is invalid/unsupported.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li>
   *     <li>2:2 - The id of the invalid command.</li></ul>
   * <p>Notes: Sent to the server when the client is requested to perform a
   *           command which it does not support.</p>
   */
  public static final int ERROR_COMMAND_INVALID = 0xF311;
  // Description:
  //      The specified command is invalid/unsupported
  // Payload:
  //      0:2 - This error code
  //      2:2 - the id of the invalid command.
  // Notes:
  //      Sent to the server when the client is requested to perform a 
  //      command which it does not support.

  /**
   * The command arguments are invalid, or an execution error was encountered.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li>
   *     <li>2:2 - The id of the commnand which had the error.</li>
   *     <li>4:2 - Returned command error (reason).</li>
   *     <li>6:X - Other data which may be useful in diagnosing the 
   *               error [optional].</li></ul>
   * <p>Notes: Sent to the server when the executed client command has found an
   *           error either in the command arguments, or in the execution 
   *           of the command.</p>
   */
  public static final int ERROR_COMMAND_ERROR = 0xF321;
  // Description:
  //      The command arguments are invalid, or an execution error was encountered.
  // Payload:
  //      0:2 - This error code
  //      2:2 - the id of the command which had the error
  //      4:2 - returned command error (reason)
  //      6:X - other data which may be useful in diagnosing the error [optional]
  // Notes:
  //      Sent to the server when the executed client command has found an 
  //      error either in the command arguments, or in the execution of the command.

  // ----------------------------------------------------------------------------
  // Upload errors (data provides specifics):

  /**
   * Invalid specified upload type.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li></ul>
   * <p>Notes: Sent to the server when the upload record type is not recognized.</p>
   */
  public static final int ERROR_UPLOAD_TYPE = 0xF401;
  // Description:
  //      Invalid speficied upload type
  // Payload:
  //      0:2 - This error code
  // Notes:
  //      Sent to the server when the upload record type is not recognized.

  /**
   * Invalid specified upload file size (too small/large).
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li></ul>
   * <p>Notes: Sent to the server when the specified data length is 
   *           too large/small.</p>
   */
  public static final int ERROR_UPLOAD_LENGTH = 0xF411;
  // Description:
  //      Invalid speficied upload file size (too small/large)
  // Payload:
  //      0:2 - This error code
  // Notes:
  //      Sent to the server when the specified data length is too large/small.

  /**
   * Invalid specified upload file offset.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li></ul>
   * <p>Notes: Sent to the server when the specified data offset overlaps a 
   *           previous record.</p>
   */
  public static final int ERROR_UPLOAD_OFFSET_OVERLAP = 0xF412;
  // Description:
  //      Invalid speficied upload file offset
  // Payload:
  //      0:2 - This error code
  // Notes:
  //      Sent to the server when the specified data offset overlaps a previous record.

  /**
   * Invalid specified upload file offset.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li></ul>
   * <p>Notes: Sent to the server when the specified data offset leaves a gap between 
   *           this and the previous record.</p>
   */
  public static final int ERROR_UPLOAD_OFFSET_GAP = 0xF413;
  // Description:
  //      Invalid speficied upload file offset
  // Payload:
  //      0:2 - This error code
  // Notes:
  //      Sent to the server when the specified data offset leaves a gap between this
  //      and the previous record.

  /**
   * Invalid specified upload file offset.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li></ul>
   * <p>Notes: Sent to the server when the specified data offset and the length of
   *           the provided data exceeds the previously specified length of 
   *           the file.</p>
   */
  public static final int ERROR_UPLOAD_OFFSET_OVERFLOW = 0xF414;
  // Description:
  //      Invalid speficied upload file offset
  // Payload:
  //      0:2 - This error code
  // Notes:
  //      Sent to the server when the specified data offset and the length of the
  //      provided data exceeds the previously specified length of the file.

  /**
   * Invalid specified upload file name.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li></ul>
   * <p>Notes: Sent to the server when the specified filename is invalid.</p>
   */
  public static final int ERROR_UPLOAD_FILE_NAME = 0xF421;
  // Description:
  //      Invalid speficied upload file name
  // Payload:
  //      0:2 - This error code
  // Notes:
  //      Sent to the server when the specified filename is invalid.

  /**
   * Invalid specified upload checksum.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li></ul>
   * <p>Notes: Sent to the server when the specified checksum value is invalid.</p>
   */
  public static final int ERROR_UPLOAD_CHECKSUM = 0xF431;
  // Description:
  //      Invalid speficied upload checksum
  // Payload:
  //      0:2 - This error code
  // Notes:
  //      Sent to the server when the specified checksum value is invalid.

  /**
   * Error saving upload file.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li>
   *     <li>2:X - Additional diagnostic information as needed.</li></ul>
   * <p>Notes: Sent to the server when the client is unable to save the 
   *           uploaded file. Possibly due to some internal client error.</p>
   */
  public static final int ERROR_UPLOAD_SAVE = 0xF441;
  // Description:
  //      Error saving upload file
  // Payload:
  //      0:2 - This error code
  //      2:X - Additional diagnostic information as needed.
  // Notes:
  //      Sent to the server when the client is unable to save the uploaded
  //      file.  Possibly due to some internal client error.

  // ----------------------------------------------------------------------------
  // GPS errors (data provides specifics):

  /**
   * GPS fix expired (possible antenna problem).
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li>
   *     <li>2:4 - The time of the last valid fix.</li></ul>
   * <p>Notes: Sent to server when the client has determined that a new GPS
   *           fix has not bee aquired in the expected time frame (as specified
   *           by the property PROP_GPS_EXPIRATION).  This typically means that
   *           either the device is not in an area where a GPS fix is possible,
   *           or that there may be a problem with the GPS antenna.
   */
  public static final int ERROR_GPS_EXPIRED = 0xF911;
  // Description:
  //      GPS fix expired (possible antenna problem)
  // Payload:
  //      0:2 - This error code
  //      2:4 - the time of the last valid fix
  // Notes:
  //      Sent to server when the client has determined that a new GPS
  //      fix has not bee aquired in the expected time frame (as specified
  //      by the property PROP_GPS_EXPIRATION).  This typically means that
  //      either the device is not in an area where a GPS fix is possible,
  //      or that there may be a problem with the GPS antenna.

  /**
   * Lost communication with GPS module (possible module problem).
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li>
   *     <li>2:4 - The time of the last GPS communication.</li>
   *     <li>6:X - Anything else that the client deems useful to diagnosing
   *               this problem.</li></ul>
   * <p>Notes:  This differs from ERROR_GPS_EXPIRED is that no communication from
   *            GPS module (whether valid, or invalid) has been received in the 
   *            expected time frame (typically 15 to 30 seconds).  This typically
   *            indicates a failure in the GPS module.</p>
   */
  public static final int ERROR_GPS_FAILURE = 0xF912;
  // Description:
  //      Lost communication with GPS module (possible module problem)
  // Payload:
  //      0:2 - This error code
  //      2:4 - the time of the last GPS communication
  //      6:X - anything else that the client deems useful to diagnosing this problem.
  // Notes:
  //      This differs from ERROR_GPS_EXPIRED is that no communication from
  //      GPS module (whether valid, or invalid) has been received in the 
  //      expected time frame (typically 15 to 30 seconds).  This typically
  //      indicates a failure in the GPS module.

  // ----------------------------------------------------------------------------
  // Internal errors (data provides specifics):

  /**
   * Internal error, as defined by client device.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li>
   *     <li>2:X - Payload format is defined by the client.</li></ul>
   * <p>Notes:  This error code is for use by the client to allow general
   *      error information to be sent to the server for analysis.</p>
   */
  public static final int ERROR_INTERNAL_ERROR_00 = 0xFE00;

  /**
   * Internal error, as defined by client device.
   * <p>Payload</p>
   * <ul><li>0:2 - This error code.</li>
   *     <li>2:X - Payload format is defined by the client.</li></ul>
   * <p>Notes:  This error code is for use by the client to allow general
   *      error information to be sent to the server for analysis.</p>
   */
  public static final int ERROR_INTERNAL_ERROR_0F = 0xFE0F;

  // Description:
  //      Internal error, as defined by client device
  // Payload:
  //      0:2 - This error code
  //      2:X - payload format is defined by the client.
  // Notes:
  //      These error codes are for use by the client to allow general
  //      error information to be sent to the server for analysis.

  // ----------------------------------------------------------------------------

  /**
   * Return a error discription as a String.
   * @param errCode - The error code.
   * @return A short description of the error.
   */
  public static String getErrorDescription(int errCode) {
    switch (errCode) {
    case ERROR_PACKET_HEADER:
      return "Invalid packet header";
    case ERROR_PACKET_TYPE:
      return "Invalid packet type";
    case ERROR_PACKET_LENGTH:
      return "Invalid packet length";
    case ERROR_PACKET_ENCODING:
      return "Unsupported packet encoding";
    case ERROR_PACKET_PAYLOAD:
      return "Invalid packet payload";
    case ERROR_PACKET_CHECKSUM:
      return "Invalid checksum";
    case ERROR_PACKET_ACK:
      return "Invalid ACL sequence";
    case ERROR_PROTOCOL_ERROR:
      return "Protocol error";
    case ERROR_PROPERTY_READ_ONLY:
      return "Property is read-only";
    case ERROR_PROPERTY_WRITE_ONLY:
      return "Property is write-only";
    case ERROR_PROPERTY_INVALID_ID:
      return "Invalid/Unrecognized property key";
    case ERROR_PROPERTY_INVALID_VALUE:
      return "Invalid propery value";
    case ERROR_PROPERTY_UNKNOWN_ERROR:
      return "Unknown property error";
    case ERROR_COMMAND_INVALID:
      return "Invalid/Unsupported command";
    case ERROR_COMMAND_ERROR:
      return "Command error";
    case ERROR_UPLOAD_TYPE:
      return "Invalid upload type";
    case ERROR_UPLOAD_LENGTH:
      return "Invalid upload length";
    case ERROR_UPLOAD_OFFSET_OVERLAP:
      return "Upload offset overlap";
    case ERROR_UPLOAD_OFFSET_GAP:
      return "Upload offset gap";
    case ERROR_UPLOAD_OFFSET_OVERFLOW:
      return "Upload offset overflow";
    case ERROR_UPLOAD_FILE_NAME:
      return "Invalid uploaded filename";
    case ERROR_UPLOAD_CHECKSUM:
      return "Invalid uploaded checksum";
    case ERROR_UPLOAD_SAVE:
      return "Unable to save uploaded file";
    case ERROR_GPS_EXPIRED:
      return "GPS fix expired";
    case ERROR_GPS_FAILURE:
      return "GPS failure";
    }
    if ((errCode >= ERROR_INTERNAL_ERROR_00) && (errCode <= ERROR_INTERNAL_ERROR_0F)) {
      return "Internal error";
    }
    return "Unknown [" + StringTools.toHexString(errCode, 16) + "]";
  }

}
