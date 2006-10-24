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
//  This class provides many Date/Time utilities
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opendmtp.util;

import java.util.*;
import java.text.*;
import java.awt.*;
import java.lang.ref.SoftReference;

public class DateTime
    implements Comparable, Cloneable
{
    
    // ------------------------------------------------------------------------

    public static String DEFAULT_TIMEZONE       = "US/Pacific";
    public static String GMT_TIMEZONE           = "GMT";
    
    // ------------------------------------------------------------------------
    
    public static final long HOURS_PER_DAY      = 24L;
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long MINUTES_PER_HOUR   = 60L;
    public static final long DAYS_PER_WEEK      = 7L;
    public static final long SECONDS_PER_HOUR   = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    public static final long MINUTES_PER_DAY    = HOURS_PER_DAY * MINUTES_PER_HOUR;
    public static final long SECONDS_PER_DAY    = MINUTES_PER_DAY * SECONDS_PER_MINUTE;
    public static final long MINUTES_PER_WEEK   = DAYS_PER_WEEK * MINUTES_PER_DAY;

    public static long DaySeconds(long days)
    {
        return days * SECONDS_PER_DAY;
    }

    public static long DaySeconds(double days)
    {
        return (long)Math.round(days * (double)SECONDS_PER_DAY);
    }
    
    public static long HourSeconds(long hours)
    {
        return hours * SECONDS_PER_HOUR;
    }
    
    public static long MinuteSeconds(long minutes)
    {
        return minutes * SECONDS_PER_MINUTE;
    }

    // ------------------------------------------------------------------------
    
    public static final int JAN       = 0;
    public static final int FEB       = 1;
    public static final int MAR       = 2;
    public static final int APR       = 3;
    public static final int MAY       = 4;
    public static final int JUN       = 5;
    public static final int JUL       = 6;
    public static final int AUG       = 7;
    public static final int SEP       = 8;
    public static final int OCT       = 9;
    public static final int NOV       = 10;
    public static final int DEC       = 11;
    
    public static final int JANUARY   = JAN;
    public static final int FEBRUARY  = FEB;
    public static final int MARCH     = MAR;
    public static final int APRIL     = APR;
  //public static final int MAY       = MAY;
    public static final int JUNE      = JUN;
    public static final int JULY      = JUL;
    public static final int AUGUST    = AUG;
    public static final int SEPTEMBER = SEP;
    public static final int OCTOBER   = OCT;
    public static final int NOVEMBER  = NOV;
    public static final int DECEMBER  = DEC;
    
    private static final String MONTH_NAME[][] = {
        { "January"  , "Jan" },
        { "February" , "Feb" },
        { "March"    , "Mar" },
        { "April"    , "Apr" },
        { "May"      , "May" },
        { "June"     , "Jun" },
        { "July"     , "Jul" },
        { "August"   , "Aug" },
        { "September", "Sep" },
        { "October"  , "Oct" },
        { "November" , "Nov" },
        { "December" , "Dec" },
    };
    
    public static String getMonthName(int mon0, boolean abbrev)
    {
        if ((mon0 >= JANUARY) && (mon0 <= DECEMBER)) {
            return abbrev? MONTH_NAME[mon0][1] : MONTH_NAME[mon0][0];
        } else {
            return "";
        }
    }
    
    public static String[] getMonthNames(boolean abbrev)
    {
        String mo[] = new String[MONTH_NAME.length];
        for (int i = 0; i < MONTH_NAME.length; i++) {
            mo[i] = DateTime.getMonthName(i, abbrev);
        }
        return mo;
    }
    
    public static Map getMonthNameMap(boolean abbrev)
    {
        Map map = new OrderedMap();
        for (int i = 0; i < MONTH_NAME.length; i++) {
            map.put(DateTime.getMonthName(i, abbrev), new Integer(i));
        }
        return map;
    }

    // ------------------------------------------------------------------------

    private static final int MONTH_DAYS[] = {
        31, // Jan
        29, // Feb
        31, // Mar
        30, // Apr
        31, // May
        30, // Jun
        31, // Jul
        31, // Aug
        30, // Sep
        31, // Oct
        30, // Nov        
        31, // Dec
    };
    
    public static int getDaysInMonth(TimeZone tz, int m0, int year)
    {
        DateTime dt = new DateTime(tz, year, m0, 1);
        return DateTime.getMaxMonthDayCount(m0, dt.isLeapYear(tz));
    }
    
    public static int getMaxMonthDayCount(int m0, boolean isLeapYear)
    {
        int d = ((m0 >= 0) && (m0 < DateTime.MONTH_DAYS.length))? DateTime.MONTH_DAYS[m0] : 31;
        return ((m0 != FEBRUARY) || isLeapYear)? d : 28;
    }
    
    // ------------------------------------------------------------------------
    
    public static final int SUN       = 0;
    public static final int MON       = 1;
    public static final int TUE       = 2;
    public static final int WED       = 3;
    public static final int THU       = 4;
    public static final int FRI       = 5;
    public static final int SAT       = 6;
    
    public static final int SUNDAY    = SUN;
    public static final int MONDAY    = MON;
    public static final int TUESDAY   = TUE;
    public static final int WEDNESDAY = WED;
    public static final int THURSDAY  = THU;
    public static final int FRIDAY    = FRI;
    public static final int SATURDAY  = SAT;
    
    private static final String DAY_NAME[][] = {
        { "Sunday"   , "Sun" },
        { "Monday"   , "Mon" },
        { "Tuesday"  , "Tue" },
        { "Wednesday", "Wed" },
        { "Thursday" , "Thu" },
        { "Friday"   , "Fri" },
        { "Saturday" , "Sat" },
    };
    
    public static String getDayName(int day, boolean abbrev)
    {
        if ((day >= SUNDAY) && (day <= SATURDAY)) {
            return abbrev? DAY_NAME[day][1] : DAY_NAME[day][0];
        } else {
            return "";
        }
    }
    
    public static String[] getDayNames(boolean abbrev)
    {
        String dy[] = new String[DAY_NAME.length];
        for (int i = 0; i < DAY_NAME.length; i++) {
            dy[i] = DateTime.getDayName(i, abbrev);
        }
        return dy;
    }
    
    public static Map getDayNameMap(boolean abbrev)
    {
        Map map = new OrderedMap();
        for (int i = 0; i < DAY_NAME.length; i++) {
            map.put(DateTime.getDayName(i, abbrev), new Integer(i));
        }
        return map;
    }

    // ------------------------------------------------------------------------

    public static String[] getHours(boolean hr24)
    {
        String hrs[] = new String[hr24? 24 : 12];
        for (int i = 0; i < hrs.length; i++) {
            hrs[i] = String.valueOf(i);
        }
        return hrs;
    }

    public static String[] getMinutes()
    {
        String min[] = new String[60];
        for (int i = 0; i < min.length; i++) {
            min[i] = String.valueOf(i);
        }
        return min;
    }
    
    // ------------------------------------------------------------------------

    public static String toString(long timeSec)
    {
        return (new java.util.Date(timeSec * 1000L)).toString();
    }

    public static long getCurrentTimeSec()
    {
        // Number of seconds since the 'epoch' January 1, 1970, 00:00:00 GMT
        return getCurrentTimeMillis() / 1000L;
    }

    public static long getCurrentTimeMillis()
    {
        // Number of milliseconds since the 'epoch' January 1, 1970, 00:00:00 GMT
        return System.currentTimeMillis();
    }
    
    public static boolean isRecentSec(long timeSec, long lapseSec)
    {
        Print.logDebug("timeSec: " + timeSec);
        Print.logDebug("getCurrentTimeSec: " + DateTime.getCurrentTimeSec());
        return (timeSec >= (DateTime.getCurrentTimeSec() - lapseSec));
    }

    // ------------------------------------------------------------------------

    public static DateTime getMinDate()
    {
        return new DateTime(1);
    }
    
    public static DateTime getMaxDate()
    {
        return new DateTime(null, 2020, 11, 31);
    }

    // ------------------------------------------------------------------------

    private TimeZone timeZone   = null;
    private long     timeMillis = 0L; // ms since January 1, 1970, 00:00:00 GMT

    public DateTime()
    {
        this.setTimeMillis(getCurrentTimeMillis());
    }

    public DateTime(TimeZone tz)
    {
        this();
        this.setTimeZone(tz);
    }

    public DateTime(Date date)
    {
        this.setTimeMillis(date.getTime());
    }

    public DateTime(Date date, TimeZone tz)
    {
        this.setTimeMillis(date.getTime());
        this.setTimeZone(tz);
    }
    
    public DateTime(TimeZone tz, int year, int month0, int day)
    {
        this.setDate(tz, year, month0, day);
    }
    
    public DateTime(TimeZone tz, int year, int month0, int day, int hour24, int minute, int second)
    {
        this.setDate(tz, year, month0, day, hour24, minute, second);
    }

    public DateTime(long timeSec)
    {
        this.setTimeSec(timeSec);
    }

    public DateTime(long timeSec, TimeZone tz)
    {
        this.setTimeSec(timeSec);
        this.setTimeZone(tz);
    }

    public DateTime(String d)
        throws ParseException
    {
        this.setDate(d);
    }
    
    public DateTime(DateTime dt)
    {
        this.timeMillis = dt.timeMillis;
        this.timeZone   = dt.timeZone;
    }
    
    public DateTime(DateTime dt, long deltaOffsetSec)
    {
        this.timeMillis = dt.timeMillis + (deltaOffsetSec * 1000L);
        this.timeZone   = dt.timeZone;
    }
    
    // ------------------------------------------------------------------------

    public java.util.Date getDate()
    {
        return new java.util.Date(this.getTimeMillis());
    }

    public void setDate(TimeZone tz, int year, int month0, int day)
    {
        this.setDate(tz, year, month0, day, 0, 0, 0);
    }

    public void setDate(TimeZone tz, int year, int month0, int day, int hour24, int minute, int second)
    {
        this.setTimeZone(tz);
        Calendar cal = new GregorianCalendar(this._timeZone(tz));
        cal.set(year, month0, day, hour24, minute, second);
        Date date = cal.getTime();
        this.setTimeMillis(date.getTime());
    }
        
    public void setDate(String d)
        throws ParseException
    {
        this.setDate(d, (TimeZone)null);
    }
        
    public void setDate(String d, TimeZone dftTMZ)
        throws ParseException
    {
        String ds[] = StringTools.parseString(d, ' ');
        String dt = (ds.length > 0)? ds[0] : "";
        String tm = (ds.length > 1)? ds[1] : "";
        String tz = (ds.length > 2)? ds[2] : "";
        // Valid format: "YYYY/MM/DD [HH:MM:SS] [PST]"
        
        /* time-zone */
        TimeZone timeZone = null;
        if (ds.length > 2) {
            if (DateTime.isValidTimeZone(tz)) {
                timeZone = DateTime.getTimeZone(tz);
            } else {
                throw new ParseException("Invalid TimeZone: " + tz, 0);
            }
        } else
        if ((ds.length > 1) && DateTime.isValidTimeZone(tm)) {
            tz = tm;
            tm = "";
            timeZone = DateTime.getTimeZone(tz);
        } else {
            timeZone = (dftTMZ != null)? dftTMZ : this.getTimeZone();
        }
        this.setTimeZone(timeZone);
        Calendar calendar = new GregorianCalendar(timeZone);
        
        /* date */
        int yr = -1, mo = -1, dy = -1;
        int d1 = dt.indexOf('/'), d2 = (d1 > 0)? dt.indexOf('/', d1 + 1) : -1;
        if ((d1 > 0) && (d2 > d1)) {
            
            /* year */
            String YR = dt.substring(0, d1);
            yr = StringTools.parseInt(YR, -1);
            if ((yr >=  0) && (yr <= 49)) { yr += 2000; } else
            if ((yr >= 50) && (yr <= 99)) { yr += 1900; }
            if ((yr < 1900) || (yr > 2100)) {
                throw new ParseException("Date/Year out of range: " + YR, 0);
            }
            
            /* month */
            String MO = dt.substring(d1 + 1, d2);
            mo = StringTools.parseInt(MO, -1) - 1; // 0 indexed
            if ((mo < 0) || (mo > 11)) {
                throw new ParseException("Date/Month out of range: " + MO, 0);
            }
            
            /* seconds */
            String DY = dt.substring(d2 + 1);
            dy = StringTools.parseInt(DY, -1);
            if ((dy < 1) || (dy > 31)) {
                throw new ParseException("Date/Day out of range: " + DY, 0);
            }
            
        } else {
            
            throw new ParseException("Invalid date format (Y/M/D): " + dt, 0);
            
        }
        
        /* time */
        if (tm.equals("")) {
            
            calendar.set(yr, mo, dy);
            
        } else {
            
            int hr = -1, mn = -1, sc = -1;
            int t1 = tm.indexOf(':'), t2 = (t1 > 0)? tm.indexOf(':', t1 + 1) : -1;
            if (t1 > 0) {
                
                /* hour */
                String HR = tm.substring(0, t1);
                hr = StringTools.parseInt(HR, -1);
                if ((hr < 0) || (hr > 23)) {
                    throw new ParseException("Time/Hour out of range: " + HR, 0);
                }
                if (t2 > t1) {
                    
                    /* minute */
                    String MN = tm.substring(t1 + 1, t2);
                    mn = StringTools.parseInt(MN, -1);
                    if ((mn < 0) || (mn > 59)) {
                        throw new ParseException("Time/Minute out of range: " + MN, 0);
                    }
                    
                    /* second */
                    String SC = tm.substring(t2 + 1);
                    sc = StringTools.parseInt(SC, -1);
                    if ((sc < 0) || (sc > 59)) {
                        throw new ParseException("Time/Second out of range: " + SC, 0);
                    }
                    
                    calendar.set(yr, mo, dy, hr, mn, sc);
                    
                } else {
                    
                    /* minute */
                    String MN = tm.substring(t1 + 1);
                    mn = StringTools.parseInt(MN, -1);
                    if ((mn < 0) || (mn > 59)) {
                        throw new ParseException("Time/Minute out of range: " + MN, 0);
                    }
                    
                    calendar.set(yr, mo, dy, hr, mn);
                    
                }
            } else {
                
                throw new ParseException("Invalid time format (H:M:S): " + tm, 0);
                
            }
        }
        
        /* ok */
        this.setTimeMillis(calendar.getTime().getTime());
        
    }

    public boolean setParsedDate_formatted(String d) // does not work!
    {
        try {
            java.util.Date date = DateFormat.getDateInstance().parse(d);
            this.setTimeMillis(date.getTime());
            return true;
        } catch (ParseException pe) {
            Print.logStackTrace("Unable to parse date: " + d, pe);
            this.setTimeMillis(0L);
            return false;
        }
    }
    
    // ------------------------------------------------------------------------
    
    public Calendar getCalendar(TimeZone tz)
    {
        Calendar c = new GregorianCalendar(this._timeZone(tz));
        c.setTimeInMillis(this.getTimeMillis());
        return c;
    }

    private int _get(TimeZone tz, int value)
    {
        return this.getCalendar(tz).get(value);
    }
    
    public int getMonth0(TimeZone tz)
    {
        // return 0..11
        return this._get(tz, Calendar.MONTH); // 0 indexed
    }
    
    public int getMonth1(TimeZone tz)
    {
        // return 1..12
        return this.getMonth0(tz) + 1;
    }
    
    public int getDayOfMonth(TimeZone tz)
    {
        // return 1..31
        return this._get(tz, Calendar.DAY_OF_MONTH);
    }
    
    public int getDaysInMonth(TimeZone tz)
    {
        return DateTime.getDaysInMonth(tz, this.getMonth0(tz), this.getYear(tz));
    }
    
    public int getDayOfWeek(TimeZone tz)
    {
        // return 0..6
        return this._get(tz, Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
    }
   
    public int getYear(TimeZone tz)
    {
        return this._get(tz, Calendar.YEAR);
    }
    
    public boolean isLeapYear(TimeZone tz)
    {
        GregorianCalendar gc = (GregorianCalendar)this.getCalendar(tz);
        return gc.isLeapYear(gc.get(Calendar.YEAR));
    }
   
    public int getHour24(TimeZone tz)
    {
        return this._get(tz, Calendar.HOUR_OF_DAY);
    }
   
    public int getHour12(TimeZone tz)
    {
        return this._get(tz, Calendar.HOUR);
    }
   
    public boolean isAM(TimeZone tz)
    {
        return this._get(tz, Calendar.AM_PM) == Calendar.AM;
    }
   
    public boolean isPM(TimeZone tz)
    {
        return this._get(tz, Calendar.AM_PM) == Calendar.PM;
    }
   
    public int getMinute(TimeZone tz)
    {
        return this._get(tz, Calendar.MINUTE);
    }
   
    public int getSecond(TimeZone tz)
    {
        return this._get(tz, Calendar.SECOND);
    }
    
    // ------------------------------------------------------------------------

    public long getTimeSec()
    {
        return this.getTimeMillis() / 1000L;
    }

    public void setTimeSec(long timeSec)
    {
        this.timeMillis = timeSec * 1000L;
    }

    // ------------------------------------------------------------------------

    public long getTimeMillis()
    {
        return this.timeMillis;
    }

    public void setTimeMillis(long timeMillis)
    {
        this.timeMillis = timeMillis;
    }

    // ------------------------------------------------------------------------
    
    public long getDayStart(TimeZone tz)
    {
        if (tz == null) { tz = _timeZone(tz); }
        Calendar c  = this.getCalendar(tz);
        Calendar nc = new GregorianCalendar(tz);
        nc.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        return nc.getTime().getTime() / 1000L;
    }
    
    public long getDayEnd(TimeZone tz)
    {
        if (tz == null) { tz = _timeZone(tz); }
        Calendar c  = this.getCalendar(tz);
        Calendar nc = new GregorianCalendar(tz);
        nc.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        return nc.getTime().getTime() / 1000L;
    }

    // ------------------------------------------------------------------------

    public long getDayStartGMT()
    {
        // GMT TimeZone
        return (this.getTimeSec() / SECONDS_PER_DAY) * SECONDS_PER_DAY;
    }

    public long getDayEndGMT()
    {
        // GMT TimeZone
        return this.getDayStartGMT() + SECONDS_PER_DAY - 1L;
    }

    // ------------------------------------------------------------------------

    public boolean after(DateTime dt)
    {
        return this.after(dt, false);
    }

    public boolean after(DateTime dt, boolean inclusive)
    {
        if (dt == null) {
            return true; // arbitrary
        } else
        if (inclusive) {
            return (this.getTimeMillis() >= dt.getTimeMillis());
        } else {
            return (this.getTimeMillis() > dt.getTimeMillis());
        }
    }

    public boolean before(DateTime dt)
    {
        return this.before(dt, false);
    }

    public boolean before(DateTime dt, boolean inclusive)
    {
        if (dt == null) {
            return false; // arbitrary
        } else
        if (inclusive) {
            return (this.getTimeMillis() <= dt.getTimeMillis());
        } else {
            return (this.getTimeMillis() < dt.getTimeMillis());
        }
    }
    
    public boolean equals(Object obj) 
    {
        if (obj instanceof DateTime) {
            return (this.getTimeMillis() == ((DateTime)obj).getTimeMillis());
        } else {
            return false;
        }
    }
    
    public int compareTo(Object other)
    {
        if (other instanceof DateTime) {
            long otherTime = ((DateTime)other).getTimeMillis();
            long thisTime  = this.getTimeMillis();
            if (thisTime < otherTime) { return -1; }
            if (thisTime > otherTime) { return  1; }
            return 0;
        } else {
            return -1;
        }
    }
    
    // ------------------------------------------------------------------------
        
    protected TimeZone _timeZone(TimeZone tz)
    {
        return (tz != null)? tz : this.getTimeZone();
    }
    
    public TimeZone getTimeZone()
    {
        return (this.timeZone != null)? this.timeZone : DateTime.getDefaultTimeZone();
    }
    
    public void setTimeZone(TimeZone tz)
    {
        this.timeZone = tz;
    }
    
    public void setTimeZone(String tz)
    {
        this.setTimeZone(DateTime.getTimeZone(tz, null));
    }
    
    public interface TimeZoneProvider
    {
        public TimeZone getTimeZone();
    }
    
    // ------------------------------------------------------------------------
    
    public static boolean isValidTimeZone(String tzid)
    {
        if ((tzid != null) && !tzid.equals("")) {
            String tz[] = TimeZone.getAvailableIDs();
            for (int i = 0; i < tz.length; i++) {
                if (tz[i].equals(tzid)) { return true; }
            }
        }
        return false;
    }
    
    public static TimeZone getTimeZone(String tzid, TimeZone dft)
    {
        if (isValidTimeZone(tzid)) {
            return TimeZone.getTimeZone(tzid);
        } else {
            return dft;
        }
    }
    
    public static TimeZone getTimeZone(String tzid)
    {
        // 'TimeZone' will return GMT if an invalid name is specified
        try {
            String tzName = isValidTimeZone(tzid)? tzid : DEFAULT_TIMEZONE;
            return TimeZone.getTimeZone(tzName);
        } catch (Throwable t) { // trap any TimeZone error
            // This threw an NPE once (actually, this was because DEFAULT_TIMEZONE wasn't yet initialized)
            Print.logException("TimeZone exception", t);
            return null;
        }
    }
    
    public static TimeZone getDefaultTimeZone()
    {
        return DateTime.getTimeZone(null);
    }
    
    public static TimeZone getGMTTimeZone()
    {
        return TimeZone.getTimeZone(GMT_TIMEZONE);
    }
            
    // ------------------------------------------------------------------------

    private static SimpleDateFormat simpleFormatter = null;
    public String toString() 
    {
        if (simpleFormatter == null) {
            // eg. "Sun Mar 26 12:38:12 PST 2006"
            simpleFormatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        }
        synchronized (simpleFormatter) {
            simpleFormatter.setTimeZone(this.getTimeZone());
            return simpleFormatter.format(this.getDate());
        }
    }
    
    // ------------------------------------------------------------------------

    public String format(String fmt, StringBuffer sb, TimeZone tz)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        sdf.setTimeZone(this._timeZone(tz)); 
        if (sb == null) { sb = new StringBuffer(); }
        sdf.format(this.getDate(), sb, new FieldPosition(0));
        return sb.toString();
    }
    
    public String format(String fmt, TimeZone tz)
    {
        return this.format(fmt, null, tz);
    }
    
    public String format(TimeZone tz)
    {   // ie. "Oct 22, 2003 7:23:18 PM"
        return this.format("MMM dd, yyyy HH:mm:ss z", null, tz);
        //DateFormat dateFmt = DateFormat.getDateTimeInstance();
        //dateFmt.setTimeZone(tz);
        //return dateFmt.format(new java.util.Date(this.getTimeMillis()));
    }
    
    public String gmtFormat()
    {
        return this.gmtFormat("dd/MM/yyyy HH:mm:ss 'GMT'");
    }
    
    public String gmtFormat(String fmt)
    {
        return this.format(fmt, null, this.getGMTTimeZone());
    }
    
    public String gmtFormat(String fmt, StringBuffer sb)
    {
        return this.format(fmt, sb, this.getGMTTimeZone());
    }
        
    // ------------------------------------------------------------------------

    public Object clone()
    {
        return new DateTime(this);
    }
    
    // ------------------------------------------------------------------------
    
    protected static String encodeHourMinuteSecond(long tod, String fmt)
    {
        StringBuffer sb = new StringBuffer();
        int h = (int)(tod / (60L * 60L)), m = (int)((tod / 60L) % 60), s = (int)(tod % 60);
        if (fmt != null) {
            String f[] = StringTools.parseString(fmt, ':');
            if (f.length > 0) { sb.append(StringTools.format(h,f[0])); }
            if (f.length > 1) { sb.append(':').append(StringTools.format(m,f[1])); }
            if (f.length > 2) { sb.append(':').append(StringTools.format(s,f[2])); }
        } else {
            sb.append(h);
            sb.append(':').append(StringTools.format(m,"00"));
            if (s > 0) {
                sb.append(':').append(StringTools.format(s,"00"));
            }
        }
        return sb.toString();
    }

    protected static int parseHourMinuteSecond(String hms)
    {
        return parseHourMinuteSecond(hms, 0);
    }
    
    protected static int parseHourMinuteSecond(String hms, int dft)
    {
        String a[] = StringTools.parseString(hms,":");
        if (a.length <= 1) {
            // assume all seconds
            return StringTools.parseInt(hms, dft);
        } else
        if (a.length == 2) {
            // assume hh:mm
            int h = StringTools.parseInt(a[0], -1);
            int m = StringTools.parseInt(a[1], -1);
            return ((h >= 0) && (m >= 0))? (((h * 60) + m) * 60) : dft;
        } else { // (a.length >= 3)
            // assume hh:mm:ss
            int h = StringTools.parseInt(a[0], -1);
            int m = StringTools.parseInt(a[1], -1);
            int s = StringTools.parseInt(a[2], -1);
            return ((h >= 0) && (m >= 0) && (s >= 0))? ((((h * 60) + m) * 60) + s) : dft;
        }
    }
    
}
