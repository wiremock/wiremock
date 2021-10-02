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
package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AdminRequestHandlerTest {

    private WireMockServer wm;
    private WireMockTestClient client;

    @AfterEach
    public void cleanup() {
        if (wm != null) {
            wm.stop();
        }
    }

    @Test
    public void shouldLogInfoOnRequest() throws UnsupportedEncodingException {
        final Notifier notifier = mock(Notifier.class);
        wm = new WireMockServer(options().dynamicPort().notifier(notifier));
        wm.start();
        client = new WireMockTestClient(wm.port());

        final String postHeaderABCName = "ABC";
        final String postHeaderABCValue = "abc123";
        final String postBody =
                "{\n" +
                "    \"request\": {\n" +
                "        \"method\": \"GET\",\n" +
                "        \"url\": \"/some/thing\"\n" +
                "    },\n" +
                "    \"response\": {\n" +
                "        \"status\": 200,\n" +
                "        \"body\": \"Hello world!\",\n" +
                "        \"headers\": {\n" +
                "            \"Content-Type\": \"text/plain\"\n" +
                "        }\n" +
                "    }\n" +
                "}";


        client.post("/__admin/mappings", new StringEntity(postBody),
                withHeader(postHeaderABCName, postHeaderABCValue));

        verify(notifier).info(contains("Admin request received:\n127.0.0.1 - POST /mappings\n"));
        verify(notifier).info(contains(postHeaderABCName + ": [" + postHeaderABCValue + "]\n"));
        verify(notifier).info(contains(postBody));
    }

}
