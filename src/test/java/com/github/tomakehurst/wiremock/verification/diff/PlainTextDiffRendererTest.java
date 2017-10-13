package com.github.tomakehurst.wiremock.verification.diff;

import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.file;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PlainTextDiffRendererTest {

    PlainTextDiffRenderer diffRenderer;

    @Before
    public void init() {
        diffRenderer = new PlainTextDiffRenderer();
    }

    @Test
    public void rendersWithDifferingUrlHeaderAndBodyAsAscii() {
        Diff diff = new Diff(post("/thing")
            .withName("Post the thing")
            .withHeader("X-My-Header", equalTo("correct value"))
            .withHeader("Accept", equalTo("text/plain"))
            .withRequestBody(equalToJson("{     \n" +
                "    \"thing\": {               \n" +
                "        \"stuff\": [1, 2, 3]   \n" +
                "    }                          \n" +
                "}")).build(),
            mockRequest()
                .method(POST)
                .url("/thin")
                .header("X-My-Header", "incorrect value")
                .header("Accept", "text/plain")
                .body("{                        \n" +
                    "    \"thing\": {           \n" +
                    "        \"nothing\": {}    \n" +
                    "    }                      \n" +
                    "}")
        );

        String output = diffRenderer.render(diff);

        assertThat(output, is(file("not-found-diff-sample_ascii.txt")));
    }
}
