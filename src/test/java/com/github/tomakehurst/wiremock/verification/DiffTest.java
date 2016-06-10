package com.github.tomakehurst.wiremock.verification;

import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
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
                "Expected: is \"\n" +
                "expected" +
                "\"\n" +
                "     but: was \"\n" +
                "actual" +
                "\"\n\n"
        ));
    }

    @Test
    public void showsDiffForUrlEqualToWhereRequestPatternIsExpected() {
        Diff diff = new Diff(
            newRequestPattern().withUrl("/expected").build(),
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
            newRequestPattern().withUrl("/actual").build(),
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
            newRequestPattern().withUrl("/expected").build(),
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
    public void showsDiffsForNonMatchingHeaders() {
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
}
