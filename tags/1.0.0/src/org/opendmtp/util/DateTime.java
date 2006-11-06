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

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * 
 * Provides useful date/time utilities.
 * 
 * @author Martin D. Flynn
 * @author Pavel V. Senin
 */
public class DateTime implements Comparable, Cloneable {

  // ------------------------------------------------------------------------

  /**
   * Defines default timezone.
   */
  public static String DEFAULT_TIMEZONE = "US/Pacific";
  /**
   * Defines GMT timezone.
   */
  public static String GMT_TIMEZONE = "GMT";

  // ------------------------------------------------------------------------

  /**
   * Number of hours within one day.
   */
  public static final long HOURS_PER_DAY = 24L;
  /**
   * Number of seconds within one minute.
   */
  public static final long SECONDS_PER_MINUTE = 60L;
  /**
   * Number of minutes within one hour.
   */
  public static final long MINUTES_PER_HOUR = 60L;
  /**
   * Number of days within one week.
   */
  public static final long DAYS_PER_WEEK = 7L;
  /**
   * Number of seconds within one hour.
   */
  public static final long SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
  /**
   * Number of minutes within one day.
   */
  public static final long MINUTES_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR;
  /**
   * Number of seconds within one day.
   */
  public static final long SECONDS_PER_DAY = MINUTES_PER_DAY * SECONDS_PER_MINUTE;
  /**
   * Number of minutes within one week.
   */
  public static final long MINUTES_PER_WEEK = DAYS_PER_WEEK * MINUTES_PER_DAY;

  /**
   * Converts time interval given in days into seconds.
   * 
   * @param days time interval in days.
   * @return time interval converted into seconds.
   */
  public static long DaySeconds(long days) {
    return days * SECONDS_PER_DAY;
  }

  /**
   * Converts time interval given in days into seconds.
   * 
   * @param days time interval in days.
   * @return time interval converted into seconds.
   */
  public static long DaySeconds(double days) {
    return (long) Math.round(days * (double) SECONDS_PER_DAY);
  }

  /**
   * Converts time interval given in hours into seconds.
   * 
   * @param hours time interval in hours.
   * @return time interval converted into seconds.
   */
  public static long HourSeconds(long hours) {
    return hours * SECONDS_PER_HOUR;
  }

  /**
   * Converts time interval given in minutes into seconds.
   * 
   * @param minutes time interval in minutes.
   * @return time interval converted into seconds.
   */
  public static long MinuteSeconds(long minutes) {
    return minutes * SECONDS_PER_MINUTE;
  }

  /**
   * January numeric constant.
   */
  public static final int JAN = 0;
  /**
   * February numeric constant.
   */
  public static final int FEB = 1;
  /**
   * March numeric constant.
   */
  public static final int MAR = 2;
  /**
   * April numeric constant.
   */
  public static final int APR = 3;
  /**
   * May numeric constant.
   */
  public static final int MAY = 4;
  /**
   * June numeric constant.
   */
  public static final int JUN = 5;
  /**
   * July numeric constant.
   */
  public static final int JUL = 6;
  /**
   * August numeric constant.
   */
  public static final int AUG = 7;
  /**
   * September numeric constant.
   */
  public static final int SEP = 8;
  /**
   * October numeric constant.
   */
  public static final int OCT = 9;
  /**
   * November numeric constant.
   */
  public static final int NOV = 10;
  /**
   * December numeric constant.
   */
  public static final int DEC = 11;

  /**
   * January numeric constant.
   */
  public static final int JANUARY = JAN;
  /**
   * February numeric constant.
   */
  public static final int FEBRUARY = FEB;
  /**
   * March numeric constant.
   */
  public static final int MARCH = MAR;
  /**
   * April numeric constant.
   */
  public static final int APRIL = APR;

  // public static final int MAY = MAY;
  /**
   * June numeric constant.
   */
  public static final int JUNE = JUN;
  /**
   * July numeric constant.
   */
  public static final int JULY = JUL;
  /**
   * August numeric constant.
   */
  public static final int AUGUST = AUG;
  /**
   * September numeric constant.
   */
  public static final int SEPTEMBER = SEP;
  /**
   * October numeric constant.
   */
  public static final int OCTOBER = OCT;
  /**
   * November numeric constant.
   */
  public static final int NOVEMBER = NOV;
  /**
   * December numeric constant.
   */
  public static final int DECEMBER = DEC;

  /**
   * Months names constant array.
   */
  private static final String MONTH_NAME[][] = { { "January", "Jan" }, { "February", "Feb" },
      { "March", "Mar" }, { "April", "Apr" }, { "May", "May" }, { "June", "Jun" },
      { "July", "Jul" }, { "August", "Aug" }, { "September", "Sep" }, { "October", "Oct" },
      { "November", "Nov" }, { "December", "Dec" }, };

