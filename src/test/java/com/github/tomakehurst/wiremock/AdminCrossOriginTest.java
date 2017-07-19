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
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AdminCrossOriginTest extends AcceptanceTestBase {

    @Test
    public void sendsCorsHeadersInResponseToOPTIONSQuery() {
        WireMockResponse response = testClient.options("/__admin/",
            withHeader("Origin", "http://my.corp.com"),
            withHeader("Access-Control-Request-Method", "POST")
        );

        assertThat(response.statusCode(), is(200));
        assertThat(response.firstHeader("Access-Control-Allow-Origin"), is("http://my.corp.com"));
        assertThat(response.firstHeader("Access-Control-Allow-Methods"), is("OPTIONS,GET,POST,PUT,PATCH,DELETE"));
    }
}
