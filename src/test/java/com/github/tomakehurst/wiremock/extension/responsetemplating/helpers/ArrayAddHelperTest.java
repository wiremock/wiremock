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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArrayAddHelperTest extends HandlebarsHelperTestBase {

  ArrayAddHelper helper;

  @BeforeEach
  void init() {
    helper = new ArrayAddHelper();
  }

  @Test
  void appendsValueWhenNoPositionSpecified() throws Exception {
    List<String> originalList = List.of("one", "two");

    Object result = render(originalList, "three", emptyMap());

    assertThat(result, instanceOf(List.class));
    List<Object> resultingList = (List<Object>) result;
    assertThat(resultingList, contains("one", "two", "three"));
  }

  @Test
  void appendsValueWhenEndPositionSpecified() throws Exception {
    List<String> originalList = List.of("one", "two");

    Object result = render(originalList, "three", Map.of("position", "end"));

    assertThat(result, instanceOf(List.class));
    List<Object> resultingList = (List<Object>) result;
    assertThat(resultingList, contains("one", "two", "three"));
  }

  @Test
  void prependsValueWhenStartPositionSpecified() throws Exception {
    List<String> originalList = List.of("one", "two");

    Object result = render(originalList, "three", Map.of("position", "start"));

    assertThat(result, instanceOf(List.class));
    List<Object> resultingList = (List<Object>) result;
    assertThat(resultingList, contains("three", "one", "two"));
  }

  @Test
  void insertsValueAtPositionSpecifiedAsInteger() throws Exception {
    List<String> originalList = List.of("one", "two");

    Object result = render(originalList, "three", Map.of("position", 1));

    assertThat(result, instanceOf(List.class));
    List<Object> resultingList = (List<Object>) result;
    assertThat(resultingList, contains("one", "three", "two"));
  }

  @Test
  void insertsValueAtPositionSpecifiedAsString() throws Exception {
    List<String> originalList = List.of("one", "two");

    Object result = render(originalList, "three", Map.of("position", "1"));

    assertThat(result, instanceOf(List.class));
    List<Object> resultingList = (List<Object>) result;
    assertThat(resultingList, contains("one", "three", "two"));
  }

  @Test
  void returnsAnErrorWhenPostionNotValidType() throws IOException {
    Object result = render(emptyList(), "three", Map.of("position", "abc"));
    assertThat(result.toString(), is("[ERROR: position must be 'start', 'end' or an integer]"));
  }

  private Object render(List<?> originalList, Object value, Map<String, Object> keyValueOptions)
      throws IOException {
    final Options options =
        new Options.Builder(null, null, TagType.VAR, createContext(), Template.EMPTY)
            .setParams(new Object[] {value})
            .setHash(keyValueOptions)
            .build();
    return helper.apply(originalList, options);
  }
}
