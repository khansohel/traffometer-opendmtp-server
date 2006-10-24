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

import java.lang.*;
import java.text.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.awt.*;

import org.opendmtp.util.*;

public class GeoEvent
{
 
    // ------------------------------------------------------------------------

    public static final String FLD_dataSource           = "dataSource";     // String
    public static final String FLD_rawData              = "rawData";        // String

    public static final String FLD_statusCode           = "code";           // Long
    public static final String FLD_timestamp            = "time";           // Long
    public static final String FLD_latitude             = "lat";            // Double
    public static final String FLD_longitude            = "lon";            // Double
    public static final String FLD_speedKPH             = "speed";          // Double
    public static final String FLD_heading              = "heading";        // Double
    public static final String FLD_altitude             = "altitude";       // Double
    public static final String FLD_distanceKM           = "distance";       // Double

    public static final String FLD_sequence             = "seq";            // Long
    public static final String FLD_sequenceLength       = "seqLen";         // Long
    
    public static final String FLD_geofenceID           = "geofence";       // Long [array]
    public static final String FLD_topSpeedKPH          = "topSpeed";       // Double

    public static final String FLD_index                = "index";          // Long
    
    public static final String FLD_inputID              = "inputID";        // Long
    public static final String FLD_inputState           = "inputState";     // Long
    public static final String FLD_outputID             = "outputID";       // Long
    public static final String FLD_outputState          = "outputState";    // Long
    public static final String FLD_elapsedTime          = "elapsedTime";    // Long [array]
    public static final String FLD_counter              = "counter";        // Long [array]
    
    public static final String FLD_sensor32LO           = "sens32LO";       // Long [array]
    public static final String FLD_sensor32HI           = "sens32HI";       // Long [array]
    public static final String FLD_sensor32AV           = "sens32AV";       // Long [array]
    public static final String FLD_tempLO               = "tempLO";         // Double [array]
    public static final String FLD_tempHI               = "tempHI";         // Double [array]
    public static final String FLD_tempAV               = "tempAV";         // Double [array]
    
    public static final String FLD_string               = "string";         // String [array]
    public static final String FLD_binary               = "binary";         // Byte [array]
    
    public static final String FLD_gpsAge               = "gpsAge";         // Long
    public static final String FLD_gpsDgpsUpdate        = "gpsDgpsUpd";     // Long
    public static final String FLD_gpsHorzAccuracy      = "gpsHorzAcc";     // Double
    public static final String FLD_gpsVertAccuracy      = "gpsVertAcc";     // Double
    public static final String FLD_gpsSatellites        = "gpsSats";        // Long
    public static final String FLD_gpsMagVariation      = "gpsMagVar";      // Double
    public static final String FLD_gpsQuality           = "gpsQuality";     // Long
    public static final String FLD_gps2D3D              = "gps2D3D";        // Long
    public static final String FLD_gpsGeoidHeight       = "gpsGeoidHt";     // Double
    public static final String FLD_gpsPDOP              = "gpsPDOP";        // Double
    public static final String FLD_gpsHDOP              = "gpsHDOP";        // Double
    public static final String FLD_gpsVDOP              = "gpsVDOP";        // Double

    // ------------------------------------------------------------------------

    private OrderedMap fieldMap = null;
    
    public GeoEvent()
    {
        this.fieldMap = new OrderedMap();
    }
    
    // ------------------------------------------------------------------------
    
    public void setEventValue(String fldName, Object newVal, int ndx) 
    {
        if (ndx < 0) {
            this.fieldMap.put(fldName, newVal);
        } else {
            this.fieldMap.put(fldName + "." + ndx, newVal);
        }
    }
    public void setEventValue(String fldName, Object newVal) 
    {
        this.setEventValue(fldName, newVal, -1);
    }
    
    public void setEventValue(String fldName, long val, int ndx) 
    {
        this.setEventValue(fldName, new Long(val), ndx);
    }
    public void setEventValue(String fldName, long val) 
    {
        this.setEventValue(fldName, new Long(val), -1);
    }

