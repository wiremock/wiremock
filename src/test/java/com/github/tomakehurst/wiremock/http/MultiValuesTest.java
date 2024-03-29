/*
 * Copyright (C) 2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MultiValuesTest {

  @Test
  public void summary_0_cookies() {
    MultiValues<Cookie> cookies = new MultiValues<>(new HashMap<>());

    assertThat(cookies.summary()).isEqualTo("");
  }

  @Test
  public void serialization() {
    MultiValues<Cookie> originalCookies =
        new MultiValues<>(
            Map.of(
                "cookie1",
                new Cookie(
                    Arrays.asList(
                        "cookie1_value1",
                        "cookie1_value2",
                        "cookie1_value3",
                        "cookie1_value4",
                        "cookie1_value5"))));

    String expectedJsonString =
        "{\n"
            + "  \"cookie1\" : [ \"cookie1_value1\", \"cookie1_value2\", \"cookie1_value3\", \"cookie1_value4\", \"cookie1_value5\" ]\n"
            + "}";
    assertThat(Json.write(originalCookies)).isEqualToIgnoringWhitespace(expectedJsonString);

    assertThat(
            Json.write(Json.read(expectedJsonString, new TypeReference<MultiValues<Cookie>>() {})))
        .isEqualToIgnoringWhitespace(expectedJsonString);
  }

  @Test
  public void summary_1_cookie_1_value() {
    MultiValues<Cookie> cookies =
        new MultiValues<>(Map.of("cookie1", new Cookie(Arrays.asList("cookie1_value1"))));

    assertThat(cookies.summary()).isEqualTo("cookie1: [cookie1_value1]");
  }

  @Test
  public void summary_1_cookie_5_value() {
    MultiValues<Cookie> cookies =
        new MultiValues<>(
            Map.of(
                "cookie1",
                new Cookie(
                    Arrays.asList(
                        "cookie1_value1",
                        "cookie1_value2",
                        "cookie1_value3",
                        "cookie1_value4",
                        "cookie1_value5"))));

    assertThat(cookies.summary())
        .isEqualTo(
            "cookie1: [cookie1_value1, cookie1_value2, cookie1_value3, cookie1_value4, cookie1_value5]");
  }

  @Test
  public void summary_2_cookies_2_values() {
    MultiValues<Cookie> cookies =
        new MultiValues<>(
            Map.of(
                "cookie1",
                new Cookie(Arrays.asList("cookie1_value1", "cookie1_value2")),
                "cookie2",
                new Cookie(Arrays.asList("cookie2_value1", "cookie2_value2"))));

    assertThat(cookies.summary())
        .isEqualTo(
            "cookie1: [cookie1_value1, cookie1_value2]\n"
                + "cookie2: [cookie2_value1, cookie2_value2]");
  }
}
