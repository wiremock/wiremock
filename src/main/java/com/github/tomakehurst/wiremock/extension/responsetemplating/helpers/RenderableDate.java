/*
 * Copyright (C) 2018-2021 Thomas Akehurst
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

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

public class RenderableDate extends Date {
  private static final long DIVIDE_MILLISECONDS_TO_SECONDS = 1000L;

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
        ? ISO8601Utils.format(this, false, TimeZone.getTimeZone(timezone))
        : ISO8601Utils.format(this, false);
  }

  private String formatCustom() {
    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    if (timezone != null) {
      TimeZone zone = TimeZone.getTimeZone(timezone);
      dateFormat.setTimeZone(zone);
    }
    return dateFormat.format(this);
  }
}
