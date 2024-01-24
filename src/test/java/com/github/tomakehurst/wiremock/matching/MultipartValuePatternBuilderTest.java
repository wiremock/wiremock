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
package com.github.tomakehurst.wiremock.matching;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

public class MultipartValuePatternBuilderTest {

  @Test
  public void testBuilderDefaultType() {
    MultipartValuePattern pattern = aMultipart("name").build();
    assertTrue(pattern.isMatchAny());
    assertFalse(pattern.isMatchAll());
  }

  @Test
  public void testBuilderAnyType() {
    MultipartValuePattern pattern =
        aMultipart("name").matchingType(MultipartValuePattern.MatchingType.ANY).build();

    assertTrue(pattern.isMatchAny());
    assertFalse(pattern.isMatchAll());
  }

  @Test
  public void testBuilderAllType() {
    MultipartValuePattern pattern =
        aMultipart("name").matchingType(MultipartValuePattern.MatchingType.ALL).build();

    assertTrue(pattern.isMatchAll());
    assertFalse(pattern.isMatchAny());
  }

  @Test
  public void testBuilderWithNameHeadersAndBody() {
    MultipartValuePattern pattern =
        aMultipart("name")
            .withHeader("X-Header", containing("something"))
            .withHeader("X-Other", absent())
            .withBody(equalToXml("<xml />"))
            .build();

    List<ContentPattern<?>> bodyPatterns = List.of(equalToXml("<xml />"));
    assertThat(bodyPatterns, everyItem(is(in(pattern.getBodyPatterns()))));
  }

  @Test
  public void testBuilderWithNameNoHeadersAndNoBody() {
    MultipartValuePattern pattern = aMultipart().build();
    assertNull(pattern);
  }

  @Test
  public void testBuilderWithoutNameWithHeadersAndBody() {
    MultipartValuePattern pattern =
        aMultipart()
            .withHeader("X-Header", containing("something"))
            .withHeader("X-Other", absent())
            .withBody(equalToXml("<xml />"))
            .build();

    List<ContentPattern<?>> bodyPatterns = List.of(equalToXml("<xml />"));
    assertThat(bodyPatterns, everyItem(is(in(pattern.getBodyPatterns()))));
  }
}
