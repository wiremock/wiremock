/*
 * Copyright (C) 2018-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.annotation.JsonValue;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class RenderableDate extends Date {
  private static final long DIVIDE_MILLISECONDS_TO_SECONDS = 1000L;
  private static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

  private final String format;
  private final ZoneId timezone;

  public RenderableDate(Date date, String format, ZoneId timezone) {
    super(date.getTime());
    this.format = format;
    this.timezone = timezone;
  }

  public String getFormat() {
    return format;
  }

  public ZoneId getTimezone() {
    return timezone;
  }

  @Override
  @JsonValue
  public String toString() {
    if (format != null) {
      if (format.equals("epoch")) {
        return String.valueOf(getTime());
      }

      if (format.equals("unix")) {
        return String.valueOf(getTime() / DIVIDE_MILLISECONDS_TO_SECONDS);
      }

      return formatCustom();
    }

    return timezone != null
        ? format(this, false, TimeZone.getTimeZone(timezone), Locale.US)
        : format(this, false, TIMEZONE_UTC, Locale.US);
  }

  private String formatCustom() {
    ZonedDateTime zonedDateTime =
        toInstant().atZone(timezone != null ? timezone : ZoneId.systemDefault());

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
    return formatter.format(zonedDateTime);
  }

  /**
   * Originally copied from <a
   * href="https://github.com/FasterXML/jackson-databind/blob/b02bf81563a9382cfb163b2e6d43872ca5a07d45/src/main/java/com/fasterxml/jackson/databind/util/ISO8601Utils.java#L58-L98">Jackson
   * Databind 2</a>.
   *
   * <p>Format date into yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
   *
   * @param date the date to format
   * @param millis true to include millis precision otherwise false
   * @param tz timezone to use for the formatting (UTC will produce 'Z')
   * @return the date formatted as yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
   */
  private static String format(Date date, boolean millis, TimeZone tz, Locale loc) {
    Calendar calendar = new GregorianCalendar(tz, loc);
    calendar.setTime(date);

    // estimate capacity of buffer as close as we can (yeah, that's pedantic ;)
    StringBuilder sb = new StringBuilder(30);
    sb.append(
        String.format(
            "%04d-%02d-%02dT%02d:%02d:%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)));
    if (millis) {
      sb.append(String.format(".%03d", calendar.get(Calendar.MILLISECOND)));
    }

    int offset = tz.getOffset(calendar.getTimeInMillis());
    if (offset != 0) {
      int hours = Math.abs((offset / (60 * 1000)) / 60);
      int minutes = Math.abs((offset / (60 * 1000)) % 60);
      sb.append(String.format("%c%02d:%02d", (offset < 0 ? '-' : '+'), hours, minutes));
    } else {
      sb.append('Z');
    }
    return sb.toString();
  }
}