  /**
   * Returns month abbreviated or full name.
   * 
   * @param mon0 numeric value for the month to convert.
   * @param abbrev specifies which name to return, full or short.
   * @return month name.
   */
  public static String getMonthName(int mon0, boolean abbrev) {
    if ((mon0 >= JANUARY) && (mon0 <= DECEMBER)) {
      return abbrev ? MONTH_NAME[mon0][1] : MONTH_NAME[mon0][0];
    }
    else {
      return "";
    }
  }

  /**
   * Returns all month names as array.
   * 
   * @param abbrev specifies which names to return, full or short ones.
   * @return array containing Months names.
   */
  public static String[] getMonthNames(boolean abbrev) {
    String mo[] = new String[MONTH_NAME.length];
    for (int i = 0; i < MONTH_NAME.length; i++) {
      mo[i] = DateTime.getMonthName(i, abbrev);
    }
    return mo;
  }

  /**
   * Returns all month names as Map with month names as keys and numeric values as mapped objects.
   * 
   * @param abbrev specifies which names to return, full or short ones.
   * @return map containing Months names.
   */
  public static Map getMonthNameMap(boolean abbrev) {
    Map map = new OrderedMap();
    for (int i = 0; i < MONTH_NAME.length; i++) {
      map.put(DateTime.getMonthName(i, abbrev), new Integer(i));
    }
    return map;
  }

  // ------------------------------------------------------------------------

