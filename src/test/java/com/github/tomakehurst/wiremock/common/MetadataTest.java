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

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
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
}
