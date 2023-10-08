/*
 * Copyright (C) 2018-2023 Thomas Akehurst
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

import static org.assertj.core.api.Assertions.assertThat;

import com.github.jknack.handlebars.Options;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ParseDateHelperTest {
  private ParseDateHelper helper;

  @BeforeEach
  public void init() {
    helper = new ParseDateHelper();
  }

  @Test
  public void parsesAnISO8601DateWhenNoFormatSpecified() throws Exception {
    Map<String, Object> optionsHash = Map.of();

    String inputDate = "2018-05-01T01:02:03Z";
    Object output = render(inputDate, optionsHash);

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    df.setTimeZone(TimeZone.getTimeZone("CEST"));

    Date expectedDate = df.parse(inputDate);
    assertThat(output).isInstanceOf(Date.class);
    assertThat(output).isEqualTo(expectedDate);
  }

  @Test
  public void parsesAnRFC1123DateWhenNoFormatSpecified() throws Exception {
    Map<String, Object> optionsHash = Map.of();

    String inputDate = "Tue, 01 Jun 2021 15:16:17 GMT";
    Object output = render(inputDate, optionsHash);

    Date expectedDate =
        Date.from(Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(inputDate)));
    assertThat(output).isInstanceOf(Date.class);
    assertThat(output).isEqualTo(expectedDate);
  }

  @Test
  public void parsesDateWithSuppliedFormat() throws Exception {
    Map<String, Object> optionsHash = Map.of("format", "dd/MM/yyyy");

    String inputDate = "01/02/2003";
    Object output = render(inputDate, optionsHash);

    Date expectedDate = Date.from(Instant.parse("2003-02-01T00:00:00Z"));
    assertThat(output).isInstanceOf(Date.class);
    assertThat(output).isEqualTo(expectedDate);
  }

  @Test
  public void parsesLocalDateTimeWithSuppliedFormat() throws Exception {
    Map<String, Object> optionsHash = Map.of("format", "dd/MM/yyyy HH:mm:ss");

    String inputDate = "01/02/2003 05:06:07";
    Object output = render(inputDate, optionsHash);

    Date expectedDate = Date.from(Instant.parse("2003-02-01T05:06:07Z"));
    assertThat(output).isInstanceOf(Date.class);
    assertThat(output).isEqualTo(expectedDate);
  }

  @Test
  public void parsesDateTimeWithEpochFormat() throws Exception {
    Map<String, Object> optionsHash = Map.of("format", "epoch");

    String inputDate = "1577964091000";
    Object output = render(inputDate, optionsHash);

    Date expectedDate = Date.from(Instant.parse("2020-01-02T11:21:31Z"));
    assertThat(output).isInstanceOf(Date.class);
    assertThat(output).isEqualTo(expectedDate);
  }

  private Object render(String context, Map<String, Object> optionsHash) throws IOException {
    return helper.apply(
        context, new Options.Builder(null, null, null, null, null).setHash(optionsHash).build());
  }
}
