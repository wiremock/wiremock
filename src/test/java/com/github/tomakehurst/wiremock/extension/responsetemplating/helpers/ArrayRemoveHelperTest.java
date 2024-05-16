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

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;

import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArrayRemoveHelperTest extends HandlebarsHelperTestBase {

  ArrayRemoveHelper helper;

  @BeforeEach
  void init() {
    helper = new ArrayRemoveHelper();
  }

  @Test
  void deletesTheLastValueWhenNoPositionSpecified() throws Exception {
    List<String> originalList = List.of("one", "two", "three");

    Object result = render(originalList, emptyMap());

    assertThat(result, instanceOf(List.class));
    List<Object> resultingList = (List<Object>) result;
    assertThat(resultingList, contains("one", "two"));
  }

  @Test
  void deletesTheValueAtTheIntegerPositionSpecified() throws Exception {
    List<String> originalList = List.of("one", "two", "three");

    Object result = render(originalList, Map.of("position", "1"));

    assertThat(result, instanceOf(List.class));
    List<Object> resultingList = (List<Object>) result;
    assertThat(resultingList, contains("one", "three"));
  }

  private Object render(List<?> originalList, Map<String, Object> keyValueOptions)
      throws IOException {
    final Options options =
        new Options.Builder(null, null, TagType.VAR, createContext(), Template.EMPTY)
            .setHash(keyValueOptions)
            .build();
    return helper.apply(originalList, options);
  }
}
