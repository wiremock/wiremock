package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.client.VerificationException;
import org.junit.Ignore;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.verification.Diff.junitStyleDiffMessage;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DiffTest {

    @Test
    public void correctlyRendersJUnitStyleDiffMessage() {
        String diff = Diff.junitStyleDiffMessage("expected", "actual");

        assertThat(diff, is(
            "\n" +
                "Expected: " +
                "expected" +
                "\n" +
                "     but: was " +
                "actual" +
                "\n\n"
        ));
    }

    @Test
    public void showsDiffForNonMatchingRequestMethod() {
        Diff diff = new Diff(
            newRequestPattern(GET, urlEqualTo("/thing"))
                .build(),
            mockRequest()
                .method(POST)
                .url("/thing"),
            false
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "GET",
                "POST")
        ));
    }

    @Test
    public void showsDiffForUrlEqualToWhereRequestPatternIsExpected() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/expected")).build(),
            mockRequest().url("/actual"),
            false
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage("/expected", "/actual")
        ));
    }

    @Test
    public void showsDiffForUrlEqualToWhereRequestIsExpected() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/actual")).build(),
            mockRequest().url("/expected"),
            true
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage("/expected", "/actual")
        ));
    }

    @Test
    public void showsNoDiffForUrlEqualToWhenUrlsAreTheSame() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/expected")).build(),
            mockRequest().url("/expected"),
            false
        );

        assertThat(diff.toString(), is(""));
    }

    @Test
    public void showsDiffForUrlPathMatchingWhereRequestPatternIsExpected() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlPathMatching("/expected/.*")).build(),
            mockRequest().url("/actual"),
            false
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage("/expected/.*", "/actual")
        ));
    }

    @Test
    public void showsDiffsForsingleNonMatchingHeader() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("X-My-Header", equalTo("actual"))
                .build(),
            mockRequest().url("/thing")
                .header("Content-Type", "application/json")
                .header("X-My-Header", "expected"),
            true
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "X-My-Header: expected",
                "X-My-Header: actual")
        ));
    }

    @Test
    public void showsDiffWhenRequestHeaderIsAbsent() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withHeader("X-My-Header", equalTo("expected"))
                .build(),
            mockRequest().url("/thing"),
            false
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "X-My-Header: expected",
                "")
        ));
    }

    @Test
    public void showsNoDiffWhenHeaderIsPresentInRequestAndExpectedAbsentInRequestPattern() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing")).build(),
            mockRequest().url("/thing").header("X-My-Header", "not-expected"),
            false
        );

        assertThat(diff.toString(), is(""));
    }

    @Test
    public void showsDiffWhenHeaderIsExpectedPresentInRequestPatternAndAbsentFromRequest() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withHeader("X-My-Header", equalTo("expected"))
                .build(),
            mockRequest().url("/thing"),
            false
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "X-My-Header: expected",
                "")
        ));
    }

    @Test
    public void showsNoDiffWhenHeaderIsExpectedPresentInRequestAndAbsentFromRequestPattern() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing")).build(),
            mockRequest().url("/thing").header("X-My-Header", "not-expected"),
            true
        );

        assertThat(diff.toString(), is(""));
    }

    @Test
    public void showsDiffWhenRequestPatternJsonBodyDoesNotEqualRequestJsonBody() {
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
            ),
            false
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "{\n" +
                "  \"outer\" : {\n" +
                "    \"inner\" : {\n" +
                "      \"thing\" : 1\n" +
                "    }\n" +
                "  }\n" +
                "}",
                "{\n" +
                "  \"outer\" : { }\n" +
                "}")
        ));
    }

    @Test
    public void prettyPrintsJsonWhenJsonBodiesDiffer() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withRequestBody(equalToJson(
                    "{\"outer\": {\"inner:\": {\"thing\": 1}}}"))
                .build(),
            mockRequest().url("/thing").body(
                "{\"outer\": {}}"
            ),
            false
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "{\n" +
                "  \"outer\" : {\n" +
                "    \"inner:\" : {\n" +
                "      \"thing\" : 1\n" +
                "    }\n" +
                "  }\n" +
                "}",
                "{\n" +
                "  \"outer\" : { }\n" +
                "}")
        ));
    }

    @Test
    public void showsDiffWhenRequestJsonBodyDoesNotMatchMultipleJsonPaths() {
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
            ),
            false
        );


        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "@.notfound\n" +
                "@.nothereeither",
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

    @Ignore
    @Test
    public void printIt() {
        Diff diff = new Diff(
            newRequestPattern(GET, urlEqualTo("/expected"))
                .withHeader("X-One", equalTo("one-expected"))
                .withHeader("X-Two", equalTo("two-expected"))
                .withRequestBody(equalToJson(
                    "{\n" +
                    "    \"outer\": {\n" +
                    "        \"inner:\": {\n" +
                    "            \"wrong\": 1\n" +
                    "        }\n" +
                    "    }\n" +
                    "}"))
                .build(),
            mockRequest()
                .url("/actual")
                .header("X-One", "one-actual")
                .body(
                    "{\n" +
                    "    \"outer\": {\n" +
                    "        \"inner:\": {\n" +
                    "            \"thing\": 1\n" +
                    "        }\n" +
                    "    }\n" +
                    "}"
            ),
            false
        );

        throw new VerificationException(diff.toString());
    }
}
