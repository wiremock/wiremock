/*
 * Copyright (C) 2011 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcher;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.http.RequestMethod.*;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.verification.diff.JUnitStyleDiffRenderer.junitStyleDiffMessage;
import static java.lang.System.lineSeparator;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DiffTest {

    @Test
    public void correctlyRendersJUnitStyleDiffMessage() {
        String diff = junitStyleDiffMessage("expected", "actual");

        assertThat(diff, is(" expected:<\nexpected> but was:<\nactual>"));
    }

    @Test
    public void showsDiffForNonMatchingRequestMethod() {
        Diff diff = new Diff(
            newRequestPattern(GET, urlEqualTo("/thing"))
                .build(),
            mockRequest()
                .method(POST)
                .url("/thing")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "GET\n" +
                "/thing\n",
                "POST\n" +
                "/thing\n")
        ));
    }

    @Test
    public void showsDiffForUrlEqualTo() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/expected")).build(),
            mockRequest().url("/actual")
            );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/expected\n",

                "ANY\n" +
                "/actual\n")
        ));
    }

    @Test
    public void showsDiffForUrlPathMatching() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlPathMatching("/expected/.*")).build(),
            mockRequest().url("/actual")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/expected/.*\n",

                "ANY\n" +
                "/actual\n")
        ));
    }

    @Test
    public void showsDiffsForSingleNonMatchingHeaderAndMatchingHeader() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-My-Header", equalTo("expected"))
            .build(),
            mockRequest().url("/thing")
                .header("Content-Type", "application/json")
                .header("X-My-Header", "actual")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "\n" +
                "Content-Type: application/json\n" +
                "X-My-Header: expected\n",

                "ANY\n" +
                "/thing\n" +
                "\n" +
                "Content-Type: application/json\n" +
                "X-My-Header: actual\n"
            )
        ));
    }

    @Test
    public void showsDiffWhenRequestHeaderIsAbsent() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withHeader("X-My-Header", equalTo("expected"))
                .build(),
            mockRequest().url("/thing")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "\n" +
                "X-My-Header: expected\n",

                "ANY\n" +
                "/thing\n" +
                "\n" +
                "\n")
        ));
    }

    @Test
    public void showsHeaders() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withHeader("X-My-Header", equalTo("expected"))
                .build(),
            mockRequest().url("/thing")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "\n" +
                "X-My-Header: expected\n",

                "ANY\n" +
                "/thing\n\n\n")
        ));
    }

    @Test
    public void showsRequestBody() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withRequestBody(equalToJson(
                    "{\n" +
                    "    \"outer\": {\n" +
                    "        \"inner\": {\n" +
                    "            \"thing\": 1\n" +
                    "        }\n" +
                    "    }\n" +
                    "}"))
                .build(),
            mockRequest().url("/thing").body(
                    "{\n" +
                    "    \"outer\": {}\n" +
                    "}"
            )
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "\n" +
                "{" + lineSeparator() +
                "  \"outer\" : {" + lineSeparator() +
                "    \"inner\" : {" + lineSeparator() +
                "      \"thing\" : 1" + lineSeparator() +
                "    }" + lineSeparator() +
                "  }" + lineSeparator() +
                "}",

                "ANY\n" +
                "/thing\n" +
                "\n" +
                "{" + lineSeparator() +
                "  \"outer\" : { }" + lineSeparator() +
                "}")
        ));
    }

    @Test
    public void prettyPrintsJsonRequestBody() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withRequestBody(equalToJson(
                    "{\"outer\": {\"inner:\": {\"thing\": 1}}}"))
                .build(),
            mockRequest().url("/thing").body(
                "{\"outer\": {}}"
            )
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "\n" +
                "{" + lineSeparator() +
                "  \"outer\" : {" + lineSeparator() +
                "    \"inner:\" : {" + lineSeparator() +
                "      \"thing\" : 1" + lineSeparator() +
                "    }" + lineSeparator() +
                "  }" + lineSeparator() +
                "}",

                "ANY\n" +
                "/thing\n" +
                "\n" +
                "{" + lineSeparator() +
                "  \"outer\" : { }" + lineSeparator() +
                "}")
        ));
    }

    @Test
    public void showsJsonPathExpectations() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withRequestBody(matchingJsonPath("@.notfound"))
                .withRequestBody(matchingJsonPath("@.nothereeither"))
                .build(),
            mockRequest().url("/thing").body(
                "{\n" +
                "    \"outer\": {\n" +
                "        \"inner:\": {\n" +
                "            \"thing\": 1\n" +
                "        }\n" +
                "    }\n" +
                "}"
            )
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "\n" +
                "@.notfound\n" +
                "@.nothereeither",

                "ANY\n" +
                "/thing\n" +
                "\n" +
                "{\n" +
                "    \"outer\": {\n" +
                "        \"inner:\": {\n" +
                "            \"thing\": 1\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "{\n" +
                "    \"outer\": {\n" +
                "        \"inner:\": {\n" +
                "            \"thing\": 1\n" +
                "        }\n" +
                "    }\n" +
                "}")
        ));
    }

    @Test
    public void prettyPrintsXml() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withRequestBody(equalToXml(
                    "<my-elements><one attr-one=\"1111\" /><two /><three /></my-elements>"))
                .build(),
            mockRequest().url("/thing").body(
                "<my-elements><one attr-one=\"2222\" /><two /><three /></my-elements>"
            )
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "\n" +
                "<my-elements>" + lineSeparator() +
                "  <one attr-one=\"1111\"/>" + lineSeparator() +
                "  <two/>" + lineSeparator() +
                "  <three/>" + lineSeparator() +
                "</my-elements>" + lineSeparator(),

                "ANY\n" +
                "/thing\n" +
                "\n" +
                "<my-elements>" + lineSeparator() +
                "  <one attr-one=\"2222\"/>" + lineSeparator() +
                "  <two/>" + lineSeparator() +
                "  <three/>" + lineSeparator() +
                "</my-elements>" + lineSeparator())
        ));
    }

    @Test
    public void showsCookiesInDiffWhenNotMatching() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withCookie("my_cookie", equalTo("expected-cookie"))
                .build(),
            mockRequest().url("/thing")
                .cookie("my_cookie", "actual-cookie")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "\n" +
                "Cookie: my_cookie=expected-cookie\n",

                "ANY\n" +
                "/thing\n" +
                "\n" +
                "actual-cookie\n"
            )
        ));
    }

    @Test
    public void showsQueryParametersInDiffWhenNotMatching() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlPathEqualTo("/thing"))
                .withQueryParam("search", equalTo("everything"))
                .build(),
            mockRequest().url("/thing?search=nothing")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                    "/thing?search=nothing\n" +
                    "\n" +
                    "Query: search = everything\n",

                "ANY\n" +
                    "/thing?search=nothing\n" +
                    "\n" +
                    "search: nothing\n"
            )
        ));
    }

    @Test
    public void showsCookiesInDiffAbsentFromRequest() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withCookie("my_cookie", equalTo("expected-cookie"))
                .build(),
            mockRequest().url("/thing")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "ANY\n" +
                "/thing\n" +
                "\n" +
                "Cookie: my_cookie=expected-cookie\n",

                "ANY\n" +
                "/thing\n\n\n"
            )
        ));
    }

    @Test
    public void showsAGenericMessageWhenTheRequestMatcherIsCustom() {
        Diff diff = new Diff(
            RequestPatternBuilder.forCustomMatcher(new RequestMatcher() {
                @Override
                public MatchResult match(Request request) {
                    return MatchResult.of(request.containsHeader("My-Header"));
                }

                @Override
                public String getName() {
                    return "custom-matcher";
                }
            }).build(),
            mockRequest().url("/thing")
        );

        assertThat(diff.toString(), is("(Request pattern had a custom matcher so no diff can be shown)"));
    }
}
