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
package com.github.tomakehurst.wiremock.common;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeOffset {

    private final DateTimeUnit amountUnit;
    private final int amount;
    private final DateTimeTruncation truncation;

    public static DateTimeOffset fromString(String offset) {
        return fromString(offset, DateTimeTruncation.NONE);
    }

    public static DateTimeOffset fromString(String offset, DateTimeTruncation truncation) {
        String[] parts = offset.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Offset must be of the form <amount> <unit> e.g. 8 seconds");
        }

        int amount = Integer.parseInt(parts[0]);
        DateTimeUnit amountUnit = DateTimeUnit.valueOf(parts[1].toUpperCase());

        return new DateTimeOffset(amount, amountUnit, truncation);
    }

    public DateTimeOffset(int amount, DateTimeUnit amountUnit) {
        this(amount, amountUnit, DateTimeTruncation.NONE);
    }

    public DateTimeOffset(int amount, DateTimeUnit amountUnit, DateTimeTruncation truncation) {
        this.amountUnit = amountUnit;
        this.amount = amount;
        this.truncation = truncation;
    }

    public DateTimeUnit getAmountUnit() {
        return amountUnit;
    }

    public int getAmount() {
        return amount;
    }

    public Date shift(Date date) {
        final ZonedDateTime input = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("Z"));
        ZonedDateTime output = shift(input);
        return Date.from(output.toInstant());
    }

    public ZonedDateTime shift(ZonedDateTime dateTime) {
        return truncation.truncate(dateTime)
                         .plus(amount, amountUnit.toTemporalUnit());
    }

    @Override
    public String toString() {
        return amount + " " + amountUnit.name().toLowerCase();
    }
}
