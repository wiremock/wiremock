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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.github.jknack.handlebars.Options;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ValHelperTest extends HandlebarsHelperTestBase {

  ValHelper helper;

  @BeforeEach
  void init() {
    helper = new ValHelper();
  }

  @Test
  void returnsTheValuePassedInWhenNoOtherParameters() throws Exception {
    assertThat(renderHelperValue(helper, "some value"), is("some value"));

    List<Integer> list = List.of(1, 2, 3);
    assertThat(renderHelperValue(helper, list), is(list));
  }

  @Test
  void returnsTheDefaultValueWhenValueIsNull() throws Exception {
    assertThat(renderHelperValue(helper, null, Map.of("or", "other value")), is("other value"));
    assertThat(
        renderHelperValue(helper, null, Map.of("default", "other value")), is("other value"));
  }

  @Test
  void returnsTheValueWhenNotNullAndDefaultIsSpecified() throws Exception {
    assertThat(
        renderHelperValue(helper, "some value", Map.of("or", "other value")), is("some value"));
    assertThat(
        renderHelperValue(helper, "some value", Map.of("default", "other value")),
        is("some value"));
  }

  @Test
  void assignsToTheNamedVariable() throws Exception {
    Options options = createOptions(Map.of("assign", "myVar"));

    Object returnValue = helper.apply("some value", options);

    assertThat(options.context.data("myVar"), is("some value"));
    assertThat(returnValue, nullValue());
  }
}
