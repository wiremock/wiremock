package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.junit.Test;

import java.text.DateFormat;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DateOffsetTest {

    static final DateFormat ISO8601 = new ISO8601DateFormat();

    @Test
    public void parsesSecondsOffset() {
        DateOffset offset = new DateOffset("7 seconds");
        assertThat(offset.getTimeUnit(), is(DateOffset.Unit.SECONDS));
        assertThat(offset.getAmount(), is(7));
    }

    @Test
    public void parsesMinutesOffset() {
        DateOffset offset = new DateOffset("78 minutes");
        assertThat(offset.getTimeUnit(), is(DateOffset.Unit.MINUTES));
        assertThat(offset.getAmount(), is(78));
    }

    @Test
    public void parsesHoursOffset() {
        DateOffset offset = new DateOffset("-12 hours");
        assertThat(offset.getTimeUnit(), is(DateOffset.Unit.HOURS));
        assertThat(offset.getAmount(), is(-12));
    }

    @Test
    public void parsesDaysOffset() {
        DateOffset offset = new DateOffset("1 days");
        assertThat(offset.getTimeUnit(), is(DateOffset.Unit.DAYS));
        assertThat(offset.getAmount(), is(1));
    }

    @Test
    public void parsesMonthsOffset() {
        DateOffset offset = new DateOffset("-12 months");
        assertThat(offset.getTimeUnit(), is(DateOffset.Unit.MONTHS));
        assertThat(offset.getAmount(), is(-12));
    }

    @Test
    public void parsesYearsOffset() {
        DateOffset offset = new DateOffset("101 years");
        assertThat(offset.getTimeUnit(), is(DateOffset.Unit.YEARS));
        assertThat(offset.getAmount(), is(101));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenUnparseableStringProvided() {
        new DateOffset("101");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenUnparseableUnitProvided() {
        new DateOffset("101 squillions");
    }

    @Test
    public void offsetsProvidedDateByConfiguredAmount() throws Exception {
        DateOffset offset = new DateOffset("3 days");
        Date startingDate = ISO8601.parse("2018-04-16T12:01:01Z");
        Date finalDate = offset.shift(startingDate);

        assertThat(ISO8601.format(finalDate), is("2018-04-19T12:01:01Z"));
    }


}
