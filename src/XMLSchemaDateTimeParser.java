/*
 * Copyright (C) 2012 Neo Visionaries Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Lightweight parser for <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">XMLSchema dateTime</a>.
 *
 * <pre style="background-color: lightgray;">
 *
 * {@link Calendar} calendar = XMLSchemaDateTimeParser.{@link #parse(String)
 * parse}("2005-11-14T02:16:38Z");
 * </pre>
 *
 * @author Takahiko Kawasaki
 */
public class XMLSchemaDateTimeParser
{
    /**
     * [-]yyyy-mm-dd'T'hh:mm:ss(.s+)?(Z|[+-]hh:mm)?
     */
    private static final Pattern dateTimeFormat =
            Pattern.compile("^\\s*([-])?(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(\\.(\\d{1,3}))?(Z|([+-])(\\d{2}):(\\d{2}))?\\s*$");

    private static final int POSITION_YEAR = 2;
    private static final int POSITION_MONTH = 3;
    private static final int POSITION_DAY = 4;
    private static final int POSITION_HOUR = 5;
    private static final int POSITION_MINUTE = 6;
    private static final int POSITION_SECOND = 7;
    private static final int POSITION_MILLISECOND = 9;
    private static final int POSITION_TIMEZONE_SIGN = 11;
    private static final int POSITION_TIMEZONE_HOUR = 12;
    private static final int POSITION_TIMEZONE_MINUTE = 13;


    /**
     * Parse <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">XMLSchema dateTime</a>.
     *
     * <p>
     * <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">XMLSchema dateTime</a>
     * is a subset of <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>
     * and its format can be described as follows.
     * </p>
     * <pre>
     * '-'? <i>yyyy</i> '-' <i>mm</i> '-' <i>dd</i> 'T' <i>hh</i> ':' <i>mm</i> ':' <i>ss</i> ('.' <i>s+</i>)? ('Z' | ('+' | '-') <i>hh</i> ':' <i>mm</i>)?
     * </pre>
     * <p>
     * Below are examples.
     * </p>
     * <ul>
     * <li>2005-11-14T02:16:38Z</li>
     * <li>2005-11-14T02:16:38-09:00</li>
     * <li>2005-11-14T02:16:38.125</li>
     * </ul>
     * <p>
     * Note that this parser additionally imposes some restrictions on the date time
     * format as described below. However, practically speaking, these are trivial.
     * </p>
     * <ol>
     * <li>The specification allows the year part to be a 5-or-more-digit number, but
     *     this parser assumes only 4-digit number at the year part.</li>
     * <li>The specification allows a leading hyphen ('-') to indicate B.C. years, but
     *     this parser just ignores the negative sign.
     * <li>The specification does not mention the length of the millisecond part, but
     *     this parser assumes that the length is in between 1 and 3.
     * </ol>
     *
     * @param dateTime
     *         A string in the <a href="http://www.w3.org/TR/xmlschema-2/#dateTime"
     *         >XMLSchema dateTime</a> format. Leading and trailing white spaces,
     *         if the given argument has any, are ignored.
     *
     * @return
     *         A {@link Calendar} instance that represents the given date time.
     *         If the given argument is null or the format of the string does not
     *         comply with the <a href="http://www.w3.org/TR/xmlschema-2/#dateTime"
     *         >XMLSchema dateTime</a> format, null is returned.
     */
    public static Calendar parse(String dateTime)
    {
        if (dateTime == null)
        {
            return null;
        }

        Matcher m = dateTimeFormat.matcher(dateTime);

        // Check if the given string matches the date-time format.
        if (m.matches() == false)
        {
            // Failed to parse the string.
            return null;
        }

        // @formatter:off
        int year   = getInt(m, POSITION_YEAR);
        int month  = getInt(m, POSITION_MONTH);
        int day    = getInt(m, POSITION_DAY);
        int hour   = getInt(m, POSITION_HOUR);
        int minute = getInt(m, POSITION_MINUTE);
        int second = getInt(m, POSITION_SECOND);
        int ms     = toMilliseconds(m.group(POSITION_MILLISECOND));
        // @formatter:on

        // Time Zone
        TimeZone timeZone = getTimeZone(m);

        // Create a Calendar instance ans set values.
        Calendar calendar = new GregorianCalendar(timeZone);
        calendar.set(year, month - 1, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, ms);

        // True if the given string has a leading '-'.
        boolean negativeYear = isNegative(m.group(POSITION_YEAR));

        if (negativeYear)
        {
            // Hmm... It seems GregorianCalendar does not handle
            // a negative year as I want. I cannot help but ignore
            // the leading '-'.

            //calendar.set(Calendar.YEAR, -year);
        }

        return calendar;
    }


    /**
     * Check if the first letter of the given string represents a negative sign.
     *
     * @param str
     *         A string whose first letter may be a negative sign.
     *
     * @return
     *         If the first letter of the given string is '-', true is returned.
     *         In other cases, for example, if the given string is null or empty,
     *         or if the first letter is '+' or any other letter, false is returned.
     */
    private static boolean isNegative(String str)
    {
        if (str != null && 1 <= str.length() && str.charAt(0) == '-')
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    /**
     * Get the value of the specified group in the given matcher as an integer.
     *
     * @param m
     * @param group
     * @return
     *         An integer converted from the string value of the group.
     *         If parsing the string fails, 0 is returned.
     */
    private static int getInt(Matcher m, int group)
    {
        try
        {
            return Integer.parseInt(m.group(group));
        }
        catch (Exception e)
        {
            return 0;
        }
    }


    private static int toMilliseconds(String str)
    {
        if (str == null || str.length() == 0)
        {
            return 0;
        }

        int number = Integer.parseInt(str);

        // The length of the input string is in between 1 and 3.
        switch (str.length())
        {
            case 1:
                return 100 * number;

            case 2:
                return 10 * number;

            default:
                return number; 
        }
    }


    private static TimeZone getTimeZone(Matcher m)
    {
        String str = m.group(10);

        if (str == null || str.length() == 0 || str.equals("Z"))
        {
            return TimeZone.getTimeZone("UTC");
        }

        // See the description of TimeZone about custom TimeZone IDs.
        String timeZoneId = "GMT" + str;

        // Calculate the offset from GMT in milliseconds.
        boolean negative = isNegative(m.group(POSITION_TIMEZONE_SIGN));
        int hours = getInt(m, POSITION_TIMEZONE_HOUR);
        int minutes = getInt(m, POSITION_TIMEZONE_MINUTE);
        int offset = (hours * 60 + minutes) * 60 * 1000 * (negative ? -1 : 1);

        // Create a custom TimeZone.
        return new SimpleTimeZone(offset, timeZoneId);
    }
}
