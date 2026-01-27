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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class QueryParameterTemplatedMatcherAcceptanceTest extends AcceptanceTestBase {

  @Nested
  class EqualToQueryParameterMatcherAcceptanceTest {
    @Test
    void matchesQueryParameterEqualToAnotherQueryParameter() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", equalTo("{{request.query.param1}}").templated())
              .willReturn(ok()));

      // Should NOT match: param1=foo, but param2=bar (not equal)
      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(404));

      // Should match: param1=foo and param2=foo (equal)
      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(200));
    }

    @Test
    void doesNotMatchEqualToQueryParameterIfTemplatingNotEnabled() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", equalTo("{{request.query.param1}}"))
              .willReturn(ok()));

      // Should not match even though they are equal as templating not enabled
      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(404));
    }

    @Test
    void matchesEqualToQueryParameterUsingUpperHelper() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", equalTo("{{upper request.query.param1}}").templated())
              .willReturn(ok()));

      // param2 should match the uppercase version of param1
      assertThat(testClient.get("/test?param1=hello&param2=HELLO").statusCode(), is(200));
    }

    @Test
    void matchesEqualToQueryParameterFromJsonStubWithTemplating() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "equalTo": "{{request.query.param1}}",
                    "templated": true
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      // Parse the JSON into a StubMapping object
      StubMapping stubMapping = Json.read(json, StubMapping.class);

      // Add the stub mapping to WireMock using importStubs
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=hello&param2=hello").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=hello&param2=wrong").statusCode(), is(404));
    }

    @Test
    void doesNotMatchEqualToQueryParameterFromJsonStubWhenTemplatingNotEnabled() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "equalTo": "{{request.query.param1}}",
                    "templated": false
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      // Parse the JSON into a StubMapping object
      StubMapping stubMapping = Json.read(json, StubMapping.class);

      // Add the stub mapping to WireMock using importStubs
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=hello&param2=hello").statusCode(), is(404));
    }
  }

  @Nested
  class ContainsQueryParameterMatcherAcceptanceTest {
    @Test
    void matchesQueryParameterContainingAnotherQueryParameter() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", containing("{{request.query.param1}}").templated())
              .willReturn(ok()));

      // Should NOT match: param2 does not contain param1's value
      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(404));

      // Should match: param2 contains param1's value
      assertThat(testClient.get("/test?param1=foo&param2=foobar").statusCode(), is(200));
    }

    @Test
    void doesNotMatchContainsQueryParameterIfTemplatingNotEnabled() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", containing("{{request.query.param1}}"))
              .willReturn(ok()));

      // Should not match even though param2 contains param1's value, as templating not enabled
      assertThat(testClient.get("/test?param1=foo&param2=foobar").statusCode(), is(404));
    }

    @Test
    void matchesContainsQueryParameterUsingUpperHelper() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", containing("{{upper request.query.param1}}").templated())
              .willReturn(ok()));

      // param2 should contain the uppercase version of param1
      assertThat(testClient.get("/test?param1=hello&param2=xHELLOx").statusCode(), is(200));
    }

    @Test
    void matchesContainsQueryParameterFromJsonStubWithTemplating() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "contains": "{{request.query.param1}}",
                    "templated": true
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=hello&param2=helloworld").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=hello&param2=world").statusCode(), is(404));
    }

    @Test
    void doesNotMatchContainsQueryParameterFromJsonStubWhenTemplatingNotEnabled() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "contains": "{{request.query.param1}}",
                    "templated": false
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=hello&param2=helloworld").statusCode(), is(404));
    }
  }

  @Nested
  class DoesNotContainQueryParameterMatcherAcceptanceTest {
    @Test
    void matchesQueryParameterNotContainingAnotherQueryParameter() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", notContaining("{{request.query.param1}}").templated())
              .willReturn(ok()));

      // Should match: param2 does not contain param1's value
      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(200));

      // Should NOT match: param2 contains param1's value
      assertThat(testClient.get("/test?param1=foo&param2=foobar").statusCode(), is(404));
    }

    @Test
    void doesNotMatchDoesNotContainQueryParameterIfTemplatingNotEnabled() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", notContaining("{{request.query.param1}}"))
              .willReturn(ok()));

      // Without templating, the literal string "{{request.query.param1}}" is checked,
      // which param2 does not contain, so it matches
      assertThat(testClient.get("/test?param1=foo&param2=foobar").statusCode(), is(200));
    }

    @Test
    void matchesDoesNotContainQueryParameterFromJsonStubWithTemplating() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "doesNotContain": "{{request.query.param1}}",
                    "templated": true
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=hello&param2=world").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=hello&param2=helloworld").statusCode(), is(404));
    }

    @Test
    void doesNotMatchDoesNotContainQueryParameterFromJsonStubWhenTemplatingNotEnabled() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "doesNotContain": "{{request.query.param1}}",
                    "templated": false
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      // Without templating, checks literal "{{request.query.param1}}" which param2 doesn't contain
      assertThat(testClient.get("/test?param1=hello&param2=helloworld").statusCode(), is(200));
    }
  }

  @Nested
  class MatchesQueryParameterMatcherAcceptanceTest {
    @Test
    void matchesQueryParameterMatchingRegexFromAnotherQueryParameter() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", matching("{{request.query.param1}}").templated())
              .willReturn(ok()));

      // Should match: param1 provides regex "foo" which matches param2 "foo"
      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(200));

      // Should NOT match: param1 provides regex "foo" which doesn't match param2 "bar"
      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(404));
    }

    @Test
    void matchesQueryParameterMatchingRegexPatternFromAnotherQueryParameter() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", matching("{{request.query.param1}}.*").templated())
              .willReturn(ok()));

      // param1=foo, regex becomes "foo.*", param2=foobar matches
      assertThat(testClient.get("/test?param1=foo&param2=foobar").statusCode(), is(200));

      // param1=foo, regex becomes "foo.*", param2=barfoo doesn't match
      assertThat(testClient.get("/test?param1=foo&param2=barfoo").statusCode(), is(404));
    }

    @Test
    void doesNotMatchMatchesQueryParameterIfTemplatingNotEnabled() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", matching("{{request.query.param1}}"))
              .willReturn(ok()));

      // Without templating, the literal template string is treated as a regex, which is invalid.
      // Before template-aware regex matching, this would have thrown at construction time; now it
      // throws during matching, resulting in a 500 response.
      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(500));
    }

    @Test
    void matchesMatchesQueryParameterFromJsonStubWithTemplating() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "matches": "{{request.query.param1}}",
                    "templated": true
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=hello&param2=hello").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=hello&param2=wrong").statusCode(), is(404));
    }

    @Test
    void doesNotMatchMatchesQueryParameterFromJsonStubWhenTemplatingNotEnabled() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "matches": "{{request.query.param1}}",
                    "templated": false
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      // The literal "{{request.query.param1}}" is an invalid regex. This would previously have
      // thrown at construction time, but now throws during matching, resulting in a 500 response.
      assertThat(testClient.get("/test?param1=hello&param2=hello").statusCode(), is(500));
    }
  }

  @Nested
  class DoesNotMatchQueryParameterMatcherAcceptanceTest {
    @Test
    void matchesQueryParameterNotMatchingRegexFromAnotherQueryParameter() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", notMatching("{{request.query.param1}}").templated())
              .willReturn(ok()));

      // Should match: param1 provides regex "foo" which doesn't match param2 "bar"
      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(200));

      // Should NOT match: param1 provides regex "foo" which matches param2 "foo"
      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(404));
    }

    @Test
    void doesNotMatchDoesNotMatchQueryParameterIfTemplatingNotEnabled() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", notMatching("{{request.query.param1}}"))
              .willReturn(ok()));

      // Without templating, the literal template string is treated as a regex, which is invalid.
      // Before template-aware regex matching, this would have thrown at construction time; now it
      // throws during matching, resulting in a 500 response.
      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(500));
    }

    @Test
    void matchesDoesNotMatchQueryParameterFromJsonStubWithTemplating() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "doesNotMatch": "{{request.query.param1}}",
                    "templated": true
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=hello&param2=world").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=hello&param2=hello").statusCode(), is(404));
    }

    @Test
    void doesNotMatchDoesNotMatchQueryParameterFromJsonStubWhenTemplatingNotEnabled() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "doesNotMatch": "{{request.query.param1}}",
                    "templated": false
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      // The literal "{{request.query.param1}}" is an invalid regex. This would previously have
      // thrown at construction time, but now throws during matching, resulting in a 500 response.
      assertThat(testClient.get("/test?param1=hello&param2=hello").statusCode(), is(500));
    }
  }

  @Nested
  class NotQueryParameterMatcherAcceptanceTest {
    @Test
    void matchesNotEqualToQueryParameterWithTemplating() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", not(equalTo("{{request.query.param1}}").templated()))
              .willReturn(ok()));

      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(404));
    }

    @Test
    void matchesNotEqualToQueryParameterWhenTemplatingNotEnabled() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", not(equalTo("{{request.query.param1}}")))
              .willReturn(ok()));

      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(200));
    }

    @Test
    void matchesNotEqualToQueryParameterFromJsonStubWithTemplating() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "not": {
                      "equalTo": "{{request.query.param1}}",
                      "templated": true
                    }
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(404));
    }

    @Test
    void matchesNotEqualToQueryParameterFromJsonStubWhenTemplatingNotEnabled() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "not": {
                      "equalTo": "{{request.query.param1}}",
                      "templated": false
                    }
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(200));
    }
  }

  @Nested
  class AndQueryParameterMatcherAcceptanceTest {
    @Test
    void matchesAndQueryParameterWithTemplating() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam(
                  "param2",
                  and(
                      containing("{{request.query.param1}}").templated(),
                      equalTo("{{request.query.param1}}").templated()))
              .willReturn(ok()));

      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=foo&param2=foobar").statusCode(), is(404));
    }

    @Test
    void matchesAndQueryParameterFromJsonStubWithTemplating() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "and": [
                      {
                        "contains": "{{request.query.param1}}",
                        "templated": true
                      },
                      {
                        "equalTo": "{{request.query.param1}}",
                        "templated": true
                      }
                    ]
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=foo&param2=foobar").statusCode(), is(404));
    }
  }

  @Nested
  class OrQueryParameterMatcherAcceptanceTest {
    @Test
    void matchesOrQueryParameterWithTemplating() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam(
                  "param2",
                  or(
                      equalTo("{{request.query.param1}}").templated(),
                      equalTo("fallback").templated()))
              .willReturn(ok()));

      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=foo&param2=fallback").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(404));
    }

    @Test
    void matchesOrQueryParameterFromJsonStubWithTemplating() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "or": [
                      {
                        "equalTo": "{{request.query.param1}}",
                        "templated": true
                      },
                      {
                        "equalTo": "fallback",
                        "templated": true
                      }
                    ]
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=foo&param2=foo").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=foo&param2=fallback").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(404));
    }
  }

  @Nested
  class AbsentQueryParameterMatcherAcceptanceTest {
    @Test
    void matchesAbsentQueryParameter() {
      stubFor(get(urlPathEqualTo("/test")).withQueryParam("param2", absent()).willReturn(ok()));

      assertThat(testClient.get("/test?param1=foo").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(404));
    }

    @Test
    void matchesAbsentQueryParameterFromJsonStub() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "absent": true
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=foo").statusCode(), is(200));
      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(404));
    }
  }

  @Nested
  class AnythingQueryParameterMatcherAcceptanceTest {
    @Test
    void matchesAnythingQueryParameter() {
      stubFor(
          get(urlPathEqualTo("/test"))
              .withQueryParam("param2", new AnythingPattern())
              .willReturn(ok()));

      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(200));
    }

    @Test
    void matchesAnythingQueryParameterFromJsonStub() {
      String json =
          """
            {
              "request": {
                "urlPath": "/test",
                "method": "GET",
                "queryParameters": {
                  "param2": {
                    "anything": "anything"
                  }
                }
              },
              "response": {
                "status": 200
              }
            }
            """;

      StubMapping stubMapping = Json.read(json, StubMapping.class);
      WireMock.importStubs(StubImport.stubImport().stub(stubMapping).build());

      assertThat(testClient.get("/test?param1=foo&param2=bar").statusCode(), is(200));
    }
  }
}
