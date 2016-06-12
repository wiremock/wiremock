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
                "GET\n",
                "POST\n")
        ));
    }

    @Test
    public void showsDiffForUrlEqualToWhereRequestPatternIsExpected() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/expected")).build(),
            mockRequest().url("/actual")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage("/expected\n", "/actual\n")
        ));
    }

    @Test
    public void showsDiffForUrlEqualToWhereRequestIsExpected() {
        Diff diff = new Diff(
            mockRequest().url("/expected"),
            newRequestPattern(ANY, urlEqualTo("/actual")).build()
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage("/expected\n", "/actual\n")
        ));
    }

    @Test
    public void showsNoDiffForUrlEqualToWhenUrlsAreTheSame() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/expected")).build(),
            mockRequest().url("/expected")
        );

        assertThat(diff.toString(), is(""));
    }

    @Test
    public void showsDiffForUrlPathMatchingWhereRequestPatternIsExpected() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlPathMatching("/expected/.*")).build(),
            mockRequest().url("/actual")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage("/expected/.*\n", "/actual\n")
        ));
    }

    @Test
    public void showsDiffsForsingleNonMatchingHeader() {
        Diff diff = new Diff(
            mockRequest().url("/thing")
                .header("Content-Type", "application/json")
                .header("X-My-Header", "expected"),
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("X-My-Header", equalTo("actual"))
                .build()
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "X-My-Header: expected\n",
                "X-My-Header: actual\n")
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
                "X-My-Header: expected\n",
                "\n")
        ));
    }

    @Test
    public void showsNoDiffWhenHeaderIsPresentInRequestAndExpectedAbsentInRequestPattern() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing")).build(),
            mockRequest().url("/thing").header("X-My-Header", "not-expected")
        );

        assertThat(diff.toString(), is(""));
    }

    @Test
    public void showsDiffWhenHeaderIsExpectedPresentInRequestPatternAndAbsentFromRequest() {
        Diff diff = new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withHeader("X-My-Header", equalTo("expected"))
                .build(),
            mockRequest().url("/thing")
        );

        assertThat(diff.toString(), is(
            junitStyleDiffMessage(
                "X-My-Header: expected\n",
                "\n")
        ));
    }

    @Test
    public void showsNoDiffWhenHeaderIsExpectedPresentInRequestAndAbsentFromRequestPattern() {
        Diff diff = new Diff(
            mockRequest().url("/thing").header("X-My-Header", "not-expected"),
            newRequestPattern(ANY, urlEqualTo("/thing")).build()
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
            )
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
            )
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
            )
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
                "<my-elements>\n" +
                "  <one attr-one=\"1111\"/>\n" +
                "  <two/>\n" +
                "  <three/>\n" +
                "</my-elements>\n",

                "<my-elements>\n" +
                "  <one attr-one=\"2222\"/>\n" +
                "  <two/>\n" +
                "  <three/>\n" +
                "</my-elements>\n")
        ));
    }

    @Ignore
    @Test
    public void printIt() {
        Diff diff = new Diff(
            newRequestPattern(GET, urlPathMatching("/expected/.*"))
                .withHeader("X-One", equalTo("one-expected"))
                .withHeader("X-Two", matching(".*-expected"))
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
                .method(POST)
                .url("/actual")
                .header("X-One", "one-actual")
                .header("X-Two", "two-actual")
                .body(
                    "{\n" +
                    "    \"outer\": {\n" +
                    "        \"inner:\": {\n" +
                    "            \"thing\": 1\n" +
                    "        }\n" +
                    "    }\n" +
                    "}"
            )
        );

        throw new VerificationException(diff.toString());
    }

    @Ignore
    @Test
    public void printIt2() {
        Diff diff = new Diff(
            newRequestPattern(GET, urlPathMatching("/expected/.*"))
                .withHeader("X-One", equalTo("one-expected"))
                .withHeader("X-Two", matching(".*-expected"))
                .withRequestBody(equalToXml("<my-elements><one attr-one=\"1111\" /><two /><three><three-inner/></three></my-elements>"))
                .build(),
            mockRequest()
                .method(POST)
                .url("/actual")
                .header("X-One", "one-actual")
                .header("X-Two", "two-actual")
                .body("<my-elements><one attr-one=\"555555\" /><two /><three /></my-elements>")
        );

        throw new VerificationException(diff.toString());
    }
}
