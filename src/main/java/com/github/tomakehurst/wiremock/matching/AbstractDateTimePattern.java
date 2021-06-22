package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.common.DateTimeOffset;
import com.github.tomakehurst.wiremock.common.DateTimeTruncation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Locale.US;

public abstract class AbstractDateTimePattern extends StringValuePattern {

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
    private final DateTimeTruncation truncateExpected;
    private final DateTimeTruncation truncateActual;

    public AbstractDateTimePattern(String dateTimeSpec) {
        this(dateTimeSpec, null, null, (DateTimeTruncation) null);
    }

    public AbstractDateTimePattern(DateTimeOffset offset, DateTimeTruncation truncateExpected, DateTimeTruncation truncateActual) {
        super(buildExpectedString(offset));
        this.dateOffset = offset;
        localDateTime = null;
        zonedDateTime = null;
        this.actualDateTimeFormat = null;
        actualDateTimeFormatter = null;
        this.truncateExpected = truncateExpected;
        this.truncateActual = truncateActual;
    }

    private static String buildExpectedString(DateTimeOffset dateTimeOffset) {
        return dateTimeOffset.getAmount() >= 0 ?
                "now +" + dateTimeOffset :
                "now " + dateTimeOffset;
    }

    // Call this from JSON creator constructor in subclasses
    public AbstractDateTimePattern(
            String dateTimeSpec,
            String actualDateFormat,
            String truncateExpected,
            String truncateActual
    ) {
        this(
                dateTimeSpec,
                actualDateFormat,
                truncateExpected != null ? DateTimeTruncation.fromString(truncateExpected) : null,
                truncateActual != null ? DateTimeTruncation.fromString(truncateActual) : null
        );
    }

    public AbstractDateTimePattern(
            String dateTimeSpec,
            String actualDateFormat,
            DateTimeTruncation truncateExpected,
            DateTimeTruncation truncateActual
    ) {
        super(dateTimeSpec);

        if (isNowOffsetExpression(dateTimeSpec)) {
            zonedDateTime = null;
            localDateTime = null;
            dateOffset = DateTimeOffset.fromString(dateTimeSpec);
        } else {
            zonedDateTime = parseZonedOrNull(dateTimeSpec);
            localDateTime = parseLocalOrNull(dateTimeSpec);
            dateOffset = null;
        }

        this.actualDateTimeFormat = actualDateFormat;
        actualDateTimeFormatter = actualDateFormat != null ? DateTimeFormatter.ofPattern(actualDateFormat) : null;

        this.truncateExpected = truncateExpected;
        this.truncateActual = truncateActual;
    }

    private static boolean isNowOffsetExpression(String dateTimeSpec) {
        return  dateTimeSpec.equalsIgnoreCase("now") ||
                dateTimeSpec.replaceAll("(?i)now ", "").matches("^[\\-+]?[0-9]+ [a-zA-Z]+$");
    }

    public String getFormat() {
        return actualDateTimeFormat;
    }

    public String getTruncateExpected() {
        return stringOrNull(truncateExpected);
    }

    public String getTruncateActual() {
        return stringOrNull(truncateActual);
    }

    private static String stringOrNull(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    @Override
    public MatchResult match(String value) {
        final ZonedDateTime zonedActual = parseZonedOrNull(value, actualDateTimeFormatter);
        final LocalDateTime localActual = parseLocalOrNull(value, actualDateTimeFormatter);

        final ZonedDateTime zonedExpectedDateTime = isNowOffset() ?
                getShiftedAndOffsetNow() :
                zonedDateTime;

        return getMatchResult(zonedExpectedDateTime, localDateTime, zonedActual, localActual);
    }

    private ZonedDateTime getShiftedAndOffsetNow() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime truncated = truncateActual != null ?
                truncateActual.truncate(now) :
                now;

        return dateOffset.shift(truncated);
    }

    protected abstract MatchResult getMatchResult(ZonedDateTime zonedExpected, LocalDateTime localExpected, ZonedDateTime zonedActual, LocalDateTime localActual);

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