    public void setEventValue(String fldName, double val, int ndx) 
    {
        this.setEventValue(fldName, new Double(val), ndx);
    }
    public void setEventValue(String fldName, double val) 
    {
        this.setEventValue(fldName, new Double(val), -1);
    }

    // ------------------------------------------------------------------------

    private Object getEventValue(String fldName, int ndx)
    {
        String fn = (ndx < 0)? fldName : (fldName + "." + ndx);
        return this.fieldMap.get(fn);
    }
    private Object getEventValue(String fldName)
    {
        return this.getEventValue(fldName, -1);
    }

    public String getStringValue(String fldName, String dft, int ndx)
    {
        String fn = (ndx < 0)? fldName : (fldName + "." + ndx);
        Object val = this.getEventValue(fn);
        if (val instanceof byte[]) {
            return "0x" + StringTools.toHexString((byte[])val);
        } else
        if (val != null) {
            return val.toString();
        } else {
            return dft;
        }
    }
    public String getStringValue(String fldName, String dft)
    {
        return this.getStringValue(fldName, dft, -1);
    }

    public byte[] getByteValue(String fldName, byte[] dft, int ndx)
    {
        String fn = (ndx < 0)? fldName : (fldName + "." + ndx);
        Object val = this.getEventValue(fn);
        if (val instanceof byte[]) {
            return (byte[])val;
        } else {
            return dft;
        }
    }
    public byte[] getByteValue(String fldName, byte[] dft)
    {
        return this.getByteValue(fldName, dft, -1);
    }

    public long getLongValue(String fldName, long dft, int ndx)
    {
        String fn = (ndx < 0)? fldName : (fldName + "." + ndx);
        Object val = this.getEventValue(fn);
        if (val instanceof Number) {
            return ((Number)val).longValue();
        } else {
            return dft;
        }
    }
    public long getLongValue(String fldName, long dft)
    {
        return this.getLongValue(fldName, dft, -1);
    }
    
    public double getDoubleValue(String fldName, double dft, int ndx)
    {
        String fn = (ndx < 0)? fldName : (fldName + "." + ndx);
        Object val = this.getEventValue(fn);
        if (val instanceof Number) {
            return ((Number)val).doubleValue();
        } else {
            return dft;
        }
    }
    public double getDoubleValue(String fldName, double dft)
    {
        return this.getDoubleValue(fldName, dft, -1);
    }

    // ------------------------------------------------------------------------

    public String getDataSource()
    {
        return this.getStringValue(FLD_dataSource, "");
    }

    public int getStatusCode()
    {
        return (int)this.getLongValue(FLD_statusCode, -1L);
    }

    public long getTimestamp()
    {
        return this.getLongValue(FLD_timestamp, -1L);
    }

    public double getLatitude()
    {
        return this.getDoubleValue(FLD_latitude, 0.0);
    }

    public double getLongitude()
    {
        return this.getDoubleValue(FLD_longitude, 0.0);
    }

    public GeoPoint getGeoPoint()
    {
        return new GeoPoint(this.getLatitude(), this.getLongitude());
    }

    public double getSpeed()
    {
        return this.getDoubleValue(FLD_speedKPH, 0.0);
    }

    public double getHeading()
    {
        return this.getDoubleValue(FLD_heading, 0.0);
    }

    public double getAltitude()
    {
        return this.getDoubleValue(FLD_altitude, 0.0);
    }

    public double getDistance()
    {
        return this.getDoubleValue(FLD_distanceKM, 0.0);
    }

    public double getTopSpeed()
    {
        return this.getDoubleValue(FLD_topSpeedKPH, 0.0);
    }

    public long getGeofence(int ndx)
    {
        return this.getLongValue(FLD_geofenceID, 0L, ndx);
    }

    // ------------------------------------------------------------------------

    public long getSequence()
    {
        return this.getLongValue(FLD_sequence, -1L);
    }
    
    public int getSequenceLength()
    {
        return (int)this.getLongValue(FLD_sequenceLength, 0L);
    }

    // ------------------------------------------------------------------------

    public String getRawData()
    {
        return this.getStringValue(FLD_rawData, "");
    }

    // ------------------------------------------------------------------------
    
}
