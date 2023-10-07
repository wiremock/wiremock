package com.github.tomakehurst.wiremock.common;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DatesTest {

    @Test
    void mapsValidInputAsDate() {
        // given
        var input = "2023-10-07T00:00:00Z";
        var expected = new GregorianCalendar(2023, Calendar.OCTOBER, 7).getTime();

        // when
        var result = Dates.parse(input);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void throwsExceptionWhenMappingInvalidInput() {
        // given
        var input = "invalid";

        // when + then
        assertThrows(ParseException.class, () -> Dates.parse(input));
    }

    @Test
    void parseDateToTextualDate() {
        // given
        var input = "2023-10-07T00:00:00Z";
        var expected = new GregorianCalendar(2023, Calendar.OCTOBER, 7).getTime();

        // when
        var result = Dates.parse(input);

        // then
        assertThat(result).isEqualTo(expected);
    }
}
