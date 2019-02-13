/*
 * Copyright (C) 2011 Thomas Akehurst
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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateOffset {

    public enum Unit {
        SECONDS(Calendar.SECOND),
        MINUTES(Calendar.MINUTE),
        HOURS(Calendar.HOUR),
        DAYS(Calendar.DAY_OF_MONTH),
        MONTHS(Calendar.MONTH),
        YEARS(Calendar.YEAR);

        private final int calendarField;

        Unit(int calendarField) {
            this.calendarField = calendarField;
        }

        public int getCalendarField() {
            return calendarField;
        }
    }

    private final Unit timeUnit;
    private final int amount;

    public DateOffset(String offset) {
        String[] parts = offset.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Offset must be of the form <amount> <unit> e.g. 8 seconds");
        }

        amount = Integer.parseInt(parts[0]);
        timeUnit = Unit.valueOf(parts[1].toUpperCase());
    }

    public Unit getTimeUnit() {
        return timeUnit;
    }

    public int getAmount() {
        return amount;
    }

    public Date shift(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Z"));
        calendar.setTime(date);
        calendar.add(timeUnit.calendarField, amount);
        return calendar.getTime();
    }
}
