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
//  2006/04/02  Martin D. Flynn
//      Minor changes to 'toString()'
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

public class Event
{

    // ------------------------------------------------------------------------
    // types:
    //      long
    //      double
    //      byte
    //      GeoPoint
    //      String
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------

    /* the fields that made up this object */
    private Packet                packet        = null;
    private PayloadTemplate       custTemplate  = null;
    private int                   custFieldLen  = 0;
    private GeoEvent              geoEvent      = null;

    // ------------------------------------------------------------------------
    
    public Event(Packet pkt)
        throws PacketParseException
    {
        super();
        this.packet = pkt;
        this.geoEvent = new GeoEvent();
        
        /* Validate Packet */
        if (this.packet == null) {
            // no packet specified
            // internal error (this should never happen)
            throw new PacketParseException(ServerErrors.NAK_PACKET_LENGTH, this.packet); // errData ok
        } else
        if (!this.packet.isEventType()) {
            // not an event packet
            // internal error (this should never happen)
            throw new PacketParseException(ServerErrors.NAK_PACKET_TYPE, this.packet); // errData ok
        } else
        if (!this.packet.hasPayload()) {
            // client did not include payload
            throw new PacketParseException(ServerErrors.NAK_PACKET_PAYLOAD, this.packet); // errData ok
        }

        /* get Event Payload Definition? */
        this.custTemplate = this.packet.getPayloadTemplate();
        if (this.custTemplate == null) {
            Print.logError("PayloadTemplate not found: " + StringTools.toHexString(this.packet.getPacketType(),8));
            throw new PacketParseException(ServerErrors.NAK_FORMAT_NOT_RECOGNIZED, this.packet); // errData ok
        }
        
        /* parse */
        this.custFieldLen = 0;
        this._decodeEvent();

    }

    // ------------------------------------------------------------------------

    public Packet getPacket()
    {
        return this.packet;
    }
    
    public GeoEvent getGeoEvent()
    {
        return this.geoEvent;
    }
    
    public long getSequence()
    {
        return this.getGeoEvent().getSequence();
    }
    
    public int getSequenceLength()
    {
        return this.getGeoEvent().getSequenceLength();
    }

    // ------------------------------------------------------------------------
    
    private void setEventValue(String fldName, Object val, int ndx) 
    {
        this.getGeoEvent().setEventValue(fldName, val, ndx);
    }
    private void setEventValue(String fldName, Object val) 
    {
        this.getGeoEvent().setEventValue(fldName, val);
    }

    public void setEventValue(String fldName, long val, int ndx) 
    {
        this.getGeoEvent().setEventValue(fldName, val, ndx);
    }
    public void setEventValue(String fldName, long val) 
    {
        this.getGeoEvent().setEventValue(fldName, val);
    }

    public void setEventValue(String fldName, double val, int ndx) 
    {
        this.getGeoEvent().setEventValue(fldName, val, ndx);
    }
    public void setEventValue(String fldName, double val)
    {
        this.getGeoEvent().setEventValue(fldName, val);
    }

    // ------------------------------------------------------------------------

    public byte[] getByteValue(String fldName, byte[] dft)
    {
        return this.getGeoEvent().getByteValue(fldName, dft);
    }
    public  byte[] getByteValue(String fldName,  byte[] dft, int ndx)
    {
        return this.getGeoEvent().getByteValue(fldName, dft, ndx);
    }

    public String getStringValue(String fldName, String dft)
    {
        return this.getGeoEvent().getStringValue(fldName, dft);
    }
    public String getStringValue(String fldName, String dft, int ndx)
    {
        return this.getGeoEvent().getStringValue(fldName, dft, ndx);
    }

    public long getLongValue(String fldName, long dft)
    {
        return this.getGeoEvent().getLongValue(fldName, dft);
    }
    public long getLongValue(String fldName, long dft, int ndx)
    {
        return this.getGeoEvent().getLongValue(fldName, dft, ndx);
    }
    
    public double getDoubleValue(String fldName, double dft)
    {
        return this.getGeoEvent().getDoubleValue(fldName, dft);
    }
    public double getDoubleValue(String fldName, double dft, int ndx)
    {
        return this.getGeoEvent().getDoubleValue(fldName, dft, ndx);
    }

