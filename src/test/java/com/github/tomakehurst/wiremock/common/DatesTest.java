/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.jupiter.api.Test;

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
