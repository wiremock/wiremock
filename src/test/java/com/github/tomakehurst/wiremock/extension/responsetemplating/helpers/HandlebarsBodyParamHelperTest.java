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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.NoFileSource.noFileSource;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class HandlebarsBodyParamHelperTest extends HandlebarsHelperTestBase {

    private HandlebarsBodyParamHelper helper;
    private ResponseTemplateTransformer transformer;

    @Before
    public void init() {
        helper = new HandlebarsBodyParamHelper();
        transformer = new ResponseTemplateTransformer(true);

        LocalNotifier.set(new ConsoleNotifier(true));
    }

    @Test
    public void paramMatched() {
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest()
                    .url("/kek").
                    body("TRTYPE=1&kek=kak"),
                aResponse()
                    .withBody("{{param request.body 'TRTYPE'}}").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), is("1"));
    }

    @Test
    public void paramNotMatched() {
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest()
                        .url("/kek").
                        body("TRTYPE=1&kek=kak"),
                aResponse()
                        .withBody("{{param request.body 'PRIVET'}}").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), is(""));
    }

    @Test
    public void emptyParam() {
        testHelperError(helper, "TRTYPE=1&kek=kak", null, is("[ERROR: The param name cannot be empty]"));
    }

    @Test
    public void wrongRequest() {
        testHelperError(helper, "TRTYPE=1=9&kek=kak", "{{param request.body 'TRTYPE'}}", is("[ERROR: Wrong format of key-value pair]"));
    }

}
