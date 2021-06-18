package com.github.tomakehurst.wiremock.common;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static java.time.ZoneOffset.UTC;

public class DateTimeParser {

    private final DateTimeFormatter dateTimeFormatter;
    private final boolean isUnix;
    private final boolean isEpoch;

    private DateTimeParser(DateTimeFormatter dateTimeFormatter, boolean isUnix, boolean isEpoch) {
        this.dateTimeFormatter = dateTimeFormatter;
        this.isUnix = isUnix;
        this.isEpoch = isEpoch;
    }

    public static DateTimeParser forFormat(String format) {
        if (format.equalsIgnoreCase("unix")) {
            return new DateTimeParser(null, true, false);
        }

        if (format.equalsIgnoreCase("epoch")) {
            return new DateTimeParser(null, false, true);
        }

        return DateTimeParser.forFormatter(DateTimeFormatter.ofPattern(format));
    }

    public static DateTimeParser forFormatter(DateTimeFormatter dateTimeFormatter) {
        return new DateTimeParser(dateTimeFormatter, false, false);
    }

    public ZonedDateTime parseZonedDateTime(String dateTimeString) {
        if (dateTimeFormatter != null) {
            return ZonedDateTime.parse(dateTimeString, dateTimeFormatter);
        }

        if (isUnix) {
            long epochMillis = Long.parseLong(dateTimeString) * 1000;
            return Instant.ofEpochMilli(epochMillis).atZone(UTC);
        }

        if (isEpoch) {
            long epochMillis = Long.parseLong(dateTimeString);
            return Instant.ofEpochMilli(epochMillis).atZone(UTC);
        }

        return null;
    }

    public LocalDateTime parseLocalDateTime(String dateTimeString) {
        if (dateTimeFormatter != null) {
            return LocalDateTime.parse(dateTimeString, dateTimeFormatter);
        }

        if (isUnix) {
            long epochMillis = Long.parseLong(dateTimeString) * 1000;
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), UTC);
        }

        if (isEpoch) {
            long epochMillis = Long.parseLong(dateTimeString);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), UTC);
        }

        return null;
    }

    public LocalDate parseLocalDate(String dateTimeString) {
        if (dateTimeFormatter != null) {
            return LocalDate.parse(dateTimeString, dateTimeFormatter);
        }

        return null;
    }
}
