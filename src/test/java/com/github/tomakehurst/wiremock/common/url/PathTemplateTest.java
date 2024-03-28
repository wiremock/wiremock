/*
 * Copyright (C) 2016-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common.url;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class PathTemplateTest {

  @Test
  public void extractsSinglePathParameter() {
    PathTemplate template = new PathTemplate("/things/{id}");

    PathParams pathParams = template.parse("/things/11-22-33");

    assertThat(pathParams.get("id"), is("11-22-33"));
  }

  @Test
  public void throwsIllegalArgumentExceptionIfAttemptingParsingOnNonMatchingUrl() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          PathTemplate template = new PathTemplate("/things/{id}");
          template.parse("/things/stuff/11-22-33");
        });
  }

  @Test
  public void matchesWhenUrlIsEquivalentToTemplate() {
    PathTemplate template = new PathTemplate("/things/{id}/otherthings/{subId}");

    assertThat(template.matches("/things/11-22-33/otherthings/12378"), is(true));
  }

  @Test
  public void nonMatchWhenUrlIsShorterThanTemplate() {
    PathTemplate template = new PathTemplate("/things/{id}/otherthings/{subId}");

    assertThat(template.matches("/things/11-22-33/otherthings"), is(false));
  }

  @Test
  public void nonMatchWhenUrlPartIsMismatch() {
    PathTemplate template = new PathTemplate("/things/{id}/otherthings/{subId}");

    assertThat(template.matches("/things/11-22-33/other-stuff/1234"), is(false));
  }

  @Test
  public void rendersWithParameters() {
    PathTemplate template = new PathTemplate("/things/{id}/otherthings/{subId}");
    PathParams pathParams = new PathParams().add("id", "123").add("subId", "456");

    String path = template.render(pathParams);

    assertThat(path, is("/things/123/otherthings/456"));
  }

  @Test
  public void rendersWithoutParameters() {
    PathTemplate template = new PathTemplate("/things/stuff");

    String path = template.render(PathParams.empty());

    assertThat(path, is("/things/stuff"));
  }

  @Test
  public void throwsErrorWhenNotAllParametersAreBound() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          PathTemplate template = new PathTemplate("/things/{id}/otherthings/{subId}");
          template.render(new PathParams().add("id", "123"));
        });
  }

  @Test
  public void parseWithWildcardAndOneDepthPath() {
    PathTemplate template = new PathTemplate("/things/**");

    PathParams pathParams = template.parse("/things/stuff");

    assertThat(pathParams.get("0"), is("stuff"));
  }

  @Test
  public void parseWithWildcardAndTwoDepthPath() {
    PathTemplate template = new PathTemplate("/things/**");

    PathParams pathParams = template.parse("/things/foo/bar");

    assertThat(pathParams.get("0"), is("foo/bar"));
  }

  @Test
  public void parseWithVariableAndWildcardAndTwoDepthPath() {
    PathTemplate template = new PathTemplate("/things/{id}/**");

    PathParams pathParams = template.parse("/things/foo/bar");

    assertThat(pathParams.get("id"), is("foo"));
    assertThat(pathParams.get("0"), is("bar"));
  }

  @Test
  public void renderWithWildcardAndOneDepth() {
    PathTemplate template = new PathTemplate("/things/**");
    PathParams pathParams = new PathParams().add("0", "stuff");

    String path = template.render(pathParams);

    assertThat(path, is("/things/stuff"));
  }

  @Test
  public void renderWithWildcardAndTwoDepth() {
    PathTemplate template = new PathTemplate("/things/**");
    PathParams pathParams = new PathParams().add("0", "foo/bar");

    String path = template.render(pathParams);

    assertThat(path, is("/things/foo/bar"));
  }

  @Test
  public void renderWithVariableAndWildcardAndTwoDepthPath() {
    PathTemplate template = new PathTemplate("/things/{id}/**");
    PathParams pathParams = new PathParams().add("id", "foo").add("0", "bar");

    String path = template.render(pathParams);

    assertThat(path, is("/things/foo/bar"));
  }

  @Test
  public void throwsErrorWhenNotWildcardParameterIsNotBound() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          PathTemplate template = new PathTemplate("/things/{id}/**");
          template.render(new PathParams().add("id", "123"));
        });
  }

  @Test
  public void checkHashAndEquality() {
    List<String> templates =
        asList(
            "/things",
            "/things/",
            "/things/**",
            "/things/{id}",
            "/things/{name}",
            "/things/**/",
            "/things/{id}/",
            "/things/{name}/");

    Set<PathTemplate> uriTemplateSet = new LinkedHashSet<>();
    for (String template : templates) {
      PathTemplate uriTemplate = new PathTemplate(template);
      if (!uriTemplateSet.add(uriTemplate)) {
        fail(format("Can't add '%s' to '%s'", template, uriTemplateSet));
      }
    }
  }

  @Test
  public void checkEquality() {
    List<String> templates =
        asList(
            "/things",
            "/things/",
            "/things/**",
            "/things/{id}",
            "/things/{name}",
            "/things/**/",
            "/things/{id}/",
            "/things/{name}/");

    List<PathTemplate> uriTemplates = new ArrayList<>();
    for (String template : templates) {
      PathTemplate uriTemplate = new PathTemplate(template);
      if (uriTemplates.contains(uriTemplate)) {
        fail(format("Can't add '%s' to '%s'", template, uriTemplates));
      }
      uriTemplates.add(uriTemplate);
    }
  }

  @Test
  void returnsPathTemplateWithVariablesStrippedOut() {
    PathTemplate pathTemplate = new PathTemplate("/one/{first}/two/{second}/three");
    assertThat(pathTemplate.withoutVariables(), is("/one/_/two/_/three"));
  }

  @Test
  void indicatesWhetherAStringCouldBeAPathTemplate() {
    assertTrue(PathTemplate.couldBePathTemplate("/things/{id}"));
    assertTrue(PathTemplate.couldBePathTemplate("/things/**"));
    assertTrue(PathTemplate.couldBePathTemplate("/things/{id}/stuff"));

    assertFalse(PathTemplate.couldBePathTemplate("/things/in/path"));
    assertFalse(PathTemplate.couldBePathTemplate("/thing"));
  }

  @Test
  void correctlyStripsFormatCharactersFromKeysWhenParsing() {
    PathTemplate pathTemplate = new PathTemplate("/one/{.first}/two/{;second*}");
    PathParams pathParams = pathTemplate.parse("/one/.3,4,5/two/;second=1;second=2");

    assertThat(pathParams.get("first"), is(".3,4,5"));
    assertThat(pathParams.get("second"), is(";second=1;second=2"));
  }

  @Test
  void correctlyStripsFormatCharactersFromKeysWhenRendering() {
    PathTemplate pathTemplate = new PathTemplate("/one/{.first}/two/{;second*}");

    PathParams pathParams =
        new PathParams().add("first", ".3,4,5").add("second", ";second=1;second=2");

    String renderedUrl = pathTemplate.render(pathParams);

    assertThat(renderedUrl, is("/one/.3,4,5/two/;second=1;second=2"));
  }

  @Test
  void ignoresQueryParameter() {
    PathTemplate pathTemplate = new PathTemplate("/things/{thingId}/stuff");
    assertTrue(pathTemplate.matches("/things/123/stuff?query=param"));
  }

  @ParameterizedTest()
  @CsvSource({
    "/things,0",
    "/things/{id},1",
    "/things/{id}/otherthings/{subId},2",
    "/things/stuff,0",
    "/things/**,1",
    "/things/**/,1",
    "/things/{id}/**,2",
    "/things/**/{id},2",
    "/one/{.first}/two/{;second*},2",
  })
  void exposesNumberOfParameters(String template, int expectedNumberOfParameters) {
    PathTemplate pathTemplate = new PathTemplate(template);
    assertThat(pathTemplate.numberOfParameters(), is(expectedNumberOfParameters));
  }
}
