package com.github.tomakehurst.wiremock.common;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Function;

import static java.time.temporal.ChronoUnit.*;

public enum DateTimeTruncation {

    NONE(input -> input),

    FIRST_SECOND_OF_MINUTE(input -> input.truncatedTo(MINUTES)),
    FIRST_MINUTE_OF_HOUR(input -> input.truncatedTo(HOURS)),
    FIRST_HOUR_OF_DAY(input -> input.truncatedTo(DAYS)),

    FIRST_DAY_OF_MONTH(input -> input.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(DAYS)),
    FIRST_DAY_OF_NEXT_MONTH(input -> input.with(TemporalAdjusters.firstDayOfNextMonth()).truncatedTo(DAYS)),
    LAST_DAY_OF_MONTH(input -> input.with(TemporalAdjusters.lastDayOfMonth()).truncatedTo(DAYS)),

    FIRST_DAY_OF_YEAR(input -> input.with(TemporalAdjusters.firstDayOfYear()).truncatedTo(DAYS)),
    FIRST_DAY_OF_NEXT_YEAR(input -> input.with(TemporalAdjusters.firstDayOfNextYear()).truncatedTo(DAYS)),
    LAST_DAY_OF_YEAR(input -> input.with(TemporalAdjusters.lastDayOfYear()).truncatedTo(DAYS));

    private final Function<ZonedDateTime, ZonedDateTime> fn;

    DateTimeTruncation(Function<ZonedDateTime, ZonedDateTime> fn) {
        this.fn = fn;
    }

    public ZonedDateTime truncate(ZonedDateTime input) {
        return fn.apply(input);
    }

    public static DateTimeTruncation fromString(String value) {
        final String normalisedKey = value.toUpperCase().replace(' ', '_');
        return valueOf(normalisedKey);
    }
}
