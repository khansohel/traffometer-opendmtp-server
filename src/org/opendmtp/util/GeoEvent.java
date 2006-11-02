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
//  GPS event information container
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.util;


/**
 * Container for GPS event information.
 * 
 * @author Martin D. Flynn
 * @author Robert S. Brewer
 */
public class GeoEvent {

  // ------------------------------------------------------------------------

  /**
   * Name of database field holding data source, stored as type String. 
   */
  public static final String FLD_dataSource = "dataSource";
  /**
   * Name of database field holding raw data, stored as type String. 
   */
  public static final String FLD_rawData = "rawData";
  /**
   * Name of database field holding status code, stored as type Long. 
   */
  public static final String FLD_statusCode = "code";
  /**
   * Name of database field holding timestamp, stored as type Long. 
   */
  public static final String FLD_timestamp = "time";
  /**
   * Name of database field holding latitude, stored as type Double. 
   */
  public static final String FLD_latitude = "lat";
  /**
   * Name of database field holding longitude, stored as type Double. 
   */
  public static final String FLD_longitude = "lon";
  /**
   * Name of database field holding speed in kph, stored as type Double. 
   */
  public static final String FLD_speedKPH = "speed";
  /**
   * Name of database field holding heading, stored as type Double. 
   */
  public static final String FLD_heading = "heading";
  /**
   * Name of database field holding altitude, stored as type Double. 
   */
  public static final String FLD_altitude = "altitude";
  /**
   * Name of database field holding distance in km, stored as type Double. 
   */
  public static final String FLD_distanceKM = "distance";

  /**
   * Name of database field holding sequence, stored as type Long. 
   */
  public static final String FLD_sequence = "seq";
  /**
   * Name of database field holding sequence length, stored as type Long. 
   */
  public static final String FLD_sequenceLength = "seqLen";

  /**
   * Name of database field holding GeoFence IDs, stored as array of type Long. 
   */
  public static final String FLD_geofenceID = "geofence";
  /**
   * Name of database field holding top speed in kph, stored as type Double. 
   */
  public static final String FLD_topSpeedKPH = "topSpeed";

  /**
   * Name of database field holding index, stored as type Long. 
   */
  public static final String FLD_index = "index";

  /**
   * Name of database field holding input ID, stored as type Long. 
   */
  public static final String FLD_inputID = "inputID";
  /**
   * Name of database field holding input state, stored as type Long. 
   */
  public static final String FLD_inputState = "inputState";
  /**
   * Name of database field holding output ID, stored as type Long. 
   */
  public static final String FLD_outputID = "outputID";
  /**
   * Name of database field holding output state, stored as type Long. 
   */
  public static final String FLD_outputState = "outputState";
  /**
   * Name of database field holding elapsed times, stored as array of type Long. 
   */
  public static final String FLD_elapsedTime = "elapsedTime";
  /**
   * Name of database field holding counters, stored as array of type Long. 
   */
  public static final String FLD_counter = "counter";

  /**
   * Name of database field holding low sensor values, stored as array of type Long. 
   */
  public static final String FLD_sensor32LO = "sens32LO";
  /**
   * Name of database field holding high sensor values, stored as array of type Long. 
   */
  public static final String FLD_sensor32HI = "sens32HI";
  /**
   * Name of database field holding average sensor values, stored as array of type Long. 
   */
  public static final String FLD_sensor32AV = "sens32AV";
  /**
   * Name of database field holding low temperature values, stored as array of type Double. 
   */
  public static final String FLD_tempLO = "tempLO";
  /**
   * Name of database field holding high temperature values, stored as array of type Double. 
   */
  public static final String FLD_tempHI = "tempHI";
  /**
   * Name of database field holding average temperature values, stored as array of type Double. 
   */
  public static final String FLD_tempAV = "tempAV";

  /**
   * Name of database field holding strings, stored as array of type String. 
   */
  public static final String FLD_string = "string";
  /**
   * Name of database field holding binaries, stored as array of type Byte. 
   */
  public static final String FLD_binary = "binary";

