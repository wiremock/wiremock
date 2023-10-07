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

import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.testsupport.WireMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.HandlebarsHelperTestBase.transform;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.ExtensionFactoryUtils.buildTemplateTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class HandlebarsCurrentDateHelperTest {

  private HandlebarsCurrentDateHelper helper;
  private ResponseTemplateTransformer transformer;

  @BeforeEach
  public void init() {
    helper = new HandlebarsCurrentDateHelper();
    transformer = buildTemplateTransformer(true);

    LocalNotifier.set(new ConsoleNotifier(true));
  }

  @Test
  public void rendersNowDateTime() throws Exception {
    Map<String, Object> optionsHash = Map.of();

    Object output = render(optionsHash);

    assertThat(output, instanceOf(RenderableDate.class));
    assertThat(output.toString(), WireMatchers.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:]+Z$"));
  }

  @Test
  public void rendersNowDateTimeWithCustomFormat() throws Exception {
    Map<String, Object> optionsHash = Map.of("format", "yyyy/mm/dd");

    Object output = render(optionsHash);

    assertThat(output, instanceOf(RenderableDate.class));
    assertThat(output.toString(), WireMatchers.matches("^[0-9]{4}/[0-9]{2}/[0-9]{2}$"));
  }

  @Test
  public void rendersPassedDateTimeWithDayOffset() throws Exception {
    String format = "yyyy-MM-dd";
    SimpleDateFormat df = new SimpleDateFormat(format);
    Map<String, Object> optionsHash = Map.of("format", format, "offset", "5 days");

    Object output = render(df.parse("2018-04-16"), optionsHash);

    assertThat(output.toString(), is("2018-04-21"));
  }

  @Test
  public void rendersNowWithDayOffset() throws Exception {
    Map<String, Object> optionsHash = Map.of("offset", "6 months");

    Object output = render(optionsHash);

    System.out.println(output);
  }

  @Test
  public void rendersNowAsUnixEpochInMilliseconds() throws Exception {
    Map<String, Object> optionsHash = Map.of("format", "epoch");

    Date date = new Date();
    Object output = render(date, optionsHash);

    assertThat(output.toString(), is(String.valueOf(date.getTime())));
  }

  @Test
  public void rendersNowAsUnixEpochInSeconds() throws Exception {
    Map<String, Object> optionsHash = Map.of("format", "unix");

    Date date = new Date();
    Object output = render(date, optionsHash);

    assertThat(output.toString(), is(String.valueOf(date.getTime() / 1000L)));
  }

  @Test
  public void adjustsISO8601ToSpecifiedTimezone() throws Exception {
    Map<String, Object> optionsHash = Map.of("offset", "3 days", "timezone", "Australia/Sydney");

    Date inputDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'").parse("2023-10-07T00:00:00.00Z");
    Object output = render(inputDate, optionsHash);

    assertThat(output.toString(), is("2023-10-10T00:00:00+11:00"));
  }

  @Test
  public void adjustsCustomFormatToSpecifiedTimezone() throws Exception {
    Map<String, Object> optionsHash =
        Map.of(
            "offset", "3 days", "timezone", "Australia/Sydney", "format", "yyyy-MM-dd HH:mm:ssZ");

    Date inputDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'").parse("2023-10-07T00:00:00.00Z");
    Object output = render(inputDate, optionsHash);

    assertThat(output.toString(), is("2023-10-10 00:00:00+1100"));
  }

  @Test
  public void helperIsIncludedInTemplateTransformerWithNowTagName() {
    final ResponseDefinition responseDefinition =
        transform(
            transformer,
            mockRequest().url("/random-value"),
            aResponse().withBody("{{now offset='6 days'}}"));

    String body = responseDefinition.getBody().trim();
    assertThat(body, WireMatchers.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:]+Z$"));
  }

  @Test
  public void helperIsIncludedInTemplateTransformerWithDateTagName() {
    final ResponseDefinition responseDefinition =
        transform(
            transformer,
            mockRequest().url("/random-value"),
            aResponse().withBody("{{date offset='6 days'}}"));

    String body = responseDefinition.getBody().trim();
    assertThat(body, WireMatchers.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:]+Z$"));
  }

  @Test
  public void acceptsDateParameterwithDateTagName() {
    final ResponseDefinition responseDefinition =
        transform(
            transformer,
            mockRequest().url("/parsed-date"),
            aResponse().withBody("{{date (parseDate '2018-05-05T10:11:12Z') offset='-1 days'}}"));

    String body = responseDefinition.getBody().trim();
    assertThat(body, is("2018-05-04T10:11:12Z"));
  }

  private Object render(Map<String, Object> optionsHash) throws IOException {
    return render(null, optionsHash);
  }

  private Object render(Date context, Map<String, Object> optionsHash) throws IOException {
    return helper.apply(
        context, new Options.Builder(null, null, null, null, null).setHash(optionsHash).build());
  }
}
