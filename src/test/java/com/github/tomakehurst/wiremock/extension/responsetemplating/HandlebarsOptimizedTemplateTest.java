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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.jknack.handlebars.Handlebars;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class HandlebarsOptimizedTemplateTest {

  private final Handlebars handlebars = new Handlebars();

  @Test
  public void treatsReversedDelimitersAsPlainContentWithoutThrowing() {
    String content = "}}x{{";

    HandlebarsOptimizedTemplate template = new HandlebarsOptimizedTemplate(handlebars, content);

    assertThat(template.hasTemplateExpressions(), is(false));
    assertThat(template.apply(Collections.emptyMap()), is(content));
  }

  @Test
  public void treatsAdjacentReversedDelimitersAsPlainContent() {
    String content = "}}{{";

    HandlebarsOptimizedTemplate template = new HandlebarsOptimizedTemplate(handlebars, content);

    assertThat(template.hasTemplateExpressions(), is(false));
    assertThat(template.apply(Collections.emptyMap()), is(content));
  }

  @Test
  public void returnsContentWithoutDelimitersUnchanged() {
    String content = "no expressions here";

    HandlebarsOptimizedTemplate template = new HandlebarsOptimizedTemplate(handlebars, content);

    assertThat(template.hasTemplateExpressions(), is(false));
    assertThat(template.apply(Collections.emptyMap()), is(content));
  }

  @Test
  public void stillRendersValidTemplateExpressions() {
    HandlebarsOptimizedTemplate template =
        new HandlebarsOptimizedTemplate(handlebars, "Hello {{name}}!");

    assertThat(template.hasTemplateExpressions(), is(true));
    assertThat(template.apply(Map.of("name", "World")), is("Hello World!"));
  }
}