  /**
   * Name of database field holding GPS age, stored as type Long.
   */
  public static final String FLD_gpsAge = "gpsAge";
  /**
   * Name of database field holding Differential GPS update, stored as type Long. 
   */
  public static final String FLD_gpsDgpsUpdate = "gpsDgpsUpd";
  /**
   * Name of database field holding GPS horizontal accuracy, stored as type Double. 
   */
  public static final String FLD_gpsHorzAccuracy = "gpsHorzAcc";
  /**
   * Name of database field holding GPS vertical accuracy, stored as type Double. 
   */
  public static final String FLD_gpsVertAccuracy = "gpsVertAcc";
  /**
   * Name of database field holding number of GPS satellites, stored as type Long. 
   */
  public static final String FLD_gpsSatellites = "gpsSats";
  /**
   * Name of database field holding GPS Mag (magnetic?) variation, stored as type Double. 
   */
  public static final String FLD_gpsMagVariation = "gpsMagVar";
  /**
   * Name of database field holding GPS quality, stored as type Long. 
   */
  public static final String FLD_gpsQuality = "gpsQuality";
  /**
   * Name of database field holding GPS 2D 3D, stored as type Long. 
   */
  public static final String FLD_gps2D3D = "gps2D3D";
  /**
   * Name of database field holding GPS geoid height, stored as type Double. 
   */
  public static final String FLD_gpsGeoidHeight = "gpsGeoidHt";
  /**
   * Name of database field holding GPS PDOP, stored as type Double. 
   */
  public static final String FLD_gpsPDOP = "gpsPDOP";
  /**
   * Name of database field holding GPS HDOP, stored as type Double. 
   */
  public static final String FLD_gpsHDOP = "gpsHDOP";
  /**
   * Name of database field holding GPS VDOP, stored as type Double. 
   */
  public static final String FLD_gpsVDOP = "gpsVDOP";

  // ------------------------------------------------------------------------

  private OrderedMap fieldMap = null;

  /**
   * Initializes field map.
   */
  public GeoEvent() {
    this.fieldMap = new OrderedMap();
  }

  // ------------------------------------------------------------------------

  /**
   * Sets the event value stored in the specified field name to an Object.
   * 
   * @param fldName name of field to store value in.
   * @param newVal new value to be stored in field.
   * @param ndx index of array value in field to set, if -1 if field is not an array type.
   */
  public void setEventValue(String fldName, Object newVal, int ndx) {
    if (ndx < 0) {
      this.fieldMap.put(fldName, newVal);
    }
    else {
      this.fieldMap.put(fldName + "." + ndx, newVal);
    }
  }

  /**
   * Sets the event value stored in the specified field name.
   * 
   * @param fldName name of field to store value in.
   * @param newVal new value to be stored in field.
   */
  public void setEventValue(String fldName, Object newVal) {
    this.setEventValue(fldName, newVal, -1);
  }

  /**
   * Sets the event value stored in the specified field name.
   * 
   * @param fldName name of field to store value in.
   * @param val new value to be stored in field.
   * @param ndx index of array value in field to set.
   */
  public void setEventValue(String fldName, long val, int ndx) {
    this.setEventValue(fldName, new Long(val), ndx);
  }

  /**
   * Sets the event value stored in the specified field name.
   * 
   * @param fldName name of field to store value in.
   * @param val new value to be stored in field.
   */
  public void setEventValue(String fldName, long val) {
    this.setEventValue(fldName, new Long(val), -1);
  }

  /**
   * Sets the event value stored in the specified field name.
   * 
   * @param fldName name of field to store value in.
   * @param val new value to be stored in field.
   * @param ndx index of array value in field to set.
   */
  public void setEventValue(String fldName, double val, int ndx) {
    this.setEventValue(fldName, new Double(val), ndx);
  }

  /**
   * Sets the event value stored in the specified field name.
   * 
   * @param fldName name of field to store value in.
   * @param val new value to be stored in field.
   */
  public void setEventValue(String fldName, double val) {
    this.setEventValue(fldName, new Double(val), -1);
  }

  // ------------------------------------------------------------------------

