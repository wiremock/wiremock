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
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.http.entity.StringEntity;
import org.junit.After;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.junit.Assert.*;

public class AdminRequestHandlerTest {

    WireMockServer wm;
    WireMockTestClient client;

    public void initWithOptions(Options options) {
        wm = new WireMockServer(options);
        wm.start();
        client = new WireMockTestClient(wm.port());
    }

    @After
    public void cleanup() {
        if (wm != null) {
            wm.stop();
        }
    }

    @Test
    public void getAdminRequestLogForAStubMappingPost() throws Exception {
        InMemoryNotifier notifier = new InMemoryNotifier();
        initWithOptions(options().dynamicPort().notifier(notifier));

        String postHeaderABCName = "ABC";
        String postHeaderABCValue = "abc123";
        String postBody =
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

        client.post("/__admin/mappings",
                new StringEntity(postBody),
                withHeader(postHeaderABCName, postHeaderABCValue));

        assertEquals(1, notifier.getLogCount());
        System.out.println(notifier.getInfoMessage());
        assertNotNull(notifier.getInfoMessage());
        assertTrue(notifier.getInfoMessage().contains("Admin request received:\n" +
                "127.0.0.1 - POST /mappings\n"));
        assertTrue(notifier.getInfoMessage().contains(postHeaderABCName + ": [" + postHeaderABCValue + "]\n"));
        assertTrue(notifier.getInfoMessage().contains(postBody));
    }

    private class InMemoryNotifier implements Notifier {
        private String infoMessage;
        private short logCount;

        @Override
        public void info(String message) {
            logCount++;
            this.infoMessage = message;
        }

        @Override
        public void error(String message) {
            logCount++;
        }

        @Override
        public void error(String message, Throwable t) {
            logCount++;
        }

        public String getInfoMessage() {
            return infoMessage;
        }

        public short getLogCount() {
            return logCount;
        }
    }
}
