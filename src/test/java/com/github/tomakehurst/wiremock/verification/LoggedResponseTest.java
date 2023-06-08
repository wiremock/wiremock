/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification;

import static com.github.tomakehurst.wiremock.common.Limit.UNLIMITED;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.http.*;
import org.junit.jupiter.api.Test;

public class LoggedResponseTest {
  private static String ISO_8859_1_RESPONSE_BODY = "köttfärssås";
  private static String UTF8_RESPONSE_BODY = "Foo © bar 𝌆 baz ☃ qux";

  @Test
  void returnsEmptyStringForBodyWhenNotConfigured() {
    LoggedResponse loggedResponse = LoggedResponse.from(Response.notConfigured(), UNLIMITED);
    assertEquals(loggedResponse.getBodyAsString(), "");
  }

  @Test
  void returnsEncodedStringForBodyWhenContentTypeHeaderGiven() {
    LoggedResponse loggedResponse =
        LoggedResponse.from(
            Response.response()
                .body(ISO_8859_1_RESPONSE_BODY)
                .headers(
                    new HttpHeaders(httpHeader("Content-Type", "text/plain; charset=iso-8859-1")))
                .build(),
            UNLIMITED);
    assertThat(ISO_8859_1_RESPONSE_BODY, is(equalTo(loggedResponse.getBodyAsString())));
  }

  @Test
  void returnsUtf8StringForBodyWhenContentTypeHeaderAbsent() {
    LoggedResponse loggedResponse =
        LoggedResponse.from(Response.response().body(UTF8_RESPONSE_BODY).build(), UNLIMITED);
    assertThat(UTF8_RESPONSE_BODY, is(equalTo(loggedResponse.getBodyAsString())));
  }
}