  /**
   * Gets the event value stored in the specified field name as an Object.
   * 
   * @param fldName name of field to retrieve value from.
   * @param ndx index of array value in field to retrieve, if -1 if field is not an array type.
   * @return the stored Object.
   */
  private Object getEventValue(String fldName, int ndx) {
    String fn = (ndx < 0) ? fldName : (fldName + "." + ndx);
    return this.fieldMap.get(fn);
  }

  /**
   * Gets the event value stored in the specified field name as an Object.
   * 
   * @param fldName name of field to retrieve value from.
   * @return the stored Object.
   */
  private Object getEventValue(String fldName) {
    return this.getEventValue(fldName, -1);
  }

  /**
   * Gets the event value stored in the specified field name as a String.
   * 
   * @param fldName name of field to retrieve value from.
   * @param dft default String value to return if retrieved value is null.
   * @param ndx index of array value in field to retrieve, if -1 if field is not an array type.
   * @return the stored value as a String.
   */
  public String getStringValue(String fldName, String dft, int ndx) {
    String fn = (ndx < 0) ? fldName : (fldName + "." + ndx);
    Object val = this.getEventValue(fn);
    if (val instanceof byte[]) {
      return "0x" + StringTools.toHexString((byte[]) val);
    }
    else if (val != null) {
      return val.toString();
    }
    else {
      return dft;
    }
  }

  /**
   * Gets the event value stored in the specified field name as a String.
   * 
   * @param fldName name of field to retrieve value from.
   * @param dft default String value to return if retrieved value is null.
   * @return the stored value as a String.
   */
  public String getStringValue(String fldName, String dft) {
    return this.getStringValue(fldName, dft, -1);
  }

  /**
   * Gets the event value stored in the specified field name as an array of bytes.
   * 
   * @param fldName name of field to retrieve value from.
   * @param dft default byte array value to return if retrieved value is null.
   * @param ndx index of array value in field to retrieve, if -1 if field is not an array type.
   * @return the stored value as a byte array.
   */
  public byte[] getByteValue(String fldName, byte[] dft, int ndx) {
    String fn = (ndx < 0) ? fldName : (fldName + "." + ndx);
    Object val = this.getEventValue(fn);
    if (val instanceof byte[]) {
      return (byte[]) val;
    }
    else {
      return dft;
    }
  }

  /**
   * Gets the event value stored in the specified field name as an array of bytes.
   * 
   * @param fldName name of field to retrieve value from.
   * @param dft default byte array value to return if retrieved value is null.
   * @return the stored value as a byte array.
   */
  public byte[] getByteValue(String fldName, byte[] dft) {
    return this.getByteValue(fldName, dft, -1);
  }

  /**
   * Gets the event value stored in the specified field name as a Long.
   * 
   * @param fldName name of field to retrieve value from.
   * @param dft default value to return if retrieved value is null.
   * @param ndx index of array value in field to retrieve, if -1 if field is not an array type.
   * @return the stored value as a Long.
   */
  public long getLongValue(String fldName, long dft, int ndx) {
    String fn = (ndx < 0) ? fldName : (fldName + "." + ndx);
    Object val = this.getEventValue(fn);
    if (val instanceof Number) {
      return ((Number) val).longValue();
    }
    else {
      return dft;
    }
  }

  /**
   * Gets the event value stored in the specified field name as a Long.
   * 
   * @param fldName name of field to retrieve value from.
   * @param dft default value to return if retrieved value is null.
   * @return the stored value as a Long.
   */
  public long getLongValue(String fldName, long dft) {
    return this.getLongValue(fldName, dft, -1);
  }

  /**
   * Gets the event value stored in the specified field name as Double.
   * 
   * @param fldName name of field to retrieve value from.
   * @param dft default value to return if retrieved value is null.
   * @param ndx index of array value in field to retrieve, if -1 if field is not an array type.
   * @return the stored value as a Double.
   */
  public double getDoubleValue(String fldName, double dft, int ndx) {
    String fn = (ndx < 0) ? fldName : (fldName + "." + ndx);
    Object val = this.getEventValue(fn);
    if (val instanceof Number) {
      return ((Number) val).doubleValue();
    }
    else {
      return dft;
    }
  }

