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
package org.opendmtp.server.db;

import org.opendmtp.util.GeoPoint;
import org.opendmtp.util.StringTools;

/**
 * Payload template class defining packet payload structure.
 * @author Martin D. Flynn
 * @author Brandon Horiuchi
 */
public class PayloadTemplate {

  // ------------------------------------------------------------------------
  /**
   * Constant integer PRIMITIVE_MASK.
   */
  public static final int PRIMITIVE_MASK = 0x00F0;
  
  /**
   * Constant integer PRIMITIVE_LONG.
   */
  public static final int PRIMITIVE_LONG = 0x0010;
  
  /**
   * Constant integer PRIMITIVE_GPS.
   */
  public static final int PRIMITIVE_GPS = 0x0030;
  
  /**
   * Constant integer PRIMITIVE_STRING.
   */
  public static final int PRIMITIVE_STRING = 0x0040;

  /**
   * Constant integer PRIMITIVE_BINARY.
   */
  public static final int PRIMITIVE_BINARY = 0x0050;

  // ------------------------------------------------------------------------
  /**
   * Payload template field for status code.
   */
  public static final int FIELD_STATUS_CODE = 0x01;
  
  /**
   * Payload template field for timestamp.
   */
  public static final int FIELD_TIMESTAMP = 0x02;
  
  /**
   * Payload template field for index.
   */
  public static final int FIELD_INDEX = 0x03;

  /**
   * Payload template field for sequence.
   */
  public static final int FIELD_SEQUENCE = 0x04;

  /**
   * Payload template field for gps point.
   */
  public static final int FIELD_GPS_POINT = 0x06;
  
  /**
   * Payload template field for gps age.  0 to 65535 sec.
   */
  public static final int FIELD_GPS_AGE = 0x07; // %2u 0 to 65535 sec

  /**
   * Payload template field for speed.
   */
  public static final int FIELD_SPEED = 0x08;
  
  /**
   * Payload template field for heading.
   */
  public static final int FIELD_HEADING = 0x09;
  
  /**
   * Payload template field for altitude.
   */
  public static final int FIELD_ALTITUDE = 0x0A;
  
  /**
   * Payload template field for distance.
   */
  public static final int FIELD_DISTANCE = 0x0B;

  // Misc fields                                                 // Low                          High
  /**
   * Payload template field for geofence id.
   */
  public static final int FIELD_GEOFENCE_ID = 0x0E; // %4u 0x00000000 to 0xFFFFFFFF
  
  /**
   * Payload template field for top speed.
   */
  public static final int FIELD_TOP_SPEED = 0x0F; // %1u 0 to 255 kph             %2u 0.0 to 655.3 kph

  /**
   * Payload template field for string.
   */
  public static final int FIELD_STRING = 0x11; // %*s may contain only 'A'..'Z', 'a'..'z, '0'..'9', '-', '.'

  /**
   * Payload template field for binary.
   */
  public static final int FIELD_BINARY = 0x1A; // %*b  

  // I/O fields                                                  // Low                          High
  /**
   * Payload template field for input id.
   */
  public static final int FIELD_INPUT_ID = 0x21; // %4u 0x00000000 to 0xFFFFFFFF
  
  /**
   * Payload template field for input state.
   */
  public static final int FIELD_INPUT_STATE = 0x22; // %4u 0x00000000 to 0xFFFFFFFF
  
  /**
   * Payload template field for output id.
   */
  public static final int FIELD_OUTPUT_ID = 0x24; // %4u 0x00000000 to 0xFFFFFFFF
  
  /**
   * Payload template field for output stste.
   */
  public static final int FIELD_OUTPUT_STATE = 0x25; // %4u 0x00000000 to 0xFFFFFFFF
  
  /**
   * Payload template field for elapsed time.
   */
  public static final int FIELD_ELAPSED_TIME = 0x27; // %3u 0 to 16777216 sec        %4u 0.000 to 4294967.295 sec
  
  /**
   * Payload template field for counter.
   */
  public static final int FIELD_COUNTER = 0x28; // %4u 0 to 4294967295

  /**
   * Payload template field for sensor low.
   */
  public static final int FIELD_SENSOR32_LOW = 0x31; // %4u 0x00000000 to 0xFFFFFFFF
  
  /**
   * Payload template field for sensor high.
   */
  public static final int FIELD_SENSOR32_HIGH = 0x32; // %4u 0x00000000 to 0xFFFFFFFF
  
  /**
   * Payload template field for sensor average.
   */
  public static final int FIELD_SENSOR32_AVER = 0x33; // %4u 0x00000000 to 0xFFFFFFFF
  
  /**
   * Payload template field for temperature low.
   */
  public static final int FIELD_TEMP_LOW = 0x3A; // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C
  
  /**
   * Payload template field for temperature high.
   */
  public static final int FIELD_TEMP_HIGH = 0x3B; // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C
  
  /**
   * Payload template field for temperature average.
   */
  public static final int FIELD_TEMP_AVER = 0x3C; // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C

