/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.DateTimeOffset;
import com.github.tomakehurst.wiremock.common.DateTimeTruncation;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.google.common.collect.Lists;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class EqualToDateTimePatternTest {

  @Test
  public void matchesZonedToZoned() {
    StringValuePattern matcher = WireMock.equalToDateTime("2021-06-14T12:13:14Z");

    assertTrue(matcher.match("2021-06-14T12:13:14Z").isExactMatch());
    assertFalse(matcher.match("1921-06-14T12:13:14Z").isExactMatch());
  }

  @Test
  public void matchesLiteralDateTimesWithDifferentZones() {
    StringValuePattern matcher = WireMock.equalToDateTime("2021-06-24T13:40:27+01:00");

    assertTrue(matcher.match("2021-06-24T12:40:27Z").isExactMatch());
    assertFalse(matcher.match("2021-06-24T13:40:27Z").isExactMatch());
  }

  @Test
  public void matchesLocalToLocal() {
    StringValuePattern matcher = WireMock.equalToDateTime("2021-06-14T12:13:14");

    assertTrue(matcher.match("2021-06-14T12:13:14").isExactMatch());
    assertFalse(matcher.match("1921-06-14T12:13:14").isExactMatch());
  }

  @Test
  public void matchesLocalToZoned() {
    StringValuePattern matcher = WireMock.equalToDateTime("2021-06-14T12:13:14");

    assertTrue(matcher.match("2021-06-14T12:13:14Z").isExactMatch());
    assertFalse(matcher.match("1921-06-14T12:13:14Z").isExactMatch());
  }

  @Test
  public void matchesZonedToLocal() {
    String localExpected = "2021-06-14T12:13:14";
    String zonedExpected =
        LocalDateTime.parse(localExpected).atZone(ZoneId.systemDefault()).toString();
    StringValuePattern matcher = WireMock.equalToDateTime(zonedExpected);

    String good = localExpected;
    String bad = LocalDateTime.parse(localExpected).minusSeconds(1).toString();

    assertTrue(matcher.match(good).isExactMatch());
    assertFalse(matcher.match(bad).isExactMatch());
  }

  @Test
  public void matchesNowToYearMonth() {
    YearMonth currentYearMonth = YearMonth.now();
    YearMonth previousYearMonth = currentYearMonth.minusMonths(1);
    StringValuePattern matcher =
        WireMock.isNow().truncateExpected(DateTimeTruncation.FIRST_DAY_OF_MONTH);

    String good = currentYearMonth.toString();
    String bad = previousYearMonth.toString();

    assertTrue(matcher.match(good).isExactMatch());
    assertFalse(matcher.match(bad).isExactMatch());
  }

  @Test
  public void matchesNowToYearMonthInCustomFormat() {
    YearMonth currentYearMonth = YearMonth.now();
    StringValuePattern matcher =
        WireMock.isNow()
            .truncateExpected(DateTimeTruncation.FIRST_DAY_OF_MONTH)
            .actualFormat("MM/yyyy");

    String good = currentYearMonth.format(DateTimeFormatter.ofPattern("MM/yyyy"));
    String bad = currentYearMonth.toString();

    assertTrue(matcher.match(good).isExactMatch());
    assertFalse(matcher.match(bad).isExactMatch());
  }

  @Test
  public void matchesNowToYear() {
    Year currentYear = Year.now();
    Year previousYear = currentYear.minusYears(1);
    StringValuePattern matcher =
        WireMock.isNow().truncateExpected(DateTimeTruncation.FIRST_DAY_OF_YEAR);

    String good = currentYear.toString();
    String bad = previousYear.toString();

    assertTrue(matcher.match(good).isExactMatch());
    assertFalse(matcher.match(bad).isExactMatch());
  }

  @Test
  public void matchesNowToYearInCustomFormat() {
    Year currentYear = Year.now();
    StringValuePattern matcher =
        WireMock.isNow().truncateExpected(DateTimeTruncation.FIRST_DAY_OF_YEAR).actualFormat("yy");

    String good = currentYear.format(DateTimeFormatter.ofPattern("yy"));
    String bad = currentYear.toString();

    assertTrue(matcher.match(good).isExactMatch());
    assertFalse(matcher.match(bad).isExactMatch());
  }

  @Test
  public void matchesActualInUnixTimeFormat() {
    String dateTime = "2021-06-14T12:13:14Z";
    StringValuePattern matcher = WireMock.equalToDateTime(dateTime).actualFormat("unix");

    String good = String.valueOf(Instant.parse(dateTime).getEpochSecond());
    String bad = String.valueOf(Instant.parse(dateTime).minusMillis(10).getEpochSecond());

    assertTrue(matcher.match(good).isExactMatch());
    assertFalse(matcher.match(bad).isExactMatch());
  }

  @Test
  public void matchesActualInEpochTimeFormat() {
    String dateTime = "2021-06-14T12:13:14Z";
    StringValuePattern matcher = WireMock.equalToDateTime(dateTime).actualFormat("epoch");

    String good = String.valueOf(Instant.parse(dateTime).toEpochMilli());
    String bad = String.valueOf(Instant.parse(dateTime).minusMillis(10).toEpochMilli());

    assertTrue(matcher.match(good).isExactMatch());
    assertFalse(matcher.match(bad).isExactMatch());
  }

  @Test
  public void doesNotMatchWhenActualValueIsNull() {
    StringValuePattern matcher = WireMock.equalToDateTime("2021-06-14T12:13:14Z");
    assertFalse(matcher.match(null).isExactMatch());
  }

  @Test
  public void returnsAReasonableDistanceWhenNoMatchForLocalExpectedZonedActual() {
    StringValuePattern matcher = WireMock.equalToDateTime("2021-01-01T00:00:00Z");
    assertThat(matcher.match("2023-01-01T00:00:00Z").getDistance(), is(0.5));
    assertThat(
        matcher.match("2121-01-01T00:00:00Z").getDistance(),
        allOf(greaterThan(0.5), lessThan(1.0)));
    assertThat(matcher.match(null).getDistance(), is(1.0));
    assertThat(
        matcher.match("2022-01-01T00:00:00Z").getDistance(),
        allOf(greaterThan(0.0), lessThan(0.5)));
  }

  @Test
  public void serialisesToJson() {
    EqualToDateTimePattern matcher =
        WireMock.isNow()
            .expectedOffset(DateTimeOffset.fromString("now -5 days"))
            .truncateExpected(DateTimeTruncation.LAST_DAY_OF_MONTH)
            .truncateActual(DateTimeTruncation.FIRST_DAY_OF_YEAR)
            .applyTruncationLast(true);

    assertThat(
        Json.write(matcher),
        jsonEquals(
            "{\n"
                + "  \"equalToDateTime\": \"now -5 days\",\n"
                + "  \"truncateExpected\": \"last day of month\",\n"
                + "  \"truncateActual\": \"first day of year\",\n"
                + "  \"applyTruncationLast\": true\n"
                + "}"));
  }

  @Test
  public void deserialisesFromJson() {
    StringValuePattern matcher =
        Json.read(
            "{\n"
                + "  \"equalToDateTime\": \"now\",\n"
                + "  \"truncateExpected\": \"first hour of day\",\n"
                + "  \"truncateActual\": \"first hour of day\"\n"
                + "}",
            EqualToDateTimePattern.class);

    ZonedDateTime good = ZonedDateTime.now().truncatedTo(DAYS);
    ZonedDateTime bad = ZonedDateTime.now().truncatedTo(DAYS).minus(5, HOURS);

    assertTrue(matcher.match(good.toString()).isExactMatch());
    assertFalse(matcher.match(bad.toString()).isExactMatch());
  }

  @Test
  public void deserialisesFromJsonWithApplyTruncationLast() {
    AbstractDateTimePattern matcher =
        Json.read(
            "{\n"
                + "  \"equalToDateTime\": \"now\",\n"
                + "  \"expectedOffset\": 1,\n"
                + "  \"expectedOffsetUnit\": \"months\",\n"
                + "  \"truncateExpected\": \"last day of month\",\n"
                + "  \"applyTruncationLast\": true\n"
                + "}",
            EqualToDateTimePattern.class);

    ZonedDateTime february1st = ZonedDateTime.parse("2024-02-01T00:00:00Z");

    ZonedDateTime march31st = february1st.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

    ZonedDateTime march29th = february1st.with(TemporalAdjusters.lastDayOfMonth()).plusMonths(1);

    // Mock static method ZonedDateTime::now so that it always returns 2024-02-01
    try (MockedStatic<ZonedDateTime> mockedZonedDateTime =
        Mockito.mockStatic(ZonedDateTime.class, Mockito.CALLS_REAL_METHODS)) {
      mockedZonedDateTime.when(ZonedDateTime::now).thenReturn(february1st);

      // Matcher expects March 31st when applyTruncationLast is set to true
      assertTrue(matcher.match(march31st.toString()).isExactMatch());

      AbstractDateTimePattern matcherWithApplyTruncationLast = matcher.applyTruncationLast(false);

      // Matcher expects March 29th when applyTruncationLast is set to false
      assertTrue(matcherWithApplyTruncationLast.match(march29th.toString()).isExactMatch());
    }
  }

  @Test
  public void acceptsJavaZonedDateTimeAsExpected() {
    EqualToDateTimePattern matcher =
        WireMock.equalToDateTime(ZonedDateTime.parse("2020-08-29T00:00:00Z"));
    assertTrue(matcher.match("2020-08-29T00:00:00Z").isExactMatch());
  }

  @Test
  public void acceptsJavaLocalDateTimeAsExpected() {
    EqualToDateTimePattern matcher =
        WireMock.equalToDateTime(LocalDateTime.parse("2020-08-29T00:00:00"));
    assertTrue(matcher.match("2020-08-29T00:00:00").isExactMatch());
  }

  @Test
  public void objectsShouldBeEqualOnSameExpectedValue() {
    EqualToDateTimePattern a = WireMock.equalToDateTime(LocalDateTime.parse("2020-08-29T00:00:00"));
    EqualToDateTimePattern b = WireMock.equalToDateTime(LocalDateTime.parse("2020-08-29T00:00:00"));
    EqualToDateTimePattern c = WireMock.equalToDateTime(LocalDateTime.parse("2022-01-10T10:10:10"));

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(b, a);
    assertEquals(b.hashCode(), a.hashCode());
    assertNotEquals(a, c);
    assertNotEquals(a.hashCode(), c.hashCode());
    assertNotEquals(b, c);
    assertNotEquals(b.hashCode(), c.hashCode());
  }

  @Test
  public void matchesMultipleZonedToMultipleLocalUsingHavingExactly() {
    String local1 = "2024-03-27T00:00:00";
    String zoned1 = LocalDateTime.parse(local1).atZone(ZoneId.systemDefault()).toString();
    String local2 = "2024-03-28T00:00:00";
    String zoned2 = LocalDateTime.parse(local2).atZone(ZoneId.systemDefault()).toString();
    MultiValuePattern matcher =
        WireMock.havingExactly(WireMock.equalToDateTime(zoned1), WireMock.equalToDateTime(zoned2));

    MultiValue good = new MultiValue("dateTimes", Lists.newArrayList(local1, local2));
    MultiValue bad =
        new MultiValue(
            "dateTimes",
            Lists.newArrayList(local1, LocalDateTime.parse(local2).minusSeconds(1).toString()));

    assertTrue(matcher.match(good).isExactMatch());
    assertFalse(matcher.match(bad).isExactMatch());
  }

  @Test
  public void matchesMultipleZonedToMultipleLocalUsingIncluding() {
    String local1 = "2024-03-27T00:00:00";
    String zoned1 = LocalDateTime.parse(local1).atZone(ZoneId.systemDefault()).toString();
    String local2 = "2024-03-28T00:00:00";
    String zoned2 = LocalDateTime.parse(local2).atZone(ZoneId.systemDefault()).toString();
    MultiValuePattern matcher =
        WireMock.including(WireMock.equalToDateTime(zoned1), WireMock.equalToDateTime(zoned2));
    String local3 = "2024-03-29T00:00:00";

    MultiValue good = new MultiValue("dateTimes", Lists.newArrayList(local1, local2, local3));
    MultiValue bad =
        new MultiValue(
            "dateTimes",
            Lists.newArrayList(
                local1, LocalDateTime.parse(local2).minusSeconds(1).toString(), local3));

    assertTrue(matcher.match(good).isExactMatch());
    assertFalse(matcher.match(bad).isExactMatch());
  }
}
