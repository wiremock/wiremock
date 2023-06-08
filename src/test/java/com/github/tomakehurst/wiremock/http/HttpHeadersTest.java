/*
 * Copyright (C) 2012-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.github.tomakehurst.wiremock.common.Json;
import org.junit.jupiter.api.Test;

class HttpHeadersTest {

  @Test
  void returnsAbsentHttpHeaderWhenHeaderNotPresent() {
    HttpHeaders httpHeaders = new HttpHeaders();
    HttpHeader header = httpHeaders.getHeader("Test-Header");

    assertThat(header.isPresent(), is(false));
  }

  @Test
  void returnsHeaderWhenPresent() {
    HttpHeaders httpHeaders = new HttpHeaders(httpHeader("Test-Header", "value1", "value2"));
    HttpHeader header = httpHeaders.getHeader("Test-Header");

    assertThat(header.isPresent(), is(true));
    assertThat(header.key(), is("Test-Header"));
    assertThat(header.containsValue("value2"), is(true));
  }

  @Test
  void createsCopy() {
    HttpHeaders httpHeaders =
        new HttpHeaders(
            httpHeader("Header-1", "h1v1", "h1v2"), httpHeader("Header-2", "h2v1", "h2v2"));

    HttpHeaders copyOfHeaders = HttpHeaders.copyOf(httpHeaders);

    assertThat(
        copyOfHeaders.all(),
        hasItems(
            header("Header-1", "h1v1"),
            header("Header-1", "h1v2"),
            header("Header-2", "h2v1"),
            header("Header-2", "h2v2")));
  }

  private static final String SINGLE_VALUE_HEADER =
      "{                	    	    		        \n"
          + "	\"Header-1\": \"only-value\"                \n"
          + "}                                               ";

  @Test
  void correctlyDeserializesWithSingleValueHeader() {
    HttpHeaders headers = Json.read(SINGLE_VALUE_HEADER, HttpHeaders.class);
    HttpHeader header = headers.getHeader("Header-1");

    assertThat(header.key(), is("Header-1"));
    assertThat(header.firstValue(), is("only-value"));
    assertThat(header.values().size(), is(1));
  }

  @Test
  void correctlySerializesSingleValueHeader() {
    HttpHeaders headers = new HttpHeaders(new HttpHeader("Header-1", "only-value"));

    String json = Json.write(headers);
    assertThat("Actual: " + json, json, equalToJson(SINGLE_VALUE_HEADER));
  }

  private static final String MULTI_VALUE_HEADER =
      "{    	                         	    		        \n"
          + "		    \"Header-1\": [                             \n"
          + "		        \"value-1\",                            \n"
          + "               \"value-2\"                             \n"
          + "           ],                                          \n"
          + "		    \"Header-2\": [                             \n"
          + "		        \"value-3\",                            \n"
          + "               \"value-4\"                             \n"
          + "           ]                                           \n"
          + "}                                                        ";

  @SuppressWarnings("unchecked")
  @Test
  void correctlyDeserializesWithMultiValueHeader() {
    HttpHeaders headers = Json.read(MULTI_VALUE_HEADER, HttpHeaders.class);

    HttpHeader header = headers.getHeader("Header-1");
    assertThat(header.key(), is("Header-1"));
    assertThat(header.values(), hasExactly(equalTo("value-1"), equalTo("value-2")));
    assertThat(header.values().size(), is(2));

    header = headers.getHeader("Header-2");
    assertThat(header.key(), is("Header-2"));
    assertThat(header.values(), hasExactly(equalTo("value-3"), equalTo("value-4")));
    assertThat(header.values().size(), is(2));

    assertThat(headers.size(), is(2));
  }

  @Test
  void correctlySerializesMultiValueHeader() {
    HttpHeaders headers =
        new HttpHeaders(
            new HttpHeader("Header-1", "value-1", "value-2"),
            new HttpHeader("Header-2", "value-3", "value-4"));

    String json = Json.write(headers);
    assertThat("Actual: " + json, json, equalToJson(MULTI_VALUE_HEADER));
  }

  @Test
  void keyMatchingIsCaseInsensitive() {
    HttpHeaders headers = new HttpHeaders(new HttpHeader("Header-One", "value 1"));

    assertThat(headers.getHeader("header-one").firstValue(), is("value 1"));
  }

  @Test
  void toStringWhenHeadersPresent() {
    HttpHeaders httpHeaders = new HttpHeaders(httpHeader("Test-Header", "value1", "value2"));
    assertThat(httpHeaders.toString().contains("Test-Header"), is(true));
  }

  @Test
  void toStringWhenHeadersEmpty() {
    HttpHeaders httpHeaders = new HttpHeaders();
    assertThat(httpHeaders.toString().equals("(no headers)\n"), is(true));
  }

  @Test
  void shouldEqualWhenIdentical() {
    HttpHeaders httpHeaders =
        new HttpHeaders(
            httpHeader("Header-1", "h1v1", "h1v2"), httpHeader("Header-2", "h2v1", "h2v2"));

    HttpHeaders copyOfHeaders = HttpHeaders.copyOf(httpHeaders);

    assertThat(httpHeaders.equals(copyOfHeaders), is(true));
    assertThat(httpHeaders.hashCode(), equalTo(copyOfHeaders.hashCode()));
  }
}
