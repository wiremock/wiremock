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
    public void rendersWithDifferingUrlHeaderAndJsonBody() {
        Diff diff = new Diff(post("/thing")
            .withName("The post stub with a really long name that ought to wrap and let us see exactly how that looks when it is done")
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
        System.out.printf(output);

        assertThat(output, is(file("not-found-diff-sample_ascii.txt")));
    }

    @Test
    public void wrapsLargeJsonBodiesAppropriately() {
        Diff diff = new Diff(post("/thing")
            .withName("The post stub with a really long name that ought to wrap and let us see exactly how that looks when it is done")
            .withHeader("Accept", equalTo("text/plain"))
            .withRequestBody(equalToJson("{\n" +
                "  \"one\": {\n" +
                "    \"two\": {\n" +
                "      \"three\": {\n" +
                "        \"four\": {\n" +
                "          \"five\": {\n" +
                "            \"six\": \"superduperlongvaluethatshouldwrapokregardless_superduperlongvaluethatshouldwrapokregardless_superduperlongvaluethatshouldwrapokregardless_superduperlongvaluethatshouldwrapokregardless\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}")).build(),
            mockRequest()
                .method(POST)
                .url("/thing")
                .header("Accept", "text/plain")
                .body("{\n" +
                    "  \"one\": {\n" +
                    "    \"two\": {\n" +
                    "      \"three\": {\n" +
                    "        \"four\": {\n" +
                    "          \"five\": {\n" +
                    "            \"six\": \"totally_the_wrong_value\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}")
        );

        String output = diffRenderer.render(diff);
        System.out.println(output);

        String expected = file("not-found-diff-sample_large_json.txt");
        assertThat(output, is(expected));
    }

    @Test
    public void wrapsLargeXmlBodiesAppropriately() {
        Diff diff = new Diff(post("/thing")
            .withName("The post stub with a really long name that ought to wrap and let us see exactly how that looks when it is done")
            .withRequestBody(equalToXml("<deep-things>\n" +
                "  <thing id=\"1\">\n" +
                "    <thing id=\"2\">\n" +
                "      <thing id=\"3\">\n" +
                "        <thing id=\"4\">\n" +
                "          <thing id=\"5\">\n" +
                "            <thing id=\"6\">\n" +
                "              Super wrong bit of text that should push it way over the length limit!\n" +
                "            </thing>\n" +
                "          </thing>\n" +
                "        </thing>\n" +
                "      </thing>\n" +
                "    </thing>\n" +
                "  </thing>\n" +
                "</deep-things>")).build(),
            mockRequest()
                .method(POST)
                .url("/thing")
                .body("<deep-things>\n" +
                    "  <thing id=\"1\">\n" +
                    "    <thing id=\"2\">\n" +
                    "      <thing id=\"3\">\n" +
                    "        <thing id=\"4\">\n" +
                    "          <thing id=\"5\">\n" +
                    "            <thing id=\"6\">\n" +
                    "              Super long bit of text that should push it way over the length limit!\n" +
                    "            </thing>\n" +
                    "          </thing>\n" +
                    "        </thing>\n" +
                    "      </thing>\n" +
                    "    </thing>\n" +
                    "  </thing>\n" +
                    "</deep-things>")
        );

        String output = diffRenderer.render(diff);
        System.out.println(output);

        assertThat(output, is(file("not-found-diff-sample_large_xml.txt")));
    }

    @Test
    public void showsMissingHeaderMessage() {
        Diff diff = new Diff(post("/thing")
            .withName("Missing header stub")
            .withHeader("X-My-Header", equalTo("correct value"))
            .build(),
            mockRequest()
                .method(POST)
                .url("/thing")
        );

        String output = diffRenderer.render(diff);
        System.out.printf(output);

        assertThat(output, is(file("not-found-diff-sample_missing_header.txt")));
    }

    @Test
    public void showsJsonPathMismatch() {
        Diff diff = new Diff(post("/thing")
            .withRequestBody(matchingJsonPath("$..six"))
            .withRequestBody(matchingJsonPath("$..seven"))
            .build(),
            mockRequest()
                .method(POST)
                .url("/thing")
                .body("{\n" +
                    "  \"one\": {\n" +
                    "    \"two\": {\n" +
                    "      \"three\": {\n" +
                    "        \"four\": {\n" +
                    "          \"five\": {\n" +
                    "            \"six\": \"match this\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}")
        );

        String output = diffRenderer.render(diff);
        System.out.println(output);

        String expected = file("not-found-diff-sample_json-path.txt");
        assertThat(output, is(expected));
    }
}