  // GPS quality fields                                          // Low                          High
  /**
   * Payload template field for gps update.
   */
  public static final int FIELD_GPS_DGPS_UPDATE = 0x41; // %2u 0 to 65535 sec
  
  /**
   * Payload template field for gps horizontal accuracy.
   */
  public static final int FIELD_GPS_HORZ_ACCURACY = 0x42; // %1u 0 to 255 m               %2u 0.0 to 6553.5 m
  
  /**
   * Payload template field for gps vertical accuracy.
   */
  public static final int FIELD_GPS_VERT_ACCURACY = 0x43; // %1u 0 to 255 m               %2u 0.0 to 6553.5 m
  
  /**
   * Payload template field for gps satellites.
   */
  public static final int FIELD_GPS_SATELLITES = 0x44; // %1u 0 to 12
  
  /**
   * Payload template field for gps mag variation.
   */
  public static final int FIELD_GPS_MAG_VARIATION = 0x45; // %2i -180.00 to 180.00 deg
  
  /**
   * Payload template field for gps quality.
   */
  public static final int FIELD_GPS_QUALITY = 0x46; // %1u (0=None, 1=GPS, 2=DGPS, ...)
  
  /**
   * Payload template field for gps type.
   */
  public static final int FIELD_GPS_TYPE = 0x47; // %1u (1=None, 2=2D, 3=3D, ...)
  
  /**
   * Payload template field for gps geoid height.
   */
  public static final int FIELD_GPS_GEOID_HEIGHT = 0x48; // %1i -128 to +127 m           %2i -3276.7 to +3276.7 m
  
  /**
   * Payload template field for gps pdop.
   */
  public static final int FIELD_GPS_PDOP = 0x49; // %1u 0.0 to 25.5              %2u 0.0 to 99.9
  
  /**
   * Payload template field for gps hdop.
   */
  public static final int FIELD_GPS_HDOP = 0x4A; // %1u 0.0 to 25.5              %2u 0.0 to 99.9
  
  /**
   * Payload template field for gps vdop.
   */
  public static final int FIELD_GPS_VDOP = 0x4B; // %1u 0.0 to 25.5              %2u 0.0 to 99.9

  // ------------------------------------------------------------------------

  private int customType = -1; // undefined
  private Field fields[] = null;
  private boolean repeatLast = false;

  /**
   * Payload template constructor with type and field parameters.
   * @param type Integer custom type.
   * @param flds Array of field objects.
   */
  public PayloadTemplate(int type, Field flds[]) {
    this.customType = type;
    this.fields = flds;
    this.repeatLast = false;
  }

  /**
   * Payload template constructor with type, field and repeat last parameters.
   * @param type Integer custom type.
   * @param flds Array of field objects.
   * @param repeatLast Boolean.
   */
  public PayloadTemplate(int type, Field flds[], boolean repeatLast) {
    this.customType = type;
    this.fields = flds;
    this.repeatLast = repeatLast;
  }

  // ------------------------------------------------------------------------
  /**
   * Gets packet type.
   * @return Integer custom type.
   */
  public int getPacketType() {
    return this.customType;
  }

  /**
   * Gets field from fields array at index.
   * @param ndx Integer index to get field from.
   * @return Field at index.
   */
  public Field getField(int ndx) {
    if ((ndx >= 0) && (this.fields != null) && (this.fields.length > 0)) {
      if (ndx < this.fields.length) {
        return this.fields[ndx];
      }
      else if (this.repeatLast) {
        return this.fields[this.fields.length - 1];
      }
    }
    return null;
  }

  /**
   * Gets array of fields.
   * @return Array of fields.
   */
  public Field[] getFields() {
    if (this.fields == null) {
      this.fields = new Field[0];
    }
    return this.fields;
  }

  /**
   * Gets repeatlast boolean value.
   * @return repeatlast boolean value.
   */
  public boolean getRepeatLast() {
    return this.repeatLast;
  }

  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------
  // ------------------------------------------------------------------------

  private static final char FIELD_VALUE_SEPARATOR = '|';

  /**
   * Class defining field object.
   */
  public static class Field {
    private boolean hiRes = false;
    private int fldType = -1;
    private int fldNdx = 0;
    private int fldLen = 0;

    /**
     * Field constructor with type, hiRes, index and length parameters.
     * @param type Integer field type.
     * @param hiRes Boolean field hiRes.
     * @param index Integer field index.
     * @param length Integer field length.
     */
    public Field(int type, boolean hiRes, int index, int length) {
      this.fldType = type;
      this.hiRes = hiRes;
      this.fldNdx = index;
      this.fldLen = length;
    }

    /**
     * Field constuctor with mask parameter.
     * @param mask Long integer.
     */
    public Field(long mask) {
      this.fldType = (int) (mask >> 16) & 0x7F;
      this.hiRes = ((mask & 0x800000) != 0);
      this.fldNdx = (int) (mask >> 8) & 0xFF;
      this.fldLen = (int) mask & 0xFF;
    }