  /**
   * Specifies number of days within specific month.
   */
  private static final int MONTH_DAYS[] = { 31, // Jan
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

  /**
   * Returns number of days within specified month.
   * 
   * @param tz time zone.
   * @param m0 month.
   * @param year year.
   * @return number of days.
   */
  public static int getDaysInMonth(TimeZone tz, int m0, int year) {
    DateTime dt = new DateTime(tz, year, m0, 1);
    return DateTime.getMaxMonthDayCount(m0, dt.isLeapYear(tz));
  }

  /**
   * Returns number of days within specified month.
   * 
   * @param m0 month.
   * @param isLeapYear specifies Leap Year.
   * @return number of days.
   */
  public static int getMaxMonthDayCount(int m0, boolean isLeapYear) {
    int d = ((m0 >= 0) && (m0 < DateTime.MONTH_DAYS.length)) ? DateTime.MONTH_DAYS[m0] : 31;
    return ((m0 != FEBRUARY) || isLeapYear) ? d : 28;
  }

  /**
   * Numeric constant for Sunday.
   */
  public static final int SUN = 0;
  /**
   * Numeric constant for Monday.
   */
  public static final int MON = 1;
  /**
   * Numeric constant for Tuesday.
   */
  public static final int TUE = 2;
  /**
   * Numeric constant for Wednesday.
   */
  public static final int WED = 3;
  /**
   * Numeric constant for Thursday.
   */
  public static final int THU = 4;
  /**
   * Numeric constant for Friday.
   */
  public static final int FRI = 5;
  /**
   * Numeric constant for Saturday.
   */
  public static final int SAT = 6;

  /**
   * Numeric constant for Sunday.
   */
  public static final int SUNDAY = SUN;
  /**
   * Numeric constant for Monday.
   */
  public static final int MONDAY = MON;
  /**
   * Numeric constant for Tuesday.
   */
  public static final int TUESDAY = TUE;
  /**
   * Numeric constant for Wednesday.
   */
  public static final int WEDNESDAY = WED;
  /**
   * Numeric constant for Thursday.
   */
  public static final int THURSDAY = THU;
  /**
   * Numeric constant for Friday.
   */
  public static final int FRIDAY = FRI;
  /**
   * Numeric constant for Saturday.
   */
  public static final int SATURDAY = SAT;

  /**
   * Defines constant array with weekday names and abbreviations.
   */
  private static final String DAY_NAME[][] = { { "Sunday", "Sun" }, { "Monday", "Mon" },
      { "Tuesday", "Tue" }, { "Wednesday", "Wed" }, { "Thursday", "Thu" }, { "Friday", "Fri" },
      { "Saturday", "Sat" }, };

  /**
   * Returns day name abbreviation.
   * 
   * @param day numeric value for day.
   * @param abbrev specifies short or long format for the day abbreviation.
   * @return day abbreviation.
   */
  public static String getDayName(int day, boolean abbrev) {
    if ((day >= SUNDAY) && (day <= SATURDAY)) {
      return abbrev ? DAY_NAME[day][1] : DAY_NAME[day][0];
    }
    else {
      return "";
    }
  }

  /**
   * Returns array containing all day names.
   * 
   * @param abbrev specifies short or long format for the day abbreviation.
   * @return day names array.
   */
  public static String[] getDayNames(boolean abbrev) {
    String dy[] = new String[DAY_NAME.length];
    for (int i = 0; i < DAY_NAME.length; i++) {
      dy[i] = DateTime.getDayName(i, abbrev);
    }
    return dy;
  }

  /**
   * Returns map containing all day names as keys and numeric values as mapped objects.
   * 
   * @param abbrev specifies short or long format for the day abbreviation.
   * @return day names map.
   */
  public static Map getDayNameMap(boolean abbrev) {
    Map map = new OrderedMap();
    for (int i = 0; i < DAY_NAME.length; i++) {
      map.put(DateTime.getDayName(i, abbrev), new Integer(i));
    }
    return map;
  }

  /**
   * Returns hours numbers as string array.
   * 
   * @param hr24 - specifies whether 12 or 24 hours format used.
   * @return hours numbers as string array.
   */
  public static String[] getHours(boolean hr24) {
    String hrs[] = new String[hr24 ? 24 : 12];
    for (int i = 0; i < hrs.length; i++) {
      hrs[i] = String.valueOf(i);
    }
    return hrs;
  }

  /**
   * Returns minutes numbers as a String array.
   * 
   * @return minutes numbers as a String array.
   */
  public static String[] getMinutes() {
    String min[] = new String[60];
    for (int i = 0; i < min.length; i++) {
      min[i] = String.valueOf(i);
    }
    return min;
  }

  /**
   * Converts time in seconds into the date.
   * 
   * @param timeSec time in seconds to convert.
   * @return converted date.
   */
  public static String toString(long timeSec) {
    return (new java.util.Date(timeSec * 1000L)).toString();
  }

  /**
   * Gets current time in seconds.
   * 
   * @return current time in seconds.
   */
  public static long getCurrentTimeSec() {
    // Number of seconds since the 'epoch' January 1, 1970, 00:00:00 GMT
    return getCurrentTimeMillis() / 1000L;
  }

  /**
   * Gets current time in milliseconds.
   * 
   * @return current time in milliseconds.
   */
  public static long getCurrentTimeMillis() {
    // Number of milliseconds since the 'epoch' January 1, 1970, 00:00:00 GMT
    return System.currentTimeMillis();
  }

  /**
   * Returns true when fixed time is happened before the (current_time - lapseSec interval) time.
   * 
   * @param timeSec time specified in seconds.
   * @param lapseSec time interval in seconds.
   * @return true when time is greater than specified interval.
   */
  public static boolean isRecentSec(long timeSec, long lapseSec) {
    Print.logDebug("timeSec: " + timeSec);
    Print.logDebug("getCurrentTimeSec: " + DateTime.getCurrentTimeSec());
    return (timeSec >= (DateTime.getCurrentTimeSec() - lapseSec));
  }

  /**
   * Returns minimal possible date value.
   * 
   * @return minimal possible date value.
   */
  public static DateTime getMinDate() {
    return new DateTime(1);
  }

  /**
   * Returns maximal possible date value.
   * 
   * @return maximal possible date value.
   */
  public static DateTime getMaxDate() {
    return new DateTime(null, 2020, 11, 31);
  }

  private TimeZone timeZone = null;
  /**
   * Contains current time.
   */
  private long timeMillis = 0L; // ms since January 1, 1970, 00:00:00 GMT

  /**
   * Instantiates DateTime object with current time.
   * 
   */
  public DateTime() {
    this.setTimeMillis(getCurrentTimeMillis());
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param tz time zone to set.
   */
  public DateTime(TimeZone tz) {
    this();
    this.setTimeZone(tz);
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param date date to set.
   */
  public DateTime(Date date) {
    this.setTimeMillis(date.getTime());
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param date date to set.
   * @param tz time zone to set.
   */
  public DateTime(Date date, TimeZone tz) {
    this.setTimeMillis(date.getTime());
    this.setTimeZone(tz);
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param tz time zone to set.
   * @param year year to set.
   * @param month0 moth to set.
   * @param day day to set.
   */
  public DateTime(TimeZone tz, int year, int month0, int day) {
    this.setDate(tz, year, month0, day);
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param tz time zone to set.
   * @param year year to set.
   * @param month0 month to set.
   * @param day day to set.
   * @param hour24 hour to set in 24 hours format.
   * @param minute minute to set.
   * @param second second to set.
   */
  public DateTime(TimeZone tz, int year, int month0, int day, int hour24, int minute, int second) {
    this.setDate(tz, year, month0, day, hour24, minute, second);
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param timeSec time to set in seconds.
   */
  public DateTime(long timeSec) {
    this.setTimeSec(timeSec);
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param timeSec time to set in seconds.
   * @param tz time zone to set.
   */
  public DateTime(long timeSec, TimeZone tz) {
    this.setTimeSec(timeSec);
    this.setTimeZone(tz);
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param d time to set, Valid format: "YYYY/MM/DD [HH:MM:SS] [PST].
   * @throws ParseException if error encountered while parsing.
   */
  public DateTime(String d) throws ParseException {
    this.setDate(d);
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param dt time to set.
   */
  public DateTime(DateTime dt) {
    this.timeMillis = dt.timeMillis;
    this.timeZone = dt.timeZone;
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param dt time to set.
   * @param deltaOffsetSec time offset in seconds, will be added to dt.
   */
  public DateTime(DateTime dt, long deltaOffsetSec) {
    this.timeMillis = dt.timeMillis + (deltaOffsetSec * 1000L);
    this.timeZone = dt.timeZone;
  }

  /**
   * Returns date.
   * 
   * @return date.
   */
  public java.util.Date getDate() {
    return new java.util.Date(this.getTimeMillis());
  }

  /**
   * Sets date.
   * 
   * @param tz specifies time zone.
   * @param year specifies year.
   * @param month0 specifies month
   * @param day specifies day.
   */
  public void setDate(TimeZone tz, int year, int month0, int day) {
    this.setDate(tz, year, month0, day, 0, 0, 0);
  }

  /**
   * Sets date.
   * 
   * @param tz specifies time zone.
   * @param year specifies year.
   * @param month0 specifies month.
   * @param day specifies day.
   * @param hour24 specifies hour in 24 hours format.
   * @param minute specifies minute.
   * @param second specifies second.
   */
  public void setDate(TimeZone tz, int year, int month0, int day, int hour24, int minute, int second) {
    this.setTimeZone(tz);
    Calendar cal = new GregorianCalendar(this._timeZone(tz));
    cal.set(year, month0, day, hour24, minute, second);
    Date date = cal.getTime();
    this.setTimeMillis(date.getTime());
  }

  /**
   * Sets date.
   * 
   * @param d date to set, Valid format: "YYYY/MM/DD [HH:MM:SS] [PST].
   * @throws ParseException if error encountered while parsing.
   */
  public void setDate(String d) throws ParseException {
    this.setDate(d, (TimeZone) null);
  }

  /**
   * Sets date.
   * 
   * @param d date to set, Valid format: "YYYY/MM/DD [HH:MM:SS] [PST].
   * @param dftTMZ time zone to set.
   * @throws ParseException if error encountered while parsing.
   */
  public void setDate(String d, TimeZone dftTMZ) throws ParseException {
    String ds[] = StringTools.parseString(d, ' ');
    String dt = (ds.length > 0) ? ds[0] : "";
    String tm = (ds.length > 1) ? ds[1] : "";
    String tz = (ds.length > 2) ? ds[2] : "";
    // Valid format: "YYYY/MM/DD [HH:MM:SS] [PST]"

    /* time-zone */
    TimeZone timeZone = null;
    if (ds.length > 2) {
      if (DateTime.isValidTimeZone(tz)) {
        timeZone = DateTime.getTimeZone(tz);
      }
      else {
        throw new ParseException("Invalid TimeZone: " + tz, 0);
      }
    }
    else if ((ds.length > 1) && DateTime.isValidTimeZone(tm)) {
      tz = tm;
      tm = "";
      timeZone = DateTime.getTimeZone(tz);
    }
    else {
      timeZone = (dftTMZ != null) ? dftTMZ : this.getTimeZone();
    }
    this.setTimeZone(timeZone);
    Calendar calendar = new GregorianCalendar(timeZone);

    /* date */
    int yr = -1, mo = -1, dy = -1;
    int d1 = dt.indexOf('/'), d2 = (d1 > 0) ? dt.indexOf('/', d1 + 1) : -1;
    if ((d1 > 0) && (d2 > d1)) {

      /* year */
      String YR = dt.substring(0, d1);
      yr = StringTools.parseInt(YR, -1);
      if ((yr >= 0) && (yr <= 49)) {
        yr += 2000;
      }
      else if ((yr >= 50) && (yr <= 99)) {
        yr += 1900;
      }
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

    }
    else {

      throw new ParseException("Invalid date format (Y/M/D): " + dt, 0);

    }

    /* time */
    if (tm.equals("")) {

      calendar.set(yr, mo, dy);

    }
    else {

      int hr = -1, mn = -1, sc = -1;
      int t1 = tm.indexOf(':'), t2 = (t1 > 0) ? tm.indexOf(':', t1 + 1) : -1;
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

        }
        else {

          /* minute */
          String MN = tm.substring(t1 + 1);
          mn = StringTools.parseInt(MN, -1);
          if ((mn < 0) || (mn > 59)) {
            throw new ParseException("Time/Minute out of range: " + MN, 0);
          }

          calendar.set(yr, mo, dy, hr, mn);

        }
      }
      else {

        throw new ParseException("Invalid time format (H:M:S): " + tm, 0);

      }
    }

    /* ok */
    this.setTimeMillis(calendar.getTime().getTime());

  }

  /**
   * Sets date, does not work!
   * 
   * @param d date to set, Valid format: "YYYY/MM/DD [HH:MM:SS] [PST].
   * @return true if date is set.
   */
  public boolean setParsedDate_formatted(String d) // does not work!
  {
    try {
      java.util.Date date = DateFormat.getDateInstance().parse(d);
      this.setTimeMillis(date.getTime());
      return true;
    }
    catch (ParseException pe) {
      Print.logStackTrace("Unable to parse date: " + d, pe);
      this.setTimeMillis(0L);
      return false;
    }
  }

  /**
   * Returns calendar.
   * 
   * @param tz specifies time zone for calendar.
   * @return calendar within specified time zone.
   */
  public Calendar getCalendar(TimeZone tz) {
    Calendar c = new GregorianCalendar(this._timeZone(tz));
    c.setTimeInMillis(this.getTimeMillis());
    return c;
  }

  /**
   * Returns the value of the given calendar field.
   * 
   * @param tz specifies time zone.
   * @param value field to return.
   * @return the value of the specified field.
   */
  private int _get(TimeZone tz, int value) {
    return this.getCalendar(tz).get(value);
  }

  /**
   * Returns current month value in 0-11 format interval.
   * 
   * @param tz specifies time zone.
   * @return current month value
   */
  public int getMonth0(TimeZone tz) {
    // return 0..11
    return this._get(tz, Calendar.MONTH); // 0 indexed
  }

  /**
   * Returns current month value in 1-12 format interval.
   * 
   * @param tz specifies time zone.
   * @return current month value
   */
  public int getMonth1(TimeZone tz) {
    // return 1..12
    return this.getMonth0(tz) + 1;
  }

  /**
   * Returns number of days within month.
   * 
   * @param tz specified time zone.
   * @return number of days within month.
   */
  public int getDayOfMonth(TimeZone tz) {
    // return 1..31
    return this._get(tz, Calendar.DAY_OF_MONTH);
  }

  /**
   * Returns number of days within current month.
   * 
   * @param tz specified time zone.
   * @return number of days within month.
   */
  public int getDaysInMonth(TimeZone tz) {
    return DateTime.getDaysInMonth(tz, this.getMonth0(tz), this.getYear(tz));
  }

  /**
   * Returns current day of week.
   * 
   * @param tz specified time zone.
   * @return current day of week.
   */
  public int getDayOfWeek(TimeZone tz) {
    // return 0..6
    return this._get(tz, Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
  }

  /**
   * Returns current year.
   * 
   * @param tz specified time zone.
   * @return current year.
   */
  public int getYear(TimeZone tz) {
    return this._get(tz, Calendar.YEAR);
  }

  /**
   * Reports whether it is Leap year.
   * 
   * @param tz specified time zone.
   * @return true if current year is leap.
   */
  public boolean isLeapYear(TimeZone tz) {
    GregorianCalendar gc = (GregorianCalendar) this.getCalendar(tz);
    return gc.isLeapYear(gc.get(Calendar.YEAR));
  }

  /**
   * Returns current hour in 24 hours mode.
   * 
   * @param tz specified time zone.
   * @return current hour.
   */
  public int getHour24(TimeZone tz) {
    return this._get(tz, Calendar.HOUR_OF_DAY);
  }

  /**
   * Returns current hour in 12 hours mode.
   * 
   * @param tz specified time zone.
   * @return current hour.
   */
  public int getHour12(TimeZone tz) {
    return this._get(tz, Calendar.HOUR);
  }

  /**
   * Reports whether it is AM time.
   * 
   * @param tz specified time zone.
   * @return true if it is AM time.
   */

  public boolean isAM(TimeZone tz) {
    return this._get(tz, Calendar.AM_PM) == Calendar.AM;
  }

  /**
   * Reports whether it is PM time.
   * 
   * @param tz specified time zone.
   * @return true if it is PM time.
   */
  public boolean isPM(TimeZone tz) {
    return this._get(tz, Calendar.AM_PM) == Calendar.PM;
  }

  /**
   * Returns current minute.
   * 
   * @param tz specified time zone.
   * @return current minute.
   */
  public int getMinute(TimeZone tz) {
    return this._get(tz, Calendar.MINUTE);
  }

  /**
   * Returns current second.
   * 
   * @param tz time zone.
   * @return current second.
   */
  public int getSecond(TimeZone tz) {
    return this._get(tz, Calendar.SECOND);
  }

  /**
   * Returns time in seconds.
   * 
   * @return time in seconds.
   */
  public long getTimeSec() {
    return this.getTimeMillis() / 1000L;
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param timeSec time to set in seconds.
   */
  public void setTimeSec(long timeSec) {
    this.timeMillis = timeSec * 1000L;
  }

  /**
   * Returns time in milliseconds.
   * 
   * @return time in milliseconds.
   */
  public long getTimeMillis() {
    return this.timeMillis;
  }

  /**
   * Instantiates the DateTime object.
   * 
   * @param timeMillis time to set in milliseconds.
   */
  public void setTimeMillis(long timeMillis) {
    this.timeMillis = timeMillis;
  }

  /**
   * Returns current day start in milliseconds.
   * 
   * @param tz specified time zone.
   * @return amount of milliseconds up to current day begin.
   */
  public long getDayStart(TimeZone tz) {
    if (tz == null) {
      tz = _timeZone(tz);
    }
    Calendar c = this.getCalendar(tz);
    Calendar nc = new GregorianCalendar(tz);
    nc.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    return nc.getTime().getTime() / 1000L;
  }

  /**
   * Returns current day end in seconds.
   * 
   * @param tz specified time zone.
   * @return current day end value in seconds.
   */
  public long getDayEnd(TimeZone tz) {
    if (tz == null) {
      tz = _timeZone(tz);
    }
    Calendar c = this.getCalendar(tz);
    Calendar nc = new GregorianCalendar(tz);
    nc.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
    return nc.getTime().getTime() / 1000L;
  }

  /**
   * Returns current day start in milliseconds within GMT.
   * 
   * @return amount of milliseconds up to current day begin.
   */
  public long getDayStartGMT() {
    // GMT TimeZone
    return (this.getTimeSec() / SECONDS_PER_DAY) * SECONDS_PER_DAY;
  }

  /**
   * Returns current day end in milliseconds within GMT.
   * 
   * @return amount of milliseconds up to the current day end.
   */
  public long getDayEndGMT() {
    // GMT TimeZone
    return this.getDayStartGMT() + SECONDS_PER_DAY - 1L;
  }

  /**
   * Reports whether the current time is after specified.
   * 
   * @param dt specified time value.
   * @return true if the current time is after specified.
   */
  public boolean after(DateTime dt) {
    return this.after(dt, false);
  }

  /**
   * Reports whether the current time is after specified.
   * 
   * @param dt specified time value.
   * @param inclusive flags the inclusion for time interval. If true, method checks [0, dt.getTime]
   *        including dt.getTime.
   * @return true if the current time is after specified.
   */
  public boolean after(DateTime dt, boolean inclusive) {
    if (dt == null) {
      return true; // arbitrary
    }
    else if (inclusive) {
      return (this.getTimeMillis() >= dt.getTimeMillis());
    }
    else {
      return (this.getTimeMillis() > dt.getTimeMillis());
    }
  }

  /**
   * Reports whether the current time is before specified.
   * 
   * @param dt specified time value.
   * @return true if the current time is before specified.
   */
  public boolean before(DateTime dt) {
    return this.before(dt, false);
  }

  /**
   * Reports whether the current time is before specified.
   * 
   * @param dt specified time value.
   * @param inclusive flags the inclusion for time interval. If true, method checks [dt.getTime,
   *        current.Time] including dt.getTime.
   * @return true if the current time is before specified.
   */
  public boolean before(DateTime dt, boolean inclusive) {
    if (dt == null) {
      return false; // arbitrary
    }
    else if (inclusive) {
      return (this.getTimeMillis() <= dt.getTimeMillis());
    }
    else {
      return (this.getTimeMillis() < dt.getTimeMillis());
    }
  }

  /**
   * Implements DateTime object comparison.
   * 
   * @param obj object to compare with.
   * @return true if objects are equals.
   */
  public boolean equals(Object obj) {
    if (obj instanceof DateTime) {
      return (this.getTimeMillis() == ((DateTime) obj).getTimeMillis());
    }
    else {
      return false;
    }
  }

  /**
   * Implements DateTime object comparison.
   * 
   * @param other object to compare with.
   * @return -1 if current Time less than specified. 0 if current Time equals to specified. 1 if
   *         current time greater than specified.
   */
  public int compareTo(Object other) {
    if (other instanceof DateTime) {
      long otherTime = ((DateTime) other).getTimeMillis();
      long thisTime = this.getTimeMillis();
      if (thisTime < otherTime) {
        return -1;
      }
      if (thisTime > otherTime) {
        return 1;
      }
      return 0;
    }
    else {
      return -1;
    }
  }

  /**
   * Returns time zone.
   * 
   * @param tz specifies timezone.
   * @return time zone.
   */
  protected TimeZone _timeZone(TimeZone tz) {
    return (tz != null) ? tz : this.getTimeZone();
  }

  /**
   * Returns current time zone.
   * 
   * @return time zone.
   */
  public TimeZone getTimeZone() {
    return (this.timeZone != null) ? this.timeZone : DateTime.getDefaultTimeZone();
  }

  /**
   * Sets time zone.
   * 
   * @param tz time zone to set.
   */
  public void setTimeZone(TimeZone tz) {
    this.timeZone = tz;
  }

  /**
   * Sets time zone.
   * 
   * @param tz time zone to set.
   */
  public void setTimeZone(String tz) {
    this.setTimeZone(DateTime.getTimeZone(tz, null));
  }

  /**
   * Provides time zone.
   * 
   * @author Martin D. Flynn.
   * 
   */
  public interface TimeZoneProvider {

    /**
     * Returns time zone.
     * 
     * @return time zone.
     */
    public TimeZone getTimeZone();
  }

  /**
   * Checks if time zone string is valid.
   * 
   * @param tzid string to check.
   * @return true if string is valid.
   */
  public static boolean isValidTimeZone(String tzid) {
    if ((tzid != null) && !tzid.equals("")) {
      String tz[] = TimeZone.getAvailableIDs();
      for (int i = 0; i < tz.length; i++) {
        if (tz[i].equals(tzid)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Parses string that encodes time zone value.
   * 
   * @param tzid string to parse.
   * @param dft default value.
   * @return time zone encoded by tzid or default value if string is not valid.
   */
  public static TimeZone getTimeZone(String tzid, TimeZone dft) {
    if (isValidTimeZone(tzid)) {
      return TimeZone.getTimeZone(tzid);
    }
    else {
      return dft;
    }
  }

  /**
   * Parses string that encodes time zone value.
   * 
   * @param tzid string to parse.
   * 
   * @return time zone encoded by string or null if unable to parse.
   */
  public static TimeZone getTimeZone(String tzid) {
    // 'TimeZone' will return GMT if an invalid name is specified
    try {
      String tzName = isValidTimeZone(tzid) ? tzid : DEFAULT_TIMEZONE;
      return TimeZone.getTimeZone(tzName);
    }
    catch (Throwable t) { // trap any TimeZone error
      // This threw an NPE once (actually, this was because DEFAULT_TIMEZONE wasn't yet initialized)
      Print.logException("TimeZone exception", t);
      return null;
    }
  }

  /**
   * Returns default time zone.
   * 
   * @return default time zone.
   */
  public static TimeZone getDefaultTimeZone() {
    return DateTime.getTimeZone(null);
  }

  /**
   * Returns value for GMT timezone.
   * 
   * @return GMT time zone.
   */
  public static TimeZone getGMTTimeZone() {
    return TimeZone.getTimeZone(GMT_TIMEZONE);
  }

  /**
   * Defines formatter for a simple date format.
   */
  private static SimpleDateFormat simpleFormatter = null;

  /**
   * Converts current time into string. String formatted as "EEE MMM dd HH:mm:ss zzz yyyy" in US
   * locale.
   * 
   * @return current time string representation.
   */
  public String toString() {
    if (simpleFormatter == null) {
      // eg. "Sun Mar 26 12:38:12 PST 2006"
      simpleFormatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
    }
    synchronized (simpleFormatter) {
      simpleFormatter.setTimeZone(this.getTimeZone());
      return simpleFormatter.format(this.getDate());
    }
  }

  /**
   * Returns formatted representation of current time.
   * 
   * @param fmt format to be used.
   * @param sb buffer to put result, if not specified new one will be created.
   * @param tz specified time zone.
   * @return current time in String format, "Oct 22, 2003 7:23:18 PM".
   */
  public String format(String fmt, StringBuffer sb, TimeZone tz) {
    SimpleDateFormat sdf = new SimpleDateFormat(fmt);
    sdf.setTimeZone(this._timeZone(tz));
    if (sb == null) {
      sb = new StringBuffer();
    }
    sdf.format(this.getDate(), sb, new FieldPosition(0));
    return sb.toString();
  }

  /**
   * Returns formatted representation of current time.
   * 
   * @param fmt format to be used.
   * @param tz specified time zone.
   * @return current time in String format, "Oct 22, 2003 7:23:18 PM".
   */
  public String format(String fmt, TimeZone tz) {
    return this.format(fmt, null, tz);
  }

  /**
   * Returns formatted representation of current time.
   * 
   * @param tz specified time zone.
   * @return current time in String format, "Oct 22, 2003 7:23:18 PM".
   */
  public String format(TimeZone tz) { // ie. "Oct 22, 2003 7:23:18 PM"
    return this.format("MMM dd, yyyy HH:mm:ss z", null, tz);
    // DateFormat dateFmt = DateFormat.getDateTimeInstance();
    // dateFmt.setTimeZone(tz);
    // return dateFmt.format(new java.util.Date(this.getTimeMillis()));
  }

  /**
   * Returns current time in GMT format.
   * 
   * @return time in GMT format.
   */
  public String gmtFormat() {
    return this.gmtFormat("dd/MM/yyyy HH:mm:ss 'GMT'");
  }

  /**
   * Returns current time in GMT format.
   * 
   * @param fmt specifies time format.
   * @return time in GMT format.
   */
  public String gmtFormat(String fmt) {
    return this.format(fmt, null, this.getGMTTimeZone());
  }

  /**
   * Returns current time in GMT format.
   * 
   * @param fmt specifies time format.
   * @param sb buffer where result returned, if not specified, new buffer allocated.
   * @return time in GMT format.
   */
  public String gmtFormat(String fmt, StringBuffer sb) {
    return this.format(fmt, sb, this.getGMTTimeZone());
  }

  /**
   * Returns clone of DateTime object.
   * 
   * @return copy of the object.
   */
  public Object clone() {
    return new DateTime(this);
  }

  /**
   * Encodes time into string format "HH:MM:SS".
   * 
   * @param tod time to encode.
   * @param fmt format string.
   * @return string containing converted time value.
   */
  protected static String encodeHourMinuteSecond(long tod, String fmt) {
    StringBuffer sb = new StringBuffer();
    int h = (int) (tod / (60L * 60L)), m = (int) ((tod / 60L) % 60), s = (int) (tod % 60);
    if (fmt != null) {
      String f[] = StringTools.parseString(fmt, ':');
      if (f.length > 0) {
        sb.append(StringTools.format(h, f[0]));
      }
      if (f.length > 1) {
        sb.append(':').append(StringTools.format(m, f[1]));
      }
      if (f.length > 2) {
        sb.append(':').append(StringTools.format(s, f[2]));
      }
    }
    else {
      sb.append(h);
      sb.append(':').append(StringTools.format(m, "00"));
      if (s > 0) {
        sb.append(':').append(StringTools.format(s, "00"));
      }
    }
    return sb.toString();
  }

  /**
   * Parses string into time in seconds. Valid string format HH:MM:SS.
   * 
   * @param hms time to parse.
   * @return seconds within specified by [0:hms] time interval.
   */
  protected static int parseHourMinuteSecond(String hms) {
    return parseHourMinuteSecond(hms, 0);
  }

  /**
   * Parses string into time in seconds. Valid string format HH:MM:SS.
   * 
   * @param hms string to parse.
   * @param dft default value.
   * @return seconds within specified by [0:hms] time interval.
   */
  protected static int parseHourMinuteSecond(String hms, int dft) {
    String a[] = StringTools.parseString(hms, ":");
    if (a.length <= 1) {
      // assume all seconds
      return StringTools.parseInt(hms, dft);
    }
    else if (a.length == 2) {
      // assume hh:mm
      int h = StringTools.parseInt(a[0], -1);
      int m = StringTools.parseInt(a[1], -1);
      return ((h >= 0) && (m >= 0)) ? (((h * 60) + m) * 60) : dft;
    }
    else { // (a.length >= 3)
      // assume hh:mm:ss
      int h = StringTools.parseInt(a[0], -1);
      int m = StringTools.parseInt(a[1], -1);
      int s = StringTools.parseInt(a[2], -1);
      return ((h >= 0) && (m >= 0) && (s >= 0)) ? ((((h * 60) + m) * 60) + s) : dft;
    }
  }

}