  /**
   * Gets the event value stored in the specified field name as Double.
   * 
   * @param fldName name of field to retrieve value from.
   * @param dft default value to return if retrieved value is null.
   * @return the stored value as a Double.
   */
  public double getDoubleValue(String fldName, double dft) {
    return this.getDoubleValue(fldName, dft, -1);
  }

  // ------------------------------------------------------------------------

  /**
   * Accessor for data source field.
   * 
   * @return the data source field from the database, or the empty String if field is null.
   */
  public String getDataSource() {
    return this.getStringValue(FLD_dataSource, "");
  }

  /**
   * Accessor for status code field.
   * 
   * @return the status code field from the database, or -1 if field is null.
   */
  public int getStatusCode() {
    return (int) this.getLongValue(FLD_statusCode, -1L);
  }

  /**
   * Accessor for timestamp field.
   * 
   * @return the timestamp field from the database, or -1 if field is null.
   */
  public long getTimestamp() {
    return this.getLongValue(FLD_timestamp, -1L);
  }

  /**
   * Accessor for latitude field.
   * 
   * @return the latitude field from the database, or 0 if field is null.
   */
  public double getLatitude() {
    return this.getDoubleValue(FLD_latitude, 0.0);
  }

  /**
   * Accessor for longitude field.
   * 
   * @return the longitude field from the database, or 0 if field is null.
   */
  public double getLongitude() {
    return this.getDoubleValue(FLD_longitude, 0.0);
  }

  /**
   * Provides latitude and longitude as a GeoPoint.
   * 
   * @return a new GeoPoint with latitude and longitude from database (or 0 if that field is null).
   */
  public GeoPoint getGeoPoint() {
    return new GeoPoint(this.getLatitude(), this.getLongitude());
  }

  /**
   * Accessor for speed field.
   * 
   * @return the speed field from the database, or 0 if field is null.
   */
  public double getSpeed() {
    return this.getDoubleValue(FLD_speedKPH, 0.0);
  }

  /**
   * Accessor for heading field.
   * 
   * @return the heading field from the database, or 0 if field is null.
   */
  public double getHeading() {
    return this.getDoubleValue(FLD_heading, 0.0);
  }

  /**
   * Accessor for altitude field.
   * 
   * @return the altitude field from the database, or 0 if field is null.
   */
  public double getAltitude() {
    return this.getDoubleValue(FLD_altitude, 0.0);
  }

  /**
   * Accessor for distance field.
   * 
   * @return the distance field from the database, or 0 if field is null.
   */
  public double getDistance() {
    return this.getDoubleValue(FLD_distanceKM, 0.0);
  }

  /**
   * Accessor for top speed field.
   * 
   * @return the top speed field from the database, or 0 if field is null.
   */
  public double getTopSpeed() {
    return this.getDoubleValue(FLD_topSpeedKPH, 0.0);
  }

  /**
   * Accessor for GeoFence ID array field.
   * 
   * @param ndx index of array value in field to retrieve.
   * @return the GeoFence ID field from the database, or 0 if indexed field is null.
   */
  public long getGeofence(int ndx) {
    return this.getLongValue(FLD_geofenceID, 0L, ndx);
  }

  // ------------------------------------------------------------------------

  /**
   * Accessor for sequence field.
   * 
   * @return the sequence field from the database, or -1 if field is null.
   */
  public long getSequence() {
    return this.getLongValue(FLD_sequence, -1L);
  }

  /**
   * Accessor for sequence length field.
   * 
   * @return the sequence length field from the database, or 0 if field is null.
   */
  public int getSequenceLength() {
    return (int) this.getLongValue(FLD_sequenceLength, 0L);
  }

  // ------------------------------------------------------------------------

  /**
   * Accessor for raw data field.
   * 
   * @return the raw data field from the database, or the empty String if field is null.
   */
  public String getRawData() {
    return this.getStringValue(FLD_rawData, "");
  }

  // ------------------------------------------------------------------------

}
