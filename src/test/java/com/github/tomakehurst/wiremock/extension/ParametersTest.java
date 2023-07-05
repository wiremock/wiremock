/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ParametersTest {

  @Test
  void convertsParametersToAnObject() {
    MyData myData =
        Parameters.from(Map.of("name", "Tom", "num", 27, "date", "2023-01-01")).as(MyData.class);

    assertThat(myData.getName(), is("Tom"));
    assertThat(myData.getNum(), is(27));
    assertThat(myData.getDate(), is(LocalDate.of(2023, 1, 1)));
  }

  @Test
  void convertsToParametersFromAnObject() {
    MyData myData = new MyData("Mark", 12, LocalDate.of(2023, 1, 1));

    Parameters parameters = Parameters.of(myData);

    assertThat(parameters.getString("name"), is("Mark"));
    assertThat(parameters.getInt("num"), is(12));
    assertThat(parameters.getString("date"), is("2023-01-01"));
  }

  public static class MyData {

    private final String name;
    private final Integer num;
    private final LocalDate date;

    @JsonCreator
    public MyData(
        @JsonProperty("name") String name,
        @JsonProperty("num") Integer num,
        @JsonProperty("date") LocalDate date) {
      this.name = name;
      this.num = num;
      this.date = date;
    }

    public String getName() {
      return name;
    }

    public Integer getNum() {
      return num;
    }

    public LocalDate getDate() {
      return date;
    }
  }
}