    /**
     * Field constuctor with string paramter.
     * @param s String with field formatted values.
     */
    public Field(String s) {
      // "<type>|[H|L]|<index>|<length>"
      String f[] = StringTools.parseString(s, FIELD_VALUE_SEPARATOR);
      this.fldType = (f.length > 0) ? StringTools.parseInt(f[1], -1) : -1;
      this.hiRes = (f.length > 1) ? f[0].equalsIgnoreCase("H") : false;
      this.fldNdx = (f.length > 2) ? StringTools.parseInt(f[2], 0) : 0;
      this.fldLen = (f.length > 3) ? StringTools.parseInt(f[3], 0) : 0;
    }

    /**
     * Gets field type.
     * @return Integer field type.
     */
    public int getType() {
      return this.fldType;
    }

    /**
     * Gets primitive types.
     * @return Integer primitive field types.
     */
    public int getPrimitiveType() {
      switch (this.fldType) {
      case FIELD_GPS_POINT:
        return PRIMITIVE_GPS;
      case FIELD_STRING:
        return PRIMITIVE_STRING;
      case FIELD_BINARY:
        return PRIMITIVE_BINARY;
      default:
        return PRIMITIVE_LONG;
      }
    }

    /**
     * Checks if field type is valid.
     * @return True always.
     */
    public boolean isValidType() {
      return true;
    }

    /**
     * Checks if field is signed.
     * @return True if field is signed.
     */
    public boolean isSigned() {
      switch (this.fldType) {
      case FIELD_GPS_MAG_VARIATION:
      case FIELD_GPS_GEOID_HEIGHT:
      case FIELD_ALTITUDE:
      case FIELD_TEMP_LOW:
      case FIELD_TEMP_HIGH:
      case FIELD_TEMP_AVER:
        return true;
      default:
        return false;
      }
    }

    /**
     * Checks if Field is Hex.
     * @return True if field is hex.
     */
    public boolean isHex() {
      switch (this.fldType) {
      case FIELD_HEADING:
        return !this.hiRes;
      case FIELD_STATUS_CODE:
      case FIELD_SEQUENCE:
      case FIELD_INPUT_ID:
      case FIELD_INPUT_STATE:
      case FIELD_OUTPUT_ID:
      case FIELD_OUTPUT_STATE:
      case FIELD_GEOFENCE_ID:
        return true;
      default:
        return false;
      }
    }

    /**
     * Checks if field is HiRes.
     * @return True if field is HiRes.
     */
    public boolean isHiRes() {
      return this.hiRes;
    }

    /**
     * Gets field index.
     * @return Integer field index
     */
    public int getIndex() {
      return this.fldNdx;
    }

    /**
     * Gets field length.
     * @return Integer field length.
     */
    public int getLength() {
      return this.fldLen;
    }

    /**
     * Parses string adds data to payload.
     * @param s String to parse.
     * @param sndx Integer index for string.
     * @param payload payload to add to.
     * @return Integer index in string.
     */
    public int parseString(String s[], int sndx, Payload payload) {
      // NOTE: This should specifically set the index to the proper payload location!!
      int length = this.getLength();
      switch (this.getPrimitiveType()) {
      case PRIMITIVE_GPS: {
        double lat = StringTools.parseDouble(s[sndx++], 0.0);
        double lon = (sndx < s.length) ? StringTools.parseDouble(s[sndx++], 0.0) : 0.0;
        payload.writeGPS(new GeoPoint(lat, lon), length);
        break;
      }
      case PRIMITIVE_STRING: {
        payload.writeString(s[sndx++], length);
        break;
      }
      case PRIMITIVE_BINARY: {
        byte b[] = StringTools.parseHex(s[sndx++], new byte[0]);
        payload.writeBytes(b, length);
        break;
      }
      case PRIMITIVE_LONG:
      default: {
        long val = s[sndx].startsWith("0x") ? StringTools.parseHexLong(s[sndx++], 0L) : StringTools
            .parseLong(s[sndx++], 0L);
        if (this.isSigned()) {
          payload.writeLong(val, length);
        }
        else {
          payload.writeULong(val, length);
        }
        break;
      }
      }
      return sndx;
    }

    /**
     * Creates Field formatted string.
     * @return String representation of field.
     */
    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append(this.getType());
      sb.append(FIELD_VALUE_SEPARATOR);
      sb.append(this.isHiRes() ? "H" : "L");
      sb.append(FIELD_VALUE_SEPARATOR);
      sb.append(this.getIndex());
      sb.append(FIELD_VALUE_SEPARATOR);
      sb.append(this.getLength());
      return sb.toString();
    }

    /**
     * Checks for field equality.
     * @param other Object to compare.
     * @return True if same.
     */
    public boolean equals(Object other) {
      if (other instanceof Field) {
        return this.toString().equals(other.toString());
      }
      else {
        return false;
      }
    }

  }

}
