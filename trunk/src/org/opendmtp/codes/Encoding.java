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

/**
 * Defines ASCII encoding types and checksum encoding. The ASCII encoding types include Hex, CVS,
 * and Base64 encoding.
 * 
 * @author Martin D. Flynn
 * @author Kiet Huynh
 */
public class Encoding {

  // ------------------------------------------------------------------------

  /**
   * Indicator of the start of ASCII packet.
   */
  public static final char AsciiEncodingChar = '$';

  /**
   * Indicator of the beginning of the checksum.
   */
  public static final char AsciiChecksumChar = '*';

  /**
   * ACSII end-of-file character.
   */
  public static final char AsciiEndOfLineChar = '\r';

  // ------------------------------------------------------------------------
  // Packet encoding

  /**
   * Supported binary encoding.
   */
  public static final int SUPPORTED_ENCODING_BINARY = 0x01;

  /**
   * Supported base64 encoding.
   */
  public static final int SUPPORTED_ENCODING_BASE64 = 0x02;

  /**
   * Supported hexidecimal encoding.
   */
  public static final int SUPPORTED_ENCODING_HEX = 0x04;

  /**
   * Supported CVS encoding.
   */
  public static final int SUPPORTED_ENCODING_CSV = 0x08;

  /**
   * Unknown ASCII encoding.
   */
  public static final int ENCODING_UNKNOWN = -1; // unknown ASCII encoding

  /**
   * Binary encoding.
   */
  public static final int ENCODING_BINARY = 0; // server must support

  /**
   * Base64 encoding.
   */
  public static final int ENCODING_BASE64 = 10; // server must support

  /**
   * Base64 checksum encoding.
   */
  public static final int ENCODING_BASE64_CKSUM = 11; // server must support

  /**
   * Hex encoding.
   */
  public static final int ENCODING_HEX = 20; // server must support

  /**
   * Hex checksum encoding.
   */
  public static final int ENCODING_HEX_CKSUM = 21; // server must support

  /**
   * CVS encoding.
   */
  public static final int ENCODING_CSV = 30; // server need not support

  /**
   * CVS Checksum encoding.
   */
  public static final int ENCODING_CSV_CKSUM = 31; // server need not support

  /**
   * Checks if an encoding is ASCII encoding.
   * 
   * @param encoding The encoding code.
   * @return Returns true if this is ASCII encoding. Otherwise, return false.
   */
  public static boolean IsEncodingAscii(int encoding) {
    return (encoding > 0);
  }

  /**
   * Checks if an encoding is checksum encoding.
   * 
   * @param encoding The encoding code.
   * @return Returns true if this is a checksum encoding. Otherwise, return false.
   */
  public static boolean IsEncodingChecksum(int encoding) {
    return (encoding > 0) && ((encoding % 10) != 0);
  }

  // ------------------------------------------------------------------------

  /**
   * Base64 encoding indicator.
   */
  public static final char ENCODING_BASE64_CHAR = '=';

  /**
   * Hex encoding indicator.
   */
  public static final char ENCODING_HEX_CHAR = ':';

  /**
   * CVS encoding indicator.
   */
  public static final char ENCODING_CSV_CHAR = ',';

  // ------------------------------------------------------------------------

}
