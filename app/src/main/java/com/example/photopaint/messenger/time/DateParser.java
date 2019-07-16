package com.example.photopaint.messenger.time;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public interface DateParser {

    /**
     * Equivalent to DateFormat.parse(String).
     * <p/>
     * See {@link java.text.DateFormat#parse(String)} for more information.
     *
     * @param source A <code>String</code> whose beginning should be parsed.
     * @return A <code>Date</code> parsed from the string
     * @throws ParseException if the beginning of the specified string cannot be parsed.
     */
    Date parse(String source) throws ParseException;

    /**
     * Equivalent to DateFormat.parse(String, ParsePosition).
     * <p/>
     * See {@link java.text.DateFormat#parse(String, ParsePosition)} for more information.
     *
     * @param source A <code>String</code>, part of which should be parsed.
     * @param pos    A <code>ParsePosition</code> object with index and error index information
     *               as described above.
     * @return A <code>Date</code> parsed from the string. In case of error, returns null.
     * @throws NullPointerException if text or pos is null.
     */
    Date parse(String source, ParsePosition pos);

    // Accessors
    //-----------------------------------------------------------------------

    /**
     * <p>Get the pattern used by this parser.</p>
     *
     * @return the pattern, {@link java.text.SimpleDateFormat} compatible
     */
    String getPattern();

    /**
     * <p>
     * Get the time zone used by this parser.
     * </p>
     * <p/>
     * <p>
     * The default {@link TimeZone} used to create a {@link Date} when the {@link TimeZone} is not specified by
     * the format pattern.
     * </p>
     *
     * @return the time zone
     */
    TimeZone getTimeZone();

    /**
     * <p>Get the locale used by this parser.</p>
     *
     * @return the locale
     */
    Locale getLocale();

    /**
     * Parses text from a string to produce a Date.
     *
     * @param source A <code>String</code> whose beginning should be parsed.
     * @return a <code>java.util.Date</code> object
     * @throws ParseException if the beginning of the specified string cannot be parsed.
     * @see java.text.DateFormat#parseObject(String)
     */
    Object parseObject(String source) throws ParseException;

    /**
     * Parse a date/time string according to the given parse position.
     *
     * @param source A <code>String</code> whose beginning should be parsed.
     * @param pos    the parse position
     * @return a <code>java.util.Date</code> object
     * @see java.text.DateFormat#parseObject(String, ParsePosition)
     */
    Object parseObject(String source, ParsePosition pos);
}
