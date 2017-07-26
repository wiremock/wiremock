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

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.*;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;

public class RequestBodyAutomaticPatternFactoryTest {
    private final static String JSON_TEST_STRING = "{ \"foo\": 1 }";
    private final static String XML_TEST_STRING = "<foo/>";

    @Test
    public void forRequestWithTextBodyIsCaseSensitiveByDefault() {
        Request request = mockRequest().body(JSON_TEST_STRING);
        EqualToPattern pattern = (EqualToPattern) patternForRequest(request);

        assertThat(pattern.getEqualTo(), is(JSON_TEST_STRING));
        assertThat(pattern.getCaseInsensitive(), is(false));
    }

    @Test
    public void forRequestWithTextBodyRespectsCaseInsensitiveOption() {
        Request request = mockRequest().body(JSON_TEST_STRING);
        RequestBodyAutomaticPatternFactory patternFactory = new RequestBodyAutomaticPatternFactory(false, false, true);
        EqualToPattern pattern = (EqualToPattern) patternFactory.forRequest(request);

        assertThat(pattern.getEqualTo(), is(JSON_TEST_STRING));
        assertThat(pattern.getCaseInsensitive(), is(true));
    }

    @Test
    public void forRequestWithJsonBodyIgnoresExtraElementsAndArrayOrderByDefault() {
        Request request = mockRequest()
            .header("Content-Type", "application/json")
            .body(JSON_TEST_STRING);
        EqualToJsonPattern pattern = (EqualToJsonPattern) patternForRequest(request);

        assertThat(pattern.getEqualToJson(), is(JSON_TEST_STRING));
        assertThat(pattern.isIgnoreExtraElements(), is(true));
        assertThat(pattern.isIgnoreArrayOrder(), is(true));
    }

    @Test
    public void forRequestWithJsonBodyRespectsOptions() {
        RequestBodyAutomaticPatternFactory patternFactory = new RequestBodyAutomaticPatternFactory(false, false, false);
        Request request = mockRequest()
            .header("Content-Type", "application/json")
            .body(JSON_TEST_STRING);
        EqualToJsonPattern pattern = (EqualToJsonPattern) patternFactory.forRequest(request);

        assertThat(pattern.getEqualToJson(), is(JSON_TEST_STRING));
        assertThat(pattern.isIgnoreExtraElements(), is(false));
        assertThat(pattern.isIgnoreArrayOrder(), is(false));
    }

    @Test
    public void forRequestWithXmlBody() {
        Request request = mockRequest()
            .header("Content-Type", "application/xml")
            .body(XML_TEST_STRING);
        EqualToXmlPattern pattern = (EqualToXmlPattern) patternForRequest(request);

        assertThat(pattern.getEqualToXml(), is(XML_TEST_STRING));
    }

    private static StringValuePattern patternForRequest(Request request) {
        return RequestBodyAutomaticPatternFactory.DEFAULTS.forRequest(request);
    }
}
