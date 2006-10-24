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
//  GPS latitude/longitude and algorithms to operate on such.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/04/02  Martin D. Flynn
//      Changed format of lat/lon to include 5 decimal places
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.lang.*;
import java.text.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.awt.*;

import org.opendmtp.util.*;

public class GeoPoint
    implements Cloneable
{

    // ------------------------------------------------------------------------

    private static boolean UseHaversineDistanceFormula = true;

    // ------------------------------------------------------------------------
    
    protected static final double EPSILON           = 1.0E-7; 

    public  static final double MAX_LATITUDE        = 90.0;
    public  static final double MIN_LATITUDE        = -90.0;
    
    public  static final double MAX_LONGITUDE       = 180.0;
    public  static final double MIN_LONGITUDE       = -180.0;
    
    public  static final String PointSeparator      = "/";

    // ------------------------------------------------------------------------
    
    public  static final int    FORMAT_TYPE_MASK    = 0x0F; // format type mask
    public  static final int    FORMAT_DEC          = 0x01; // decimal format
    public  static final String FORMAT_DEC_NAME     = "Decimal"; // decimal format
    public  static final int    FORMAT_DMS          = 0x02; // DMS format
    public  static final String FORMAT_DMS_NAME     = "Deg/Min/Sec"; // DMS format
    public  static final int    FORMAT_AXIS_MASK    = 0xF0; // axis mask
    public  static final int    FORMAT_LATITUDE     = 0x10; // latitude
    public  static final int    FORMAT_LONGITUDE    = 0x20; // longitude
    
    public  static final String NORTH_NAME          = "North";
    public  static final String NORTH_ABBR          = "N";
    public  static final String SOUTH_NAME          = "South";
    public  static final String SOUTH_ABBR          = "S";
    public  static final String EAST_NAME           = "East";
    public  static final String EAST_ABBR           = "E";
    public  static final String WEST_NAME           = "West";
    public  static final String WEST_ABBR           = "W";
    
    public  static final String NE_ABBR             = NORTH_ABBR + EAST_ABBR;
    public  static final String NW_ABBR             = NORTH_ABBR + WEST_ABBR;
    public  static final String SE_ABBR             = SOUTH_ABBR + EAST_ABBR;
    public  static final String SW_ABBR             = SOUTH_ABBR + WEST_ABBR;

    // ------------------------------------------------------------------------

    private static double SQ(double X) { return X * X; }
    
    // ------------------------------------------------------------------------
    // References:
    //   http://www.jqjacobs.net/astro/geodesy.html
    //   http://www.boeing-727.com/Data/fly%20odds/distance.html
    //   http://mathforum.org/library/drmath/view/51785.html
    //   http://mathforum.org/library/drmath/view/52070.html
    
    public  static final double PI                           = Math.PI;
    public  static final double RADIANS                      = PI / 180.0;
    public  static final double EARTH_EQUATORIAL_RADIUS_KM   = 6378.1370;   // Km: a
    public  static final double EARTH_POLOR_RADIUS_KM        = 6356.752314; // Km: b
    public  static final double EARTH_MEAN_RADIUS_KM         = 6371.0088;   // Km: (2a + b)/3 
    
    public  static final double FEET_PER_MILE                = 5280.0;
    public  static final double MILES_PER_KILOMETER          = 0.621371192;
    public  static final double KILOMETERS_PER_MILE          = 1.0 / MILES_PER_KILOMETER; // 1.609344
    public  static final double METERS_PER_FOOT              = 0.3048;
    public  static final double FEET_PER_METER               = 1.0 / METERS_PER_FOOT;     // 3.280839895;
    public  static final double FEET_PER_KILOMETER           = FEET_PER_METER * 1000.0;   // 3280.84
    public  static final double NAUTICAL_MILES_PER_KILOMETER = 0.539956803;
    public  static final double KILOMETERS_PER_NAUTICAL_MILE = 1.0 / NAUTICAL_MILES_PER_KILOMETER;
    public  static final double METERS_PER_MILE              = METERS_PER_FOOT * FEET_PER_MILE; // 1609.344
    
    public  static final int    KILOMETERS                   = 0;
    public  static final String KILOMETERS_NAME              = "KILOMETERS";
    public  static final int    METERS                       = 1;
    public  static final String METERS_NAME                  = "METERS";
    public  static final int    MILES                        = 2;
    public  static final String MILES_NAME                   = "MILES";
    public  static final int    FEET                         = 3;
    public  static final String FEET_NAME                    = "FEET";
    public  static final int    NAUTICAL_MILES               = 4;
    public  static final String NAUTICAL_MILES_NAME          = "NAUTICAL_MILES";

    // ------------------------------------------------------------------------
    
    public  static final String SPEED_KPH                    = "kph";
    public  static final String SPEED_MPH                    = "mph";
    public  static final String SPEED_KNOTS                  = "knots";

    public static String getSpeedAbbr(int distUnits)
    {
        switch (distUnits) {
            case GeoPoint.KILOMETERS: 
                return SPEED_KPH; 
            case GeoPoint.MILES: 
                return SPEED_MPH; 
            case GeoPoint.NAUTICAL_MILES: 
                return SPEED_KNOTS; 
            default: 
                return SPEED_MPH; 
        }
    }

    // ------------------------------------------------------------------------
    
    public  static final String DISTANCE_KM                  = "km";
    public  static final String DISTANCE_MILES               = "miles";
    public  static final String DISTANCE_KNOTS               = "knots"; // for distance?

    public static String getDistanceAbbr(int distUnits)
    {
        switch (distUnits) {
            case GeoPoint.KILOMETERS: 
                return DISTANCE_KM; 
            case GeoPoint.MILES: 
                return DISTANCE_MILES; 
            case GeoPoint.NAUTICAL_MILES: 
                return DISTANCE_KNOTS; 
            default: 
                return DISTANCE_MILES; 
        }
    }
    
    // ------------------------------------------------------------------------

    private double latitude  = 0.0;
    private double longitude = 0.0;

    public GeoPoint()
    {
    }

    public GeoPoint(GeoPoint gp)
    {
        this();
        this.setLatitude(gp.getLatitude());
        this.setLongitude(gp.getLongitude());
    }

    public GeoPoint(double latitude, double longitude)
    {
        this();
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }
    
    public GeoPoint(
        double latDeg, double latMin, double latSec, 
        double lonDeg, double lonMin, double lonSec)
    {
        this();
        this.setLatitude( latDeg, latMin, latSec);
        this.setLongitude(lonDeg, lonMin, lonSec);
    }
 
    // ------------------------------------------------------------------------

    public boolean isValid()
    {
        double latAbs = Math.abs(this.getLatitude());
        double lonAbs = Math.abs(this.getLongitude());
        if ((latAbs >= MAX_LATITUDE) || (lonAbs >= MAX_LONGITUDE)) {
            // invalid values
            return false;
        } else
        if ((latAbs <= 0.0002) && (lonAbs <= 0.0002)) {
            // small square off the coast of Africa (Ghana)
            return false;
        } else {
            return true;
        }
    }
    
    // ------------------------------------------------------------------------

    public void setLatitude(double deg, double min, double sec)
    {
        this.setLatitude(GeoPoint.convertDmsToDec(deg, min, sec));
    }

    public void setLatitude(double lat)
    {
        this.latitude = lat;
    }
    
    public double getLatitude()
    {
        return this.latitude;
    }
    
    public double getLatitudeRadians()
    {
        return this.getLatitude() * RADIANS;
    }

    public String getLatitudeString()
    {
        return getLatitudeString(false);
    }

    public String getLatitudeString(boolean dms)
    {
        return formatLatitude(this.getLatitude(), dms);
    }
    
    public static String formatLatitude(double lat, boolean dms)
    {
        int fmt = FORMAT_LATITUDE | (dms? FORMAT_DMS : FORMAT_DEC);
        return formatCoord(lat, fmt);
    }
    
    // ------------------------------------------------------------------------

    public void setLongitude(double deg, double min, double sec)
    {
        this.setLongitude(GeoPoint.convertDmsToDec(deg, min, sec));
    }

    public void setLongitude(double lon)
    {
        this.longitude = lon;
    }

    public double getLongitude()
    {
        return this.longitude;
    }
    
    public double getLongitudeRadians()
    {
        return this.getLongitude() * RADIANS;
    }

    public String getLongitudeString()
    {
        return getLongitudeString(false);
    }

    public String getLongitudeString(boolean dms)
    {
        return formatLongitude(this.getLongitude(), dms);
    }
    
    public static String formatLongitude(double lon, boolean dms)
    {
        int fmt = FORMAT_LONGITUDE | (dms? FORMAT_DMS : FORMAT_DEC);
        return formatCoord(lon, fmt);
    }
    
    // ------------------------------------------------------------------------

    private static final double POW_24  =    16777216.0; // 2^24
    private static final double POW_28  =   268435456.0; // 2^28
    private static final double POW_32  =  4294967296.0; // 2^32

    public static byte[] encodeGeoPoint(GeoPoint gp, byte enc[], int ofs, int len)
    {
        /* null/empty bytes */
        if (enc == null) {
            return null;
        }
        
        /* offset/length out-of-range */
        if (len < 0) { len = enc.length; }
        if ((ofs + len) > enc.length) {
            return null;
        }
        
        /* not enough bytes to encode */
        if (len < 6) {
            return null;
        }

        /* lat/lon */
        double lat = gp.getLatitude();
        double lon = gp.getLongitude();
        
        /* standard resolution */
        if ((len >= 6) && (len < 8)) {
            // LL-LL-LL LL-LL-LL
            long rawLat24 = (lat != 0.0)? Math.round((lat -  90.0) * (POW_24 / -180.0)) : 0L;
            long rawLon24 = (lon != 0.0)? Math.round((lon + 180.0) * (POW_24 /  360.0)) : 0L;
            long rawAccum = ((rawLat24 << 24) & 0xFFFFFF000000L) | (rawLon24 & 0xFFFFFFL);
            enc[ofs + 0] = (byte)((rawAccum >> 40) & 0xFF);
            enc[ofs + 1] = (byte)((rawAccum >> 32) & 0xFF);
            enc[ofs + 2] = (byte)((rawAccum >> 24) & 0xFF);
            enc[ofs + 3] = (byte)((rawAccum >> 16) & 0xFF);
            enc[ofs + 4] = (byte)((rawAccum >>  8) & 0xFF);
            enc[ofs + 5] = (byte)((rawAccum      ) & 0xFF);
            return enc;
        } 
        
        /* high resolution */
        if (len >= 8) {
            // LL-LL-LL-LL LL-LL-LL-LL
            long rawLat32 = (lat != 0.0)? Math.round((lat -  90.0) * (POW_32 / -180.0)) : 0L;
            long rawLon32 = (lon != 0.0)? Math.round((lon + 180.0) * (POW_32 /  360.0)) : 0L;
            long rawAccum = ((rawLat32 << 32) & 0xFFFFFFFF00000000L) | (rawLon32 & 0xFFFFFFFFL);
            enc[ofs + 0] = (byte)((rawAccum >> 56) & 0xFF);
            enc[ofs + 1] = (byte)((rawAccum >> 48) & 0xFF);
            enc[ofs + 2] = (byte)((rawAccum >> 40) & 0xFF);
            enc[ofs + 3] = (byte)((rawAccum >> 32) & 0xFF);
            enc[ofs + 4] = (byte)((rawAccum >> 24) & 0xFF);
            enc[ofs + 5] = (byte)((rawAccum >> 16) & 0xFF);
            enc[ofs + 6] = (byte)((rawAccum >>  8) & 0xFF);
            enc[ofs + 7] = (byte)((rawAccum      ) & 0xFF);
            return enc;
        }
       
        /* will never reach here */
        return null;

    }

    public static GeoPoint decodeGeoPoint(byte enc[], int ofs, int len)
    {
        
        /* null/empty bytes */
        if (enc == null) {
            return null;
        }
        
        /* offset/length out-of-range */
        if (len < 0) { len = enc.length; }
        if ((ofs + len) > enc.length) {
            return null;
        }
        
        /* not enough bytes to decode */
        if (len < 6) {
            return null;
        }
        
        /* standard resolution */
        if ((len >= 6) && (len < 8)) {
            // LL-LL-LL LL-LL-LL
            long rawLat24 = (((long)enc[ofs+0] & 0xFF) << 16) | (((long)enc[ofs+1] & 0xFF) << 8) | ((long)enc[ofs+2] & 0xFF);
            long rawLon24 = (((long)enc[ofs+3] & 0xFF) << 16) | (((long)enc[ofs+4] & 0xFF) << 8) | ((long)enc[ofs+5] & 0xFF);
            double lat = (rawLat24 != 0L)? (((double)rawLat24 * (-180.0 / POW_24)) +  90.0) : 0.0;
            double lon = (rawLon24 != 0L)? (((double)rawLon24 * ( 360.0 / POW_24)) - 180.0) : 0.0;
            return new GeoPoint(lat, lon);
        }
        
        /* high resolution */
        if (len >= 8) {
            // LL-LL-LL-LL LL-LL-LL-LL
            long rawLat32 = (((long)enc[ofs+0] & 0xFF) << 24) | (((long)enc[ofs+1] & 0xFF) << 16) | (((long)enc[ofs+2] & 0xFF) << 8) | ((long)enc[ofs+3] & 0xFF);
            long rawLon32 = (((long)enc[ofs+4] & 0xFF) << 24) | (((long)enc[ofs+5] & 0xFF) << 16) | (((long)enc[ofs+6] & 0xFF) << 8) | ((long)enc[ofs+7] & 0xFF);
            double lat = (rawLat32 != 0L)? (((double)rawLat32 * (-180.0 / POW_32)) +  90.0) : 0.0;
            double lon = (rawLon32 != 0L)? (((double)rawLon32 * ( 360.0 / POW_32)) - 180.0) : 0.0;
            return new GeoPoint(lat, lon);
        }
        
        /* will never reach here */
        return null;
        
    }
    
    // ------------------------------------------------------------------------

    public static double convertDistanceUnits(double v, int fromUnits, int toUnits)
    {
        if (fromUnits == toUnits) {
            return v;
        } else {
            return convertFromKilometers(convertToKilometers(v, fromUnits), toUnits);
        }
    }
   
    public static double convertToKilometers(double v, int units)
    {
        switch (units) {
            case KILOMETERS     : return v;
            case METERS         : return v / 1000.0;
            case MILES          : return v / MILES_PER_KILOMETER;
            case FEET           : return v / FEET_PER_KILOMETER;
            case NAUTICAL_MILES : return v / NAUTICAL_MILES_PER_KILOMETER;
        }
        return v; // default KILOMETERS
    }
    
    public static double convertFromKilometers(double v, int units)
    {
        switch (units) {
            case KILOMETERS     : return v;
            case METERS         : return v * 1000.0;
            case MILES          : return v * MILES_PER_KILOMETER;
            case FEET           : return v * FEET_PER_KILOMETER;
            case NAUTICAL_MILES : return v * NAUTICAL_MILES_PER_KILOMETER;
        }
        return v; // default KILOMETERS
    }
    
    public static String getDistanceUnitsName(int units, String dft)
    {
        switch (units) {
            case KILOMETERS     : return KILOMETERS_NAME;
            case METERS         : return METERS_NAME;
            case MILES          : return MILES_NAME;
            case FEET           : return FEET_NAME;
            case NAUTICAL_MILES : return NAUTICAL_MILES_NAME;
        }
        return dft;
    }
    
    public static int getDistanceUnitsCode(String name, int dft)
    {
        if (KILOMETERS_NAME.equalsIgnoreCase(name)) {
            return KILOMETERS;
        } else
        if (METERS_NAME.equalsIgnoreCase(name)) {
            return METERS;
        } else
        if (MILES_NAME.equalsIgnoreCase(name)) {
            return MILES;
        } else
        if (FEET_NAME.equalsIgnoreCase(name)) {
            return FEET;
        } else
        if (NAUTICAL_MILES_NAME.equalsIgnoreCase(name)) {
            return NAUTICAL_MILES;
        } else {
            return dft;
        }
    }
    
    // ------------------------------------------------------------------------

    public double distanceToPoint(GeoPoint gp, int units)
    {
        double d = this.kilometersToPoint(gp);
        if (d >= 0.0) {
            return convertFromKilometers(d, units);
        } else {
            return d; // error
        }
    }

    public double radiansToPoint(GeoPoint dest)
    {
        // Flat plane approximations:
        //   http://mathforum.org/library/drmath/view/51833.html
        //   http://mathforum.org/library/drmath/view/62720.html
        if (dest == null) {
            // you pass in 'null', you deserver what you get
            return Double.NaN;
        } else
        if (this.equals(dest)) {
            // If the points are equals, the radians would be NaN
            return 0.0;
        } else {
            try {
                double lat1 = this.getLatitudeRadians(), lon1 = this.getLongitudeRadians();
                double lat2 = dest.getLatitudeRadians(), lon2 = dest.getLongitudeRadians();
                double rad  = 0.0;
                if (UseHaversineDistanceFormula) {
                    // Haversine formula:
                    // "The Haversine formula may be more accurate for small distances"
                    // See: http://www.census.gov/cgi-bin/geo/gisfaq?Q5.1
                    //      http://mathforum.org/library/drmath/view/51879.html
                    // Also, use of the Haversine formula is about twice as fast as the Law of Cosines
                    double dlat = lat2 - lat1;
                    double dlon = lon2 - lon1;
                    double a    = SQ(Math.sin(dlat/2.0)) + (Math.cos(lat1) * Math.cos(lat2) * SQ(Math.sin(dlon/2.0)));
                    rad = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
                } else {
                    // Law of Cosines for Spherical Trigonometry:
                    // Per http://www.census.gov/cgi-bin/geo/gisfaq?Q5.1 this method isn't recommended:
                    //  "Although this formula is mathematically exact, it is unreliable for
                    //   small distances because the inverse cosine is ill-conditioned."
                    // Note: this problem appears to be less of an issue in Java.  The amount of error
                    // between Law-of-Cosine and Haversine formulas appears small even when calculating
                    // distance aven as low as 1.5 meters.
                    double dlon = lon2 - lon1;
                    rad = Math.acos((Math.sin(lat1) * Math.sin(lat2)) + (Math.cos(lat1) * Math.cos(lat2) * Math.cos(dlon)));
                }
                
                return rad;
                
            } catch (Throwable t) { // trap any Math error
                
                return Double.NaN;
                
            }
        }
    }

    public double kilometersToPoint(GeoPoint gp)
    {
        double radians = this.radiansToPoint(gp);
        return !Double.isNaN(radians)? (EARTH_MEAN_RADIUS_KM * radians) : Double.NaN;
    }
    
    // ------------------------------------------------------------------------

    // convert radius to delta lat/lon
    public GeoPoint getRadiusDeltaPoint(double radiusMeters)
    {
        double a = EARTH_EQUATORIAL_RADIUS_KM * 1000.0;
        double b = EARTH_POLOR_RADIUS_KM * 1000.0;
        double lat = this.getLatitudeRadians();
        // r(T) = (a^2) / sqrt((a^2)*(cos(T)^2) + (b^2)*(sin(T)^2))
        double r = SQ(a) / Math.sqrt((SQ(a) * SQ(Math.cos(lat))) + (SQ(b) * SQ(Math.sin(lat))));
        // dlat = (180 * R) / (PI * r);
        double dlat = (180.0 * radiusMeters) / (Math.PI * r);
        // dlon = dlat / cos(lat);
        double dlon = dlat / Math.cos(lat);
        return new GeoPoint(dlat, dlon);
    }
    
    // ------------------------------------------------------------------------

    public boolean isNearby(GeoPoint gp, double deltaKilometers)
    {
        return (this.kilometersToPoint(gp) <= deltaKilometers);
    }

    public boolean isNearby(GeoPoint gp, double delta, int units)
    {
        return (this.distanceToPoint(gp, units) <= delta);
    }

    // ------------------------------------------------------------------------
    
    private static String DIRECTION[] = { NORTH_ABBR, NE_ABBR, EAST_ABBR, SE_ABBR, SOUTH_ABBR, SW_ABBR, WEST_ABBR, NW_ABBR };
    
    public static String GetHeadingString(double heading)
    {
        if (!Double.isNaN(heading) && (heading >= 0.0)) {
            int h = (int)Math.round(heading / 45.0) % 8;
            return DIRECTION[(h > 7)? 0 : h];
        } else {
            return "";
        }
    }

    public double headingToPoint(GeoPoint dest)
    {
        // Assistance from:
        //   http://mathforum.org/library/drmath/view/55417.html
        //   http://williams.best.vwh.net/avform.htm
        try {              
            double lat1 = this.getLatitudeRadians(), lon1 = this.getLongitudeRadians();
            double lat2 = dest.getLatitudeRadians(), lon2 = dest.getLongitudeRadians();
            double dist = this.radiansToPoint(dest);
            double rad  = Math.acos((Math.sin(lat2) - (Math.sin(lat1) * Math.cos(dist))) / (Math.sin(dist) * Math.cos(lat1)));
            if (Math.sin(lon2 - lon1) < 0) { rad = (2.0 * Math.PI) - rad; }
            double deg  = rad / RADIANS;
            return deg;
        } catch (Throwable t) { // trap any Math error
            Print.logException("headingToPoint", t);
            return 0.0;
        }
    }
    
    // ------------------------------------------------------------------------
    
    public Object clone()
    {
        return new GeoPoint(this);
    }

    public String toString()
    {
        return this.getLatitudeString() + PointSeparator + this.getLongitudeString();
    }

    public String toString(boolean dms)
    {
        return this.getLatitudeString(dms) + PointSeparator + this.getLongitudeString(dms);
    }
    
    public boolean equals(Object other)
    {
        if (other instanceof GeoPoint) {
            GeoPoint gp = (GeoPoint)other;
            double deltaLat = Math.abs(gp.getLatitude()  - this.getLatitude() );
            double deltaLon = Math.abs(gp.getLongitude() - this.getLongitude());
            return ((deltaLat < EPSILON) && (deltaLon < EPSILON));
        } else {
            return false;
        }
    }
    
    // ------------------------------------------------------------------------
    
    public static double convertDmsToDec(int deg, int min, int sec)
    {
        return GeoPoint.convertDmsToDec((double)deg, (double)min, (double)sec);
    }
    
    public static double convertDmsToDec(double deg, double min, double sec)
    {
        double sign = (deg >= 0.0)? 1.0 : -1.0;
        double d = Math.abs(deg);
        double m = Math.abs(min / 60.0);
        double s = Math.abs(sec / 3600.0);
        return sign * (d + m + s);
    }
    
    // ------------------------------------------------------------------------

    public static String formatCoord(double location)
    {
        return GeoPoint.formatCoord(location, FORMAT_DEC);
    }

    public static String formatCoord(double loc, int fmt)
    {
        if ((fmt & FORMAT_TYPE_MASK) == FORMAT_DMS) {
            int    sgn   = (loc >= 0.0)? 1 : -1;
            double abs   = Math.abs(loc);
            int    deg   = (int)abs;
            double accum = (abs - (double)deg) * 60.0;
            int    min   = (int)accum;
            accum = (accum - (double)min) * 60.0;
            int    sec   = (int)accum;
            StringBuffer sb = new StringBuffer();
            sb.append(StringTools.format(deg,"##0")).append("-");
            sb.append(StringTools.format(min,"00")).append("'");
            sb.append(StringTools.format(sec,"00")).append("\"");
            if ((fmt & FORMAT_AXIS_MASK) == FORMAT_LATITUDE) {
                sb.append((sgn >= 0)? NORTH_ABBR : SOUTH_ABBR);
            } else
            if ((fmt & FORMAT_AXIS_MASK) == FORMAT_LONGITUDE) {
                sb.append((sgn >= 0)? EAST_ABBR : WEST_ABBR);
            }
            return sb.toString();
        } else {
            // NOTE: European locale may attempt to format this value with "," instead of "."
            // This needs to be "." in order to work for CSV files, etc.
            return StringTools.format(loc, "###0.00000");
        }
    }

    // ------------------------------------------------------------------------
    
}
