package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.DateTimeOffset;
import com.github.tomakehurst.wiremock.common.DateTimeTruncation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Locale.US;

public class BeforeDateTimePattern extends StringValuePattern {

    private static final DateTimeFormatter RFC_1036_DATE_TIME = DateTimeFormatter.ofPattern("EEEE, dd-MMM-yy HH:mm:ss zzz").withLocale(US);
    private static final DateTimeFormatter ASCTIME1 = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy").withZone(ZoneId.of("GMT"));
    private static final DateTimeFormatter ASCTIME2 = DateTimeFormatter.ofPattern("EEE MMM  d HH:mm:ss yyyy").withZone(ZoneId.of("GMT"));
    private static final List<DateTimeFormatter> ZONED_FORMATTERS = asList(
            ISO_ZONED_DATE_TIME,
            RFC_1123_DATE_TIME,
            RFC_1036_DATE_TIME,
            ASCTIME1,
            ASCTIME2
    );

    private final ZonedDateTime zonedDateTime;
    private final LocalDateTime localDateTime;
    private final String actualDateTimeFormat;
    private final DateTimeFormatter actualDateTimeFormatter;
    private final DateTimeOffset dateOffset;

    public BeforeDateTimePattern(String dateTimeString) {
        this(dateTimeString, null, null);
    }

    public BeforeDateTimePattern(DateTimeOffset dateOffset) {
        super(dateOffset.toString());
        this.dateOffset = dateOffset;
        localDateTime = null;
        zonedDateTime = null;
        this.actualDateTimeFormat = null;
        actualDateTimeFormatter = null;
    }

    public BeforeDateTimePattern(
            @JsonProperty("before") String dateTimeSpec,
            @JsonProperty("format") String actualDateFormat,
            @JsonProperty("truncate") String truncate
    ) {
        super(dateTimeSpec);

        if (isNowOffsetExpression(dateTimeSpec)) {
            zonedDateTime = null;
            localDateTime = null;

            DateTimeTruncation truncation = truncate != null ?
                    DateTimeTruncation.fromString(truncate) :
                    DateTimeTruncation.NONE;
            dateOffset = DateTimeOffset.fromString(dateTimeSpec, truncation);
        } else {
            zonedDateTime = parseZonedOrNull(dateTimeSpec);
            localDateTime = parseLocalOrNull(dateTimeSpec);
            dateOffset = null;
        }

        this.actualDateTimeFormat = actualDateFormat;
        actualDateTimeFormatter = actualDateFormat != null ? DateTimeFormatter.ofPattern(actualDateFormat) : null;
    }

    private static boolean isNowOffsetExpression(String dateTimeSpec) {
        return dateTimeSpec.matches("^\\-?[0-9]+ [a-zA-Z]+$");
    }

    public String getBefore() {
        return expectedValue;
    }

    public String getFormat() {
        return actualDateTimeFormat;
    }

    public String getTruncate() {
        if (isNowOffset() && dateOffset.getTruncation() != null) {
            return dateOffset.getTruncation().toString();
        }

        return null;
    }

    @Override
    public MatchResult match(String value) {
        final ZonedDateTime zonedActual = parseZonedOrNull(value, actualDateTimeFormatter);
        final LocalDateTime localActual = parseLocalOrNull(value, actualDateTimeFormatter);

        final ZonedDateTime zonedExpectedDateTime = isNowOffset() ?
                dateOffset.shift(ZonedDateTime.now()) :
                zonedDateTime;
        final boolean isZoned = zonedExpectedDateTime != null;

        return new MatchResult() {
            @Override
            public boolean isExactMatch() {
                boolean isMatch = false;
                if (isZoned && zonedActual != null) {
                    isMatch = zonedActual.isBefore(zonedExpectedDateTime);
                } else if (isLocal() && localActual != null) {
                    isMatch = localActual.isBefore(localDateTime);
                } else if (isLocal() && zonedActual != null) {
                    isMatch = zonedActual.toLocalDateTime().isBefore(localDateTime);
                }

                return isMatch;
            }

            @Override
            public double getDistance() {
                double distance = -1;
                if (isZoned && zonedActual != null) {
                    distance = calculateDistance(zonedExpectedDateTime, zonedActual);
                } else if (isLocal() && localActual != null) {
                    distance = calculateDistance(localDateTime, localActual);
                } else if (isLocal() && zonedActual != null) {
                    distance = calculateDistance(localDateTime, zonedActual.toLocalDateTime());
                }

                return distance;
            }

            private double calculateDistance(Temporal start, Temporal end) {
                double distance = ((double) ChronoUnit.YEARS.between(start, end)) / 100;
                distance = Math.abs(distance);
                return Math.min(distance, 1.0);
            }
        };
    }

    private boolean isLocal() {
        return localDateTime != null;
    }

    private boolean isNowOffset() {
        return dateOffset != null;
    }

    private static ZonedDateTime parseZonedOrNull(String dateTimeString) {
        return parseZonedOrNull(dateTimeString, (DateTimeFormatter) null);
    }

    private static ZonedDateTime parseZonedOrNull(String dateTimeString, DateTimeFormatter formatter) {
        final List<DateTimeFormatter> formatters = formatter != null ? singletonList(formatter) : ZONED_FORMATTERS;
        return parseZonedOrNull(dateTimeString, formatters);
    }

    private static ZonedDateTime parseZonedOrNull(String dateTimeString, List<DateTimeFormatter> formatters) {
        if (formatters.isEmpty()) {
            return null;
        }

        try {
            return ZonedDateTime.parse(dateTimeString, formatters.get(0));
        } catch (DateTimeParseException e) {
            return parseZonedOrNull(dateTimeString, formatters.subList(1, formatters.size()));
        }
    }

    private static LocalDateTime parseLocalOrNull(String dateTimeString) {
        return parseLocalOrNull(dateTimeString, null);
    }

    private static LocalDateTime parseLocalOrNull(String dateTimeString, DateTimeFormatter formatter) {
        try {
            return formatter != null ?
                    LocalDateTime.parse(dateTimeString, formatter) :
                    LocalDateTime.parse(dateTimeString);
        } catch (DateTimeParseException ignored) {
            try {
                return (formatter != null ?
                            LocalDate.parse(dateTimeString, formatter) :
                            LocalDate.parse(dateTimeString))
                        .atStartOfDay();
            } catch (DateTimeParseException ignored2) {
                return null;
            }
        }
    }
}
