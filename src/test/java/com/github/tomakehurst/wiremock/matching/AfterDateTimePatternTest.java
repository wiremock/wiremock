package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.*;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

import static java.time.temporal.ChronoUnit.DAYS;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AfterDateTimePatternTest {

    @Test
    public void matchesZonedToZoned() {
        StringValuePattern matcher = WireMock.after("2021-06-14T12:13:14Z");

        assertTrue(matcher.match("2022-06-14T12:13:14Z").isExactMatch());
        assertFalse(matcher.match("2020-06-14T12:13:14Z").isExactMatch());
    }

    @Test
    public void matchesLocalToLocal() {
        StringValuePattern matcher = WireMock.after("2021-06-14T12:13:14");

        assertTrue(matcher.match("2022-06-14T12:13:14").isExactMatch());
        assertFalse(matcher.match("2020-06-14T12:13:14").isExactMatch());
    }

    @Test
    public void matchesLocalToZoned() {
        StringValuePattern matcher = WireMock.after("2021-06-14T12:13:14");

        assertTrue(matcher.match("2022-06-14T12:13:14Z").isExactMatch());
        assertFalse(matcher.match("2020-06-14T12:13:14Z").isExactMatch());
    }

    @Test
    public void matchesZonedToNowOffset() {
        StringValuePattern matcher = WireMock.afterNow().expectedOffset(27, DateTimeUnit.MINUTES);

        ZonedDateTime good = ZonedDateTime.now().plusHours(1);
        ZonedDateTime bad = ZonedDateTime.now().minusMinutes(1);
        assertTrue(matcher.match(good.toString()).isExactMatch());
        assertFalse(matcher.match(bad.toString()).isExactMatch());
    }

    @Test
    public void matchesNowWithExpectedAndActualTruncated() {
        StringValuePattern matcher = WireMock.afterNow()
                .truncateExpected(DateTimeTruncation.FIRST_DAY_OF_MONTH)
                .truncateActual(DateTimeTruncation.LAST_DAY_OF_MONTH);

        ZonedDateTime good = ZonedDateTime.now();
        ZonedDateTime bad = ZonedDateTime.now().minusMonths(1).minusHours(1);

        assertTrue(matcher.match(good.toString()).isExactMatch());
        assertFalse(matcher.match(bad.toString()).isExactMatch());
    }

    @Test
    public void returnsAReasonableDistanceWhenNoMatchForLocalExpectedZonedActual() {
        StringValuePattern matcher = WireMock.after("2021-01-01T00:00:00Z");
        assertThat(matcher.match("1971-01-01T00:00:00Z").getDistance(), is(0.5));
        assertThat(matcher.match("1921-01-01T00:00:00Z").getDistance(), is(1.0));
        assertThat(matcher.match("2020-01-01T00:00:00Z").getDistance(), is(0.01));
    }

    @Test
    public void serialisesToJson() {
        AfterDateTimePattern matcher = WireMock.afterNow()
                .expectedOffset(DateTimeOffset.fromString("now -5 days"))
                .truncateExpected(DateTimeTruncation.LAST_DAY_OF_MONTH)
                .truncateActual(DateTimeTruncation.FIRST_DAY_OF_YEAR);

        assertThat(Json.write(matcher), jsonEquals("{\n" +
                "  \"after\": \"now -5 days\",\n" +
                "  \"truncateExpected\": \"last day of month\",\n" +
                "  \"truncateActual\": \"first day of year\"\n" +
                "}"));
    }

    @Test
    public void deserialisesFromJson() {
        AfterDateTimePattern matcher = Json.read("{\n" +
                "  \"after\": \"now\",\n" +
                "  \"truncateExpected\": \"first hour of day\",\n" +
                "  \"truncateActual\": \"last day of year\"\n" +
                "}", AfterDateTimePattern.class);

        assertThat(matcher.getExpected(), is("now +0 seconds"));
        assertThat(matcher.getTruncateExpected(), is("first hour of day"));
        assertThat(matcher.getTruncateActual(), is("last day of year"));
    }

}
