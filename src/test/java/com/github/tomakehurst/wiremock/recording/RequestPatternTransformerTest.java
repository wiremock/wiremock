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
package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.recording.CaptureHeadersSpec;
import com.github.tomakehurst.wiremock.recording.RequestPatternTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.junit.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static org.junit.Assert.assertEquals;

public class RequestPatternTransformerTest {
    @Test
    public void applyWithDefaultsAndNoBody() {
        Request request = mockRequest()
            .url("/foo")
            .method(RequestMethod.GET)
            .header("User-Agent", "foo")
            .header("X-Foo", "bar");
        RequestPatternBuilder expected = new RequestPatternBuilder(RequestMethod.GET, urlEqualTo("/foo"));

        // Default is to include method and URL exactly
        assertEquals(expected.build(), new RequestPatternTransformer(null, null).apply(request).build());
    }

    @Test
    public void applyWithUrlAndPlainTextBody() {
        Request request = mockRequest()
            .url("/foo")
            .method(RequestMethod.GET)
            .body("HELLO")
            .header("Accept", "foo")
            .header("User-Agent", "bar");

        RequestPatternBuilder expected = new RequestPatternBuilder(RequestMethod.GET, urlEqualTo("/foo"))
            .withRequestBody(equalTo("HELLO"));

        Map<String, CaptureHeadersSpec> headers = newLinkedHashMap();

        assertEquals(expected.build(), new RequestPatternTransformer(headers, null).apply(request).build());
    }

    @Test
    public void applyWithOnlyJsonBody() {
        Request request = mockRequest()
            .url("/somewhere")
            .header("Content-Type", "application/json")
            .body("['hello']");
        RequestPatternBuilder expected = new RequestPatternBuilder()
            .withUrl("/somewhere")
            .withRequestBody(equalToJson("['hello']"));

        assertEquals(expected.build(), new RequestPatternTransformer(null, null).apply(request).build());
    }

    @Test
    public void applyWithOnlyXmlBody() {
        Request request = mockRequest()
            .url("/somewhere")
            .header("Content-Type", "application/xml")
            .body("<foo/>");

        RequestPatternBuilder expected = new RequestPatternBuilder()
            .withUrl("/somewhere")
            .withRequestBody(equalToXml("<foo/>"));

        assertEquals(expected.build(), new RequestPatternTransformer(null, null).apply(request).build());
    }
}
