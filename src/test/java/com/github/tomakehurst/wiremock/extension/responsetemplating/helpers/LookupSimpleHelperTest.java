/*
 * Copyright (C) 2026 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LookupSimpleHelperTest {

  private final TemplateEngine engine = TemplateEngine.defaultTemplateEngine();

  @Test
  void looksUpLiteralDottedKeysWithoutChangingRecursiveLookup() {
    Map<String, Object> data =
        Map.of("things.something", "literal", "things", Map.of("something", "nested"));

    assertThat(
        render(
            "{{lookupSimple data 'things.something'}}|{{lookup data 'things.something'}}",
            Map.of("data", data)),
        is("literal|nested"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"array[0]", "", "../name", "this", "@index"})
  void treatsDynamicKeysAsLiteralMapKeys(String key) {
    assertThat(
        render("{{lookupSimple data key}}", Map.of("data", Map.of(key, "value"), "key", key)),
        is("value"));
  }

  @Test
  void assignmentPreservesArrayObjectNumberAndBooleanValues() {
    Map<String, Object> data =
        Map.of(
            "values.list",
            List.of(1, 2),
            "value.object",
            Map.of("n", 3),
            "value.number",
            10,
            "value.boolean",
            false);
    String template =
        "{{lookupSimple data 'values.list' assign='items'}}"
            + "{{lookupSimple data 'value.object' assign='object'}}"
            + "{{lookupSimple data 'value.number' assign='number'}}"
            + "{{lookupSimple data 'value.boolean' assign='flag'}}"
            + "{{arrayJoin ',' items}}|{{object.n}}|{{{toJson number}}}|{{{toJson flag}}}";

    assertThat(render(template, Map.of("data", data)), is("1,2|3|10|false"));
  }

  @Test
  void rendersNullAndMissingValuesAsEmpty() {
    Map<String, Object> data = new HashMap<>();
    data.put("presentNull", null);
    String template =
        "{{lookupSimple data 'presentNull'}}|{{lookupSimple data 'missing'}}|"
            + "{{lookupSimple null 'key'}}|{{lookupSimple data null}}";

    assertThat(render(template, Map.of("data", data)), is("|||"));
  }

  @Test
  void assigningMissingValueClearsPreviousAssignment() {
    assertThat(
        render(
            "{{val 'old' assign='value'}}{{lookupSimple data 'missing' assign='value'}}[{{value}}]",
            Map.of("data", Map.of())),
        is("[]"));
  }

  @Test
  void retainsHandlebarsEscaping() {
    assertThat(
        render(
            "{{lookupSimple data 'html'}}|{{{lookupSimple data 'html'}}}",
            Map.of("data", Map.of("html", "<b>value & test</b>"))),
        is("&lt;b&gt;value &amp; test&lt;/b&gt;|<b>value & test</b>"));
  }

  @Test
  void reportsMissingKeyParameter() {
    assertThat(
        render("{{lookupSimple data}}", Map.of("data", Map.of())),
        is("[ERROR: A single lookup key must be supplied]"));
  }

  @Test
  void reportsNonStringKey() {
    assertThat(
        render("{{lookupSimple data 1}}", Map.of("data", Map.of())),
        is("[ERROR: Lookup key must be a string]"));
  }

  @Test
  void reportsNonMapInput() {
    assertThat(
        render("{{lookupSimple data 'key'}}", Map.of("data", List.of("value"))),
        is("[ERROR: Lookup input must be a map]"));
  }

  private String render(String template, Map<String, ?> model) {
    return engine.getUncachedTemplate(template).apply(model);
  }
}