    // ------------------------------------------------------------------------

    private void _decodeEvent()
        throws PacketParseException
    {
        Payload payload = this.packet.getPayload(true);
        
        /* defaults */
        this.setEventValue(GeoEvent.FLD_rawData   , this.packet.toString());
        this.setEventValue(GeoEvent.FLD_statusCode, StatusCodes.STATUS_NONE);
        this.setEventValue(GeoEvent.FLD_timestamp , DateTime.getCurrentTimeSec());
        
        /* parse payload */
        boolean hasStatusCode = false;
        boolean hasGeoPoint = false;
        payload.resetIndex();
        for (this.custFieldLen = 0; payload.isValidLength(0); this.custFieldLen++) {
            PayloadTemplate.Field field = this.custTemplate.getField(this.custFieldLen);
            if (field == null) { break; }
            int type      = field.getType();
            boolean hiRes = field.isHiRes();
            int ndx       = field.getIndex();
            int length    = field.getLength();
            GeoPoint gp   = null;
            switch (type) {
                
                case PayloadTemplate.FIELD_STATUS_CODE  : // %2u
                    this.setEventValue(GeoEvent.FLD_statusCode, payload.readULong(length, 0L));
                    hasStatusCode = true;
                    break;
                case PayloadTemplate.FIELD_TIMESTAMP    : // %4u
                    this.setEventValue(GeoEvent.FLD_timestamp, payload.readULong(length, 0L));
                    break;
                case PayloadTemplate.FIELD_INDEX        : // %4u 0 to 4294967295
                    this.setEventValue(GeoEvent.FLD_index, payload.readULong(length, 0L));
                    break;
                case PayloadTemplate.FIELD_GPS_POINT    : // %6g                          %8g
                    gp = payload.readGPS(length);
                    this.setEventValue(GeoEvent.FLD_latitude , gp.getLatitude());
                    this.setEventValue(GeoEvent.FLD_longitude, gp.getLongitude());
                    hasGeoPoint = true;
                    break;
                case PayloadTemplate.FIELD_SPEED        : // %1u 0 to 255 kph             %2u 0.0 to 655.3 kph
                    this.setEventValue(GeoEvent.FLD_speedKPH, hiRes?
                        ((double)payload.readULong(length, 0L) / 10.0) :
                        ((double)payload.readULong(length, 0L)));
                    break;
                case PayloadTemplate.FIELD_HEADING      : // %1u 1.412 deg un.            %2u 0.00 to 360.00 deg
                    this.setEventValue(GeoEvent.FLD_heading, hiRes?
                        ((double)payload.readULong(length, 0L) / 100.0) :
                        ((double)payload.readULong(length, 0L) * 360.0/255.0));
                    break;
                case PayloadTemplate.FIELD_ALTITUDE     : // %2i -32767 to +32767 m       %3i -838860.7 to +838860.7 m
                    this.setEventValue(GeoEvent.FLD_altitude, hiRes?
                        ((double)payload.readLong(length, 0L) / 10.0) :
                        ((double)payload.readLong(length, 0L)));
                    break;
                case PayloadTemplate.FIELD_DISTANCE     : // %3u 0 to 16777216 km         %3u 0.0 to 1677721.6 km
                    this.setEventValue(GeoEvent.FLD_distanceKM, hiRes?
                        ((double)payload.readULong(length, 0L) / 10.0) :
                        ((double)payload.readULong(length, 0L)));
                    break;
                case PayloadTemplate.FIELD_SEQUENCE     : // %1u 0 to 255
                    this.setEventValue(GeoEvent.FLD_sequence, payload.readULong(length, -1L));
                    this.setEventValue(GeoEvent.FLD_sequenceLength, length);
                    break;

                case PayloadTemplate.FIELD_INPUT_ID     : // %4u 0x00000000 to 0xFFFFFFFF
                    this.setEventValue(GeoEvent.FLD_inputID, payload.readULong(length, 0L));
                    break;
                case PayloadTemplate.FIELD_INPUT_STATE  : // %4u 0x00000000 to 0xFFFFFFFF
                    this.setEventValue(GeoEvent.FLD_inputState, payload.readULong(length, 0L));
                    break;
                case PayloadTemplate.FIELD_OUTPUT_ID    : // %4u 0x00000000 to 0xFFFFFFFF
                    this.setEventValue(GeoEvent.FLD_outputID, payload.readULong(length, 0L));
                    break;
                case PayloadTemplate.FIELD_OUTPUT_STATE : // %4u 0x00000000 to 0xFFFFFFFF
                    this.setEventValue(GeoEvent.FLD_outputState, payload.readULong(length, 0L));
                    break;
                case PayloadTemplate.FIELD_ELAPSED_TIME : // %3u 0 to 16777216 sec        %4u 0.000 to 4294967.295 sec
                    this.setEventValue(GeoEvent.FLD_elapsedTime, hiRes?
                        (payload.readULong(length, 0L)) :
                        (payload.readULong(length, 0L) * 1000L), ndx);
                    break;
                case PayloadTemplate.FIELD_COUNTER : // %4u 0 to 4294967295
                    this.setEventValue(GeoEvent.FLD_counter, payload.readULong(length, 0L), ndx);
                    break;
                case PayloadTemplate.FIELD_SENSOR32_LOW : // %4u 0x00000000 to 0xFFFFFFFF
                    this.setEventValue(GeoEvent.FLD_sensor32LO, payload.readULong(length, 0L), ndx);
                    break;
                case PayloadTemplate.FIELD_SENSOR32_HIGH  : // %4u 0x00000000 to 0xFFFFFFFF
                    this.setEventValue(GeoEvent.FLD_sensor32HI, payload.readULong(length, 0L), ndx);
                    break;
                case PayloadTemplate.FIELD_SENSOR32_AVER  : // %4u 0x00000000 to 0xFFFFFFFF
                    this.setEventValue(GeoEvent.FLD_sensor32AV, payload.readULong(length, 0L), ndx);
                    break;
                case PayloadTemplate.FIELD_TEMP_LOW     : // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C
                    this.setEventValue(GeoEvent.FLD_tempLO, hiRes?
                        ((double)payload.readLong(length, 0L) / 10.0) :
                        ((double)payload.readLong(length, 0L)), ndx);
                    break;
                case PayloadTemplate.FIELD_TEMP_HIGH    : // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C
                    this.setEventValue(GeoEvent.FLD_tempHI, hiRes?
                        ((double)payload.readLong(length, 0L) / 10.0) :
                        ((double)payload.readLong(length, 0L)), ndx);
                    break;
                case PayloadTemplate.FIELD_TEMP_AVER    : // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C
                    this.setEventValue(GeoEvent.FLD_tempAV, hiRes?
                        ((double)payload.readLong(length, 0L) / 10.0) :
                        ((double)payload.readLong(length, 0L)), ndx);
                    break;

                case PayloadTemplate.FIELD_GEOFENCE_ID  : // %4u 0x00000000 to 0xFFFFFFFF
                    this.setEventValue(GeoEvent.FLD_geofenceID, payload.readULong(length, 0L), ndx);
                    break;
                case PayloadTemplate.FIELD_TOP_SPEED    : // %1u 0 to 255 kph             %2u 0.0 to 655.3 kph
                    this.setEventValue(GeoEvent.FLD_topSpeedKPH, hiRes?
                        ((double)payload.readULong(length, 0L) / 10.0) :
                        ((double)payload.readULong(length, 0L)), ndx);
                    break;
                case PayloadTemplate.FIELD_STRING       : // %*s may contain only 'A'..'Z', 'a'..'z, '0'..'9', '-', '.'
                    this.setEventValue(GeoEvent.FLD_string, payload.readString(length), ndx);
                    break;
                case PayloadTemplate.FIELD_BINARY       : // %*b
                    this.setEventValue(GeoEvent.FLD_binary, payload.readBytes(length));
                    break;

                case PayloadTemplate.FIELD_GPS_AGE      : // %2u 0 to 65535 sec
                    this.setEventValue(GeoEvent.FLD_gpsAge, payload.readULong(length, 0L));
                    break;
                case PayloadTemplate.FIELD_GPS_DGPS_UPDATE: // %2u 0 to 65535 sec
                    this.setEventValue(GeoEvent.FLD_gpsDgpsUpdate, payload.readULong(length, 0L));
                    break;
                case PayloadTemplate.FIELD_GPS_HORZ_ACCURACY : // %1u 0 to 255 m               %2u 0.0 to 6553.5 m
                    this.setEventValue(GeoEvent.FLD_gpsHorzAccuracy, hiRes?
                        ((double)payload.readULong(length, 0L) / 10.0) :
                        ((double)payload.readULong(length, 0L)));
                    break;
                case PayloadTemplate.FIELD_GPS_VERT_ACCURACY : // %1u 0 to 255 m               %2u 0.0 to 6553.5 m
                    this.setEventValue(GeoEvent.FLD_gpsVertAccuracy, hiRes?
                        ((double)payload.readULong(length, 0L) / 10.0) :
                        ((double)payload.readULong(length, 0L)));
                    break;
                case PayloadTemplate.FIELD_GPS_SATELLITES : // %1u 0 to 12
                    this.setEventValue(GeoEvent.FLD_gpsSatellites, payload.readULong(length, 0L));
                    break;
                case PayloadTemplate.FIELD_GPS_MAG_VARIATION : // %2i -180.00 to 180.00 deg
                    this.setEventValue(GeoEvent.FLD_gpsMagVariation, (double)payload.readLong(length, 0L) / 100.0);
                    break;
                case PayloadTemplate.FIELD_GPS_QUALITY : // %1u (0=None, 1=GPS, 2=DGPS, ...)
                    this.setEventValue(GeoEvent.FLD_gpsQuality, payload.readULong(length, 0L));
                    break;
                case PayloadTemplate.FIELD_GPS_TYPE : // %1u (1=None, 2=2D, 3=3D, ...)
                    this.setEventValue(GeoEvent.FLD_gps2D3D, payload.readULong(length, 0L));
                    break;
                case PayloadTemplate.FIELD_GPS_GEOID_HEIGHT : // %1i -128 to +127 m           %2i -3276.7 to +3276.7 m
                    this.setEventValue(GeoEvent.FLD_gpsGeoidHeight, hiRes?
                        ((double)payload.readLong(length, 0L) / 10.0) :
                        ((double)payload.readLong(length, 0L)));
                    break;
                case PayloadTemplate.FIELD_GPS_PDOP : // %1u 0.0 to 25.5              %2u 0.0 to 99.9
                    this.setEventValue(GeoEvent.FLD_gpsPDOP, ((double)payload.readLong(length, 0L) / 10.0));
                    break;
                case PayloadTemplate.FIELD_GPS_HDOP : // %1u 0.0 to 25.5              %2u 0.0 to 99.9
                    this.setEventValue(GeoEvent.FLD_gpsHDOP, ((double)payload.readLong(length, 0L) / 10.0));
                    break;
                case PayloadTemplate.FIELD_GPS_VDOP : // %1u 0.0 to 25.5              %2u 0.0 to 99.9
                    this.setEventValue(GeoEvent.FLD_gpsVDOP, ((double)payload.readLong(length, 0L) / 10.0));
                    break;

                default:
                    // internal error (this should not occur here - formats should be pre-validated)
                    Print.logError("Field not defined: " + StringTools.toHexString(type,8));
                    Payload p = new Payload();
                    p.writeULong(this.packet.getPacketType(), 1);
                    byte errData[] = p.getBytes();
                    throw new PacketParseException(ServerErrors.NAK_FORMAT_DEFINITION_INVALID, this.packet, errData); // formatType, fieldIndex
            }
        }
        
        /* set status code if not specified in packet */
        if (!hasStatusCode) {
            this.setEventValue(GeoEvent.FLD_statusCode, hasGeoPoint? StatusCodes.STATUS_LOCATION : StatusCodes.STATUS_NONE);
        }
        
    }

