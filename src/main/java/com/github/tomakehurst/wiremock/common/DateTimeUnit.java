package com.github.tomakehurst.wiremock.common;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;

public enum DateTimeUnit {
    SECONDS(Calendar.SECOND),
    MINUTES(Calendar.MINUTE),
    HOURS(Calendar.HOUR),
    DAYS(Calendar.DAY_OF_MONTH),
    MONTHS(Calendar.MONTH),
    YEARS(Calendar.YEAR);

    private final int calendarField;

    DateTimeUnit(int calendarField) {
        this.calendarField = calendarField;
    }

    public int getCalendarField() {
        return calendarField;
    }

    public TemporalUnit toTemporalUnit() {
        return ChronoUnit.valueOf(name());
    }
}
