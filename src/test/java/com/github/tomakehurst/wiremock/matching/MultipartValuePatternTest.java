/*
 * Copyright (C) 2017 Arjan Duijzer
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
package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.common.Json;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MultipartValuePatternTest {

    @Test
    public void testAnyMatchWithName() {
        String serializedPattern = "{\n" +
                "        \"matchingType\": \"ANY\"" +
                "    }";

        MultipartValuePattern pattern = Json.read(serializedPattern, MultipartValuePattern.class);
        assertTrue(pattern.isMatchAny());
        assertFalse(pattern.isMatchAll());
    }

    @Test
    public void testAllMatchWithName() {
        String serializedPattern = "{\n" +
                "        \"matchingType\": \"ALL\",\n" +
                "            \"multipartHeaders\": {\n" +
                "               \"Content-Disposition\": [\n" +
                "                  {\n" +
                "                     \"contains\": \"name=\\\"part1\\\"\"\n" +
                "                  }\n" +
                "               ]\n" +
                "            }" +
                "         }";

        MultipartValuePattern pattern = Json.read(serializedPattern, MultipartValuePattern.class);
        Map<String, List<MultiValuePattern>> headerPatterns = newLinkedHashMap();
        headerPatterns.put("Content-Disposition", asList(MultiValuePattern.of(containing("name=\"part1\""))));
        assertThat(headerPatterns.entrySet(), everyItem(isIn(pattern.getMultipartHeaders().entrySet())));

        assertNull(pattern.getBodyPatterns());

        assertTrue(pattern.isMatchAll());
        assertFalse(pattern.isMatchAny());
    }

    @Test
    public void testAnyMatchWithBody() throws JSONException {
        String expectedJson = "{ \"someKey\": \"someValue\" }";
        String serializedPattern = "{\n" +
                "        \"matchingType\": \"ANY\",\n" +
                "        \"bodyPatterns\": [\n" +
                "        { \"equalToJson\": " + expectedJson + " }\n" +
                "      ]\n" +
                "    }";

        MultipartValuePattern pattern = Json.read(serializedPattern, MultipartValuePattern.class);

        JSONAssert.assertEquals(pattern.getBodyPatterns().get(0).getExpected(), expectedJson, false);

        assertNull(pattern.getMultipartHeaders());
        assertTrue(pattern.isMatchAny());
        assertFalse(pattern.isMatchAll());
    }

    @Test
    public void testAnyMatchWithHeadersAndJsonBody() throws JSONException {
        String expectedJson = "{ \"someKey\": \"someValue\" }";
        String serializedPattern = "{\n" +
                "        \"matchingType\": \"ANY\",\n" +
                "            \"multipartHeaders\": {\n" +
                "        \"Content-Disposition\": [\n" +
                "        {\n" +
                "            \"contains\": \"name=\\\"part1\\\"\"\n" +
                "        }\n" +
                "        ],\n" +
                "        \"Content-Type\": [\n" +
                "        {\n" +
                "            \"contains\": \"application/json\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"contains\": \"charset=utf-8\"\n" +
                "        }\n" +
                "        ]\n" +
                "    },\n" +
                "        \"bodyPatterns\": [\n" +
                "        {\n" +
                "            \"equalToJson\": " + expectedJson + "\n" +
                "        }\n" +
                "      ]\n" +
                "    }";

        MultipartValuePattern pattern = Json.read(serializedPattern, MultipartValuePattern.class);

        JSONAssert.assertEquals(pattern.getBodyPatterns().get(0).getExpected(), expectedJson, false);

        List<MultiValuePattern> contentTypeHeaderPattern = pattern.getMultipartHeaders().get("Content-Type");
        List<MultiValuePattern> expectedContentTypeHeaders = asList(MultiValuePattern.of(containing("application/json")), MultiValuePattern.of(containing("charset=utf-8")));
        assertThat(contentTypeHeaderPattern, everyItem(isIn(expectedContentTypeHeaders)));

        assertTrue(pattern.isMatchAny());
        assertFalse(pattern.isMatchAll());
    }

    @Test
    public void testAnyMatchWithHeadersAndBinaryBody() {
        String expectedBinary = "RG9jdW1lbnQgYm9keSBjb250ZW50cw==";
        String serializedPattern = "{\n" +
                "        \"matchingType\": \"ALL\",\n" +
                "            \"multipartHeaders\": {\n" +
                "        \"Content-Disposition\": [\n" +
                "        {\n" +
                "            \"contains\": \"name=\\\"file\\\"\"\n" +
                "        }\n" +
                "        ],\n" +
                "        \"Content-Type\": [\n" +
                "        {\n" +
                "            \"equalTo\": \"application/octet-stream\"\n" +
                "        }\n" +
                "        ]\n" +
                "    },\n" +
                "        \"bodyPatterns\": [\n" +
                "        {\n" +
                "            \"binaryEqualTo\": \"" + expectedBinary + "\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }";

        MultipartValuePattern pattern = Json.read(serializedPattern, MultipartValuePattern.class);

        assertEquals(pattern.getBodyPatterns().get(0).getExpected(), expectedBinary);

        List<MultiValuePattern> contentTypeHeaderPattern = pattern.getMultipartHeaders().get("Content-Type");
        List<MultiValuePattern> expectedContentTypeHeaders = asList(MultiValuePattern.of(equalTo("application/octet-stream")));
        assertThat(contentTypeHeaderPattern, everyItem(isIn(expectedContentTypeHeaders)));

        assertTrue(pattern.isMatchAll());
        assertFalse(pattern.isMatchAny());
    }
}
