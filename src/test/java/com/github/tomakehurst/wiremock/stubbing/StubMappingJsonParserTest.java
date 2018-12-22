package com.github.tomakehurst.wiremock.stubbing;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StubMappingJsonParserTest {

    private static final String SINGLE = "{\n" +
            "  \"request\": {\n" +
            "    \"url\": \"/single/1\",\n" +
            "    \"method\": \"GET\"\n" +
            "  },\n" +
            "\n" +
            "  \"response\": {\n" +
            "    \"status\": 200\n" +
            "  }\n" +
            "}";

    private static final String MULTI = "{\n" +
            "  \"mappings\": [\n" +
            "    {\n" +
            "      \"request\": {\n" +
            "        \"url\": \"/multi/1\",\n" +
            "        \"method\": \"GET\"\n" +
            "      },\n" +
            "\n" +
            "      \"response\": {\n" +
            "        \"status\": 200\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    {\n" +
            "      \"request\": {\n" +
            "        \"url\": \"/multi/2\",\n" +
            "        \"method\": \"GET\"\n" +
            "      },\n" +
            "\n" +
            "      \"response\": {\n" +
            "        \"status\": 200\n" +
            "      }\n" +
            "    },\n" +
            "\n" +
            "    {\n" +
            "      \"request\": {\n" +
            "        \"url\": \"/multi/3\",\n" +
            "        \"method\": \"GET\"\n" +
            "      },\n" +
            "\n" +
            "      \"response\": {\n" +
            "        \"status\": 200\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    StubMappingJsonParser parser;

    @Before
    public void init() {
        parser = new StubMappingJsonParser();
    }

    @Test
    public void parsesSingleStubMappingJsonDocument() {
        List<StubMapping> stubs = parser.parse(SINGLE);

        assertThat(stubs.size(), is(1));
        assertThat(stubs.get(0).getRequest().getUrl(), is("/single/1"));
    }

    @Test
    public void parsesMultiStubMappingJsonDocument() {
        List<StubMapping> stubs = parser.parse(MULTI);

        assertThat(stubs.size(), is(3));
        assertThat(stubs.get(0).getRequest().getUrl(), is("/multi/1"));
        assertThat(stubs.get(1).getRequest().getUrl(), is("/multi/2"));
        assertThat(stubs.get(2).getRequest().getUrl(), is("/multi/3"));
    }
}
