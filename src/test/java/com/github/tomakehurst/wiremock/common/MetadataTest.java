/*
 * Copyright (C) 2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class MetadataTest {

  @Test
  void deserialisesStubWithMetadata() {
    // language=json
    String json =
        """
                {
                    "request": {
                        "method": "GET",
                        "url": "/test",
                        "customMatcher": {
                            "parameters": {
                                "name": "test-params",
                                "key": "val"
                            }
                        }
                    },
                    "response": {
                        "status": 200
                    },
                    "metadata": {
                        "single": "value",
                        "obj": {
                            "a": "b"
                        }
                    }
                }
                """;

    StubMapping stub = Json.read(json, StubMapping.class);

    Metadata metadata = stub.getMetadata();
    assertThat(metadata.getString("single"), is("value"));
    assertThat(metadata.getMetadata("obj").getString("a"), is("b"));
  }

  @Test
  void deepMergesMetadata() {
    final Metadata initialMetadata =
        Metadata.create(
            builder ->
                builder
                    .attr("oneString", "abc")
                    .list("oneList", "a", "b", "c")
                    .list("oneListToBeReplaced", "1", "2")
                    .attr("oneInt", 1)
                    .attr("oneObjToBeReplaced", innerBuilder -> innerBuilder.attr("inner", "to go"))
                    .attr("oneObj", innerBuilder -> innerBuilder.attr("oneInner", 123)));

    Metadata toMerge =
        Metadata.create(
            builder ->
                builder
                    .attr("twoString", "def")
                    .list("oneList", "d", "e")
                    .attr("oneListToBeReplaced", false)
                    .attr("oneObjToBeReplaced", 0)
                    .attr("twoObj", innerBuilder -> innerBuilder.attr("twoInner", 456))
                    .attr("oneInt", 2)
                    .attr("oneObj", innerBuilder -> innerBuilder.attr("oneInner", 789)));

    Metadata merged = initialMetadata.deepMerge(toMerge);

    // Unchanged
    assertThat(merged.getString("oneString"), is("abc"));

    // Added
    assertThat(merged.getString("twoString"), is("def"));
    assertThat(merged.getMetadata("twoObj").getInt("twoInner"), is(456));

    // Overridden
    assertThat(merged.getInt("oneInt"), is(2));
    assertThat(merged.getMetadata("oneObj").getInt("oneInner"), is(789));

    // Replaced with different type
    assertThat(merged.get("oneListToBeReplaced"), is(false));
    assertThat(merged.getInt("oneObjToBeReplaced"), is(0));

    // Inserted
    assertThat(merged.getList("oneList"), is(List.of("a", "b", "c", "d", "e")));
  }

  @Test
  void initialisesMetadataRecursivelyFromMap() {
    var data =
        Map.of(
            "one",
            1,
            "two",
            Map.of(
                "a", "b",
                "c", "d"));

    var metadata = new Metadata(data);

    assertThat(metadata.getInt("one"), is(1));

    Metadata two = metadata.getMetadata("two");
    assertThat(two.getString("a"), is("b"));
  }

  @Test
  void initialisesParametersRecursivelyFromMap() {
    var data =
        Map.of(
            "one",
            1,
            "two",
            Map.of(
                "a", "b",
                "c", "d"));

    var parameters = new Parameters(data);

    assertThat(parameters.getInt("one"), is(1));

    Parameters two = parameters.getParameters("two");
    assertThat(two.getString("a"), is("b"));
  }

  @Test
  void initialisesParametersRecursivelyFromJson() {
    var json =
        // language=json
        """
        {
          "one": 1,
          "two": {
            "a": "b",
            "c": "d",
            "three": {
              "e": "f"
            }
          }
        }
        """;

    var parameters = Json.read(json, Parameters.class);

    assertThat(parameters.getInt("one"), is(1));

    Parameters two = parameters.getParameters("two");
    assertThat(two.getString("a"), is("b"));

    Parameters three = (Parameters) two.get("three");
    assertThat(three.getString("e"), is("f"));
  }
}