    // ------------------------------------------------------------------------
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Event:\n");
        for (int i = 0; i < this.custFieldLen; i++) {
            PayloadTemplate.Field field = this.custTemplate.getField(i);
            if (field == null) { break; }
            int type      = field.getType();
            boolean hiRes = field.isHiRes();
            int ndx       = field.getIndex();
            int length    = field.getLength();
            double dblVal = 0.0;
            GeoEvent gev  = this.getGeoEvent();
            GeoPoint gp   = null;
            switch (type) {
                
                case PayloadTemplate.FIELD_STATUS_CODE  : // %2u
                    sb.append("  Status Code  : ");
                    sb.append("0x");
                    sb.append(StringTools.toHexString(gev.getStatusCode(), length * 8));
                    sb.append("  [");
                    sb.append(StatusCodes.GetCodeDescription(gev.getStatusCode()));
                    sb.append("]");
                    break;
                case PayloadTemplate.FIELD_TIMESTAMP    : // %4u
                    sb.append("  Timestamp    : ");
                    sb.append(gev.getTimestamp());
                    sb.append(" [").append((new DateTime(gev.getTimestamp())).toString()).append("]");
                    break;
                case PayloadTemplate.FIELD_INDEX        : // %4u 0 to 4294967295
                    sb.append("  Index        : ");
                    sb.append(this.getLongValue(GeoEvent.FLD_index,0L));
                    break;
                case PayloadTemplate.FIELD_GPS_POINT    : // %6g                          %8g
                    sb.append("  GPS Point    : ");
                    gp = gev.getGeoPoint();
                    if (gp != null) {
                        sb.append(gp.toString());
                    } else {
                        sb.append("null");
                    }
                    break;
                case PayloadTemplate.FIELD_SPEED        : // %1u 0 to 255 kph             %2u 0.0 to 655.3 kph
                    sb.append("  Speed        : ");
                    dblVal = this.getDoubleValue(GeoEvent.FLD_speedKPH,0.0);
                    sb.append(StringTools.format(dblVal,"#0.0"));
                    sb.append(" kph");
                    break;
                case PayloadTemplate.FIELD_HEADING      : // %1u 1.412 deg un.            %2u 0.00 to 360.00 deg
                    sb.append("  Heading      : ");
                    dblVal = this.getDoubleValue(GeoEvent.FLD_heading,0.0);
                    sb.append(StringTools.format(dblVal,"#0.0"));
                    sb.append(" degrees");
                    break;
                case PayloadTemplate.FIELD_ALTITUDE     : // %2i -32767 to +32767 m       %3i -838860.7 to +838860.7 m
                    sb.append("  Altitude     : ");
                    dblVal = this.getDoubleValue(GeoEvent.FLD_altitude,0.0);
                    sb.append(StringTools.format(dblVal,"#0.0"));
                    sb.append(" meters");
                    break;
                case PayloadTemplate.FIELD_DISTANCE     : // %3u 0 to 16777216 km         %3u 0.0 to 1677721.6 km
                    sb.append("  Distance     : ");
                    dblVal = this.getDoubleValue(GeoEvent.FLD_distanceKM,0.0);
                    sb.append(StringTools.format(dblVal,"#0.0"));
                    sb.append(" km");
                    break;
                case PayloadTemplate.FIELD_SEQUENCE     : // %1u 0 to 255
                    sb.append("  Sequence     : ");
                    sb.append("0x");
                    sb.append(StringTools.toHexString(this.getLongValue(GeoEvent.FLD_sequence,0L), length * 8));
                    break;

                case PayloadTemplate.FIELD_INPUT_ID     : // %4u 0x00000000 to 0xFFFFFFFF
                    sb.append("  Input ID     : ");
                    sb.append("0x");
                    sb.append(StringTools.toHexString(this.getLongValue(GeoEvent.FLD_inputID,0L), length * 8));
                    break;
                case PayloadTemplate.FIELD_INPUT_STATE  : // %4u 0x00000000 to 0xFFFFFFFF
                    sb.append("  Input State  : ");
                    sb.append("0x");
                    sb.append(StringTools.toHexString(this.getLongValue(GeoEvent.FLD_inputState,0L), length * 8));
                    break;
                case PayloadTemplate.FIELD_OUTPUT_ID    : // %4u 0x00000000 to 0xFFFFFFFF
                    sb.append("  Output ID    : ");
                    sb.append("0x");
                    sb.append(StringTools.toHexString(this.getLongValue(GeoEvent.FLD_outputID,0L), length * 8));
                    break;
                case PayloadTemplate.FIELD_OUTPUT_STATE : // %4u 0x00000000 to 0xFFFFFFFF
                    sb.append("  Output State : ");
                    sb.append("0x");
                    sb.append(StringTools.toHexString(this.getLongValue(GeoEvent.FLD_outputState,0L), length * 8));
                    break;
                case PayloadTemplate.FIELD_ELAPSED_TIME : // %3u 0 to 16777216 sec        %4u 0.000 to 4294967.295 sec
                    sb.append("  Elapsed Time : ");
                    sb.append("[").append(ndx).append("] ");
                    sb.append(this.getLongValue(GeoEvent.FLD_elapsedTime, 0L, ndx));
                    sb.append(" milliseconds");
                    break;
                case PayloadTemplate.FIELD_COUNTER : // %4u 0 to 4294967295
                    sb.append("  Counter      : ");
                    sb.append("[").append(ndx).append("] ");
                    sb.append(this.getLongValue(GeoEvent.FLD_counter, 0L, ndx));
                    break;
                case PayloadTemplate.FIELD_SENSOR32_LOW : // %4u 0x00000000 to 0xFFFFFFFF
                    sb.append("  Sensor Low   : ");
                    sb.append("[").append(ndx).append("] ");
                    sb.append(this.getLongValue(GeoEvent.FLD_sensor32LO, 0L, ndx));
                    break;
                case PayloadTemplate.FIELD_SENSOR32_HIGH : // %4u 0x00000000 to 0xFFFFFFFF
                    sb.append("  Sensor High  : ");
                    sb.append("[").append(ndx).append("] ");
                    sb.append(this.getLongValue(GeoEvent.FLD_sensor32HI, 0L, ndx));
                    break;
                case PayloadTemplate.FIELD_SENSOR32_AVER : // %4u 0x00000000 to 0xFFFFFFFF
                    sb.append("  Sensor Aver  : ");
                    sb.append("[").append(ndx).append("] ");
                    sb.append(this.getLongValue(GeoEvent.FLD_sensor32AV, 0L, ndx));
                    break;
                case PayloadTemplate.FIELD_TEMP_LOW     : // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C
                    sb.append("  Temp Low     : ");
                    sb.append("[").append(ndx).append("] ");
                    sb.append(this.getDoubleValue(GeoEvent.FLD_tempLO, 0.0, ndx));
                    sb.append(" C");
                    break;
                case PayloadTemplate.FIELD_TEMP_HIGH    : // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C
                    sb.append("  Temp High    : ");
                    sb.append("[").append(ndx).append("] ");
                    sb.append(this.getDoubleValue(GeoEvent.FLD_tempHI, 0.0, ndx));
                    sb.append(" C");
                    break;
                case PayloadTemplate.FIELD_TEMP_AVER    : // %1i -127 to +127 C           %2i -3276.7 to +3276.7 C
                    sb.append("  Temp Average : ");
                    sb.append("[").append(ndx).append("] ");
                    sb.append(this.getDoubleValue(GeoEvent.FLD_tempAV, 0.0, ndx));
                    sb.append(" C");
                    break;

                case PayloadTemplate.FIELD_GEOFENCE_ID  : // %4u 0x00000000 to 0xFFFFFFFF
                    sb.append("  Geofence ID  : ");
                    sb.append("0x");
                    sb.append(StringTools.toHexString(this.getLongValue(GeoEvent.FLD_geofenceID,0L), length * 8));
                    break;
                case PayloadTemplate.FIELD_TOP_SPEED    : // %1u 0 to 255 kph             %2u 0.0 to 655.3 kph
                    sb.append("  Top Speed    : ");
                    sb.append(this.getDoubleValue(GeoEvent.FLD_topSpeedKPH,0.0));
                    sb.append(" kph");
                    break;
                case PayloadTemplate.FIELD_STRING       : // %*s may contain only 'A'..'Z', 'a'..'z, '0'..'9', '-', '.'
                    sb.append("  String       : ");
                    sb.append("[").append(ndx).append("] ");
                    sb.append(this.getStringValue(GeoEvent.FLD_string,"",ndx));
                    break;
                case PayloadTemplate.FIELD_BINARY       : // %*b
                    sb.append("  Binary       : ");
                    sb.append("0x");
                    sb.append(StringTools.toHexString(this.getByteValue(GeoEvent.FLD_binary,null)));
                    break;

                case PayloadTemplate.FIELD_GPS_AGE      : // %2u 0 to 65535 sec
                    sb.append("  GPS Age      : ");
                    sb.append(this.getLongValue(GeoEvent.FLD_gpsAge,0L));
                    sb.append(" seconds");
                    break;
                case PayloadTemplate.FIELD_GPS_DGPS_UPDATE : // %2u 0 to 65535 sec
                    sb.append("  DGPS Age     : ");
                    sb.append(this.getLongValue(GeoEvent.FLD_gpsDgpsUpdate,0L));
                    sb.append(" seconds");
                    break;
                case PayloadTemplate.FIELD_GPS_HORZ_ACCURACY : // %1u 0 to 255 m               %2u 0.0 to 6553.5 m
                    sb.append("  GPS H-Accur  : ");
                    sb.append(this.getDoubleValue(GeoEvent.FLD_gpsHorzAccuracy,0.0));
                    sb.append(" meters");
                    break;
                case PayloadTemplate.FIELD_GPS_VERT_ACCURACY : // %1u 0 to 255 m               %2u 0.0 to 6553.5 m
                    sb.append("  GPS V-Accur  : ");
                    sb.append(this.getDoubleValue(GeoEvent.FLD_gpsVertAccuracy,0.0));
                    sb.append(" meters");
                    break;
                case PayloadTemplate.FIELD_GPS_SATELLITES : // %1u 0 to 12
                    sb.append("  GPS Satellites: ");
                    sb.append(this.getLongValue(GeoEvent.FLD_gpsSatellites,0L));
                    sb.append(" meters");
                    break;
                case PayloadTemplate.FIELD_GPS_MAG_VARIATION : // %1u -180.00 to 180.00
                    sb.append("  Mag Variation: ");
                    sb.append(this.getDoubleValue(GeoEvent.FLD_gpsMagVariation,0.0));
                    sb.append(" degrees");
                    break;
                case PayloadTemplate.FIELD_GPS_QUALITY : // 0,1,2
                    sb.append("  GPS Quality: ");
                    sb.append(this.getLongValue(GeoEvent.FLD_gpsQuality,1L));
                    sb.append(" (0=None, 1=GPS, 2=DGPS, ...)");
                    break;
                case PayloadTemplate.FIELD_GPS_TYPE : // 0,1,2
                    sb.append("  GPS 2D/3D     : ");
                    sb.append(this.getLongValue(GeoEvent.FLD_gps2D3D,0L));
                    sb.append(" (1=None, 2=2D, 3=3D, ...)");
                    break;
                case PayloadTemplate.FIELD_GPS_GEOID_HEIGHT : // %1i -128 to +127 m           %2i -3276.7 to +3276.7 m
                    sb.append("  Geoid Height  : ");
                    sb.append(this.getDoubleValue(GeoEvent.FLD_gpsGeoidHeight,0.0));
                    sb.append(" meters");
                    break;
                case PayloadTemplate.FIELD_GPS_PDOP : // %1u 0.0 to 25.5              %2u 0.0 to 99.9
                    sb.append("  GPS PDOP    : ");
                    sb.append(this.getDoubleValue(GeoEvent.FLD_gpsPDOP,0.0));
                    sb.append(" ");
                    break;
                case PayloadTemplate.FIELD_GPS_HDOP : // %1u 0.0 to 25.5              %2u 0.0 to 99.9
                    sb.append("  GPS HDOP    : ");
                    sb.append(this.getDoubleValue(GeoEvent.FLD_gpsHDOP,0.0));
                    sb.append(" ");
                    break;
                case PayloadTemplate.FIELD_GPS_VDOP : // %1u 0.0 to 25.5              %2u 0.0 to 99.9
                    sb.append("  GPS VDOP    : ");
                    sb.append(this.getDoubleValue(GeoEvent.FLD_gpsVDOP,0.0));
                    sb.append(" ");
                    break;

            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
