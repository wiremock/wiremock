package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.DateTimeTruncation;
import com.github.tomakehurst.wiremock.common.DateTimeUnit;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DateTimePatternsTest {

    @Test
    public void matchesZonedISO8601BeforeZonedLiteralDateTime() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15Z");

        assertTrue(matcher.match("2021-06-01T15:15:15Z").isExactMatch());
        assertFalse(matcher.match("2021-07-01T23:59:59Z").isExactMatch());
    }

    @Test
    public void doesNotMatchLocalISO8601BeforeZonedLiteralDateTime() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15Z");

        // Shouldn't match even if it's apparently right as a local -> zoned comparison does not make sense
        assertFalse(matcher.match("2021-06-01T15:15:15").isExactMatch());
        assertFalse(matcher.match("2021-07-01T23:59:59").isExactMatch());
    }

    @Test
    public void matchesLocalISO8601BeforeLocalLiteralDateTime() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15");

        assertTrue(matcher.match("2021-06-01T15:15:15").isExactMatch());
        assertFalse(matcher.match("2021-07-01T23:59:59").isExactMatch());
    }

    @Test
    public void matchesZonedISO8601BeforeLocalLiteralDateTime() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15");

        assertTrue(matcher.match("2021-06-01T15:15:15Z").isExactMatch());
        assertFalse(matcher.match("2021-07-01T23:59:59Z").isExactMatch());
    }

    @Test
    public void doesNotMatchWhenActualValueUnparseable() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15");
        assertFalse(matcher.match("2021-06-01T15:15:blahsdfj123").isExactMatch());
    }

    @Test
    public void doesNotMatchWhenExpectedValueUnparseable() {
        StringValuePattern matcher = WireMock.before("2021-06-wrongstuff:15:15");
        assertFalse(matcher.match("2021-06-01T15:15:15Z").isExactMatch());
    }

    @Test
    public void returnsAReasonableDistanceWhenNoMatchForZonedExpectedZonedActual() {
        StringValuePattern matcher = WireMock.before("2021-01-01T00:00:00Z");
        assertThat(matcher.match("2071-01-01T00:00:00Z").getDistance(), is(0.5));
        assertThat(matcher.match("2121-01-01T00:00:00Z").getDistance(), is(1.0));
        assertThat(matcher.match("2022-01-01T00:00:00Z").getDistance(), is(0.01));
    }

    @Test
    public void returnsAReasonableDistanceWhenNoMatchForLocalExpectedZonedActual() {
        StringValuePattern matcher = WireMock.before("2021-01-01T00:00:00");
        assertThat(matcher.match("2071-01-01T00:00:00Z").getDistance(), is(0.5));
        assertThat(matcher.match("2121-01-01T00:00:00Z").getDistance(), is(1.0));
        assertThat(matcher.match("2022-01-01T00:00:00Z").getDistance(), is(0.01));
    }

    @Test
    public void returnsAReasonableDistanceWhenNoMatchForLocalExpectedLocalActual() {
        StringValuePattern matcher = WireMock.before("2021-01-01T00:00:00");
        assertThat(matcher.match("2071-01-01T00:00:00").getDistance(), is(0.5));
        assertThat(matcher.match("2121-01-01T00:00:00").getDistance(), is(1.0));
        assertThat(matcher.match("2022-01-01T00:00:00").getDistance(), is(0.01));
    }

    @Test
    public void matchesZonedRFC1123ActualDate() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15Z");

        assertTrue(matcher.match("Tue, 01 Jun 2021 15:16:17 GMT").isExactMatch());
        assertFalse(matcher.match("Thu, 01 Jul 2021 15:16:17 GMT").isExactMatch());
    }

    @Test
    public void matchesZonedRFC1036ActualDate() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15Z");

        assertTrue(matcher.match("Tuesday, 01-Jun-21 14:14:14 GMT").isExactMatch());
        assertFalse(matcher.match("Thursday, 01-Jul-21 15:16:17 GMT").isExactMatch());
    }

    @Test
    public void matchesZonedSingleDigitDayAsctimeActualDate() {
        StringValuePattern matcher = WireMock.before("2021-06-14T01:01:01Z");

        assertTrue(matcher.match("Tue Jun  1 01:01:01 2021").isExactMatch());
        assertFalse(matcher.match("Thu Jul  1 01:01:01 2021").isExactMatch());
    }

    @Test
    public void matchesZonedDoubleDigitDayAsctimeActualDate() {
        StringValuePattern matcher = WireMock.before("2021-06-14T01:01:01Z");

        assertTrue(matcher.match("Thu Jun 10 01:01:01 2021").isExactMatch());
        assertFalse(matcher.match("Sat Jul 10 01:01:01 2021").isExactMatch());
    }

    @Test
    public void matchesNonUTCZonedISO8601ActualDate() {
        StringValuePattern matcher = WireMock.before("2021-06-14T15:15:15Z");

        assertTrue(matcher.match("2021-06-14T15:15:15+01:00[Europe/London]").isExactMatch());
        assertFalse(matcher.match("2021-06-14T16:15:15+01:00[Europe/London]").isExactMatch());
    }

    @Test
    public void matchesActualDateAccordingToSpecifiedFormat() {
        StringValuePattern matcher = WireMock.before("2021-06-14", "dd/MM/yyyy");

        assertTrue(matcher.match("01/06/2021").isExactMatch());
        assertFalse(matcher.match("01/07/2021").isExactMatch());
    }

    @Test
    public void matchesAgainstOffsetFromNow() {
        StringValuePattern matcher = WireMock.beforeNow("-5 days");

        String right = ZonedDateTime.now().minusDays(7).toString();
        assertTrue(matcher.match(right).isExactMatch());

        String wrong = ZonedDateTime.now().minusDays(4).toString();
        assertFalse(matcher.match(wrong).isExactMatch());
    }

    @Test
    public void truncatesActualDateToSpecifiedUnitWhenUsingLiteralBound() {
        StringValuePattern matcher = WireMock.beforeNow(DateTimeTruncation.FIRST_DAY_OF_MONTH, "15 days"); // Before the 15th of this month

        TemporalAdjuster truncateToMonth = TemporalAdjusters.firstDayOfMonth();
        ZonedDateTime good = ZonedDateTime.now().with(truncateToMonth).plus(14, DAYS);
        ZonedDateTime bad = ZonedDateTime.now().with(truncateToMonth).plus(16, DAYS);

        assertTrue(matcher.match(good.toString()).isExactMatch());
        assertFalse(matcher.match(bad.toString()).isExactMatch());
    }

}
