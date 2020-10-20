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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.junit.Test;

import java.text.DateFormat;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
