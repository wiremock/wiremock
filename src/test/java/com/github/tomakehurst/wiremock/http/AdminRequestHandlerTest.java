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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.io.UnsupportedEncodingException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class AdminRequestHandlerTest {

  private Notifier notifier = mock(Notifier.class);

  @RegisterExtension
  private WireMockExtension wm =
      WireMockExtension.newInstance().options(options().dynamicPort().notifier(notifier)).build();

  @Test
  public void shouldLogInfoOnRequest() throws UnsupportedEncodingException {
    WireMockTestClient client = new WireMockTestClient(wm.getPort());

    String postHeaderABCName = "ABC";
    String postHeaderABCValue = "abc123";
    String postBody =
        "{\n"
            + "    \"request\": {\n"
            + "        \"method\": \"GET\",\n"
            + "        \"url\": \"/some/thing\"\n"
            + "    },\n"
            + "    \"response\": {\n"
            + "        \"status\": 200,\n"
            + "        \"body\": \"Hello world!\",\n"
            + "        \"headers\": {\n"
            + "            \"Content-Type\": \"text/plain\"\n"
            + "        }\n"
            + "    }\n"
            + "}";

    client.post(
        "/__admin/mappings",
        new StringEntity(postBody),
        withHeader(postHeaderABCName, postHeaderABCValue));

    verify(notifier).info(contains("Admin request received:\n127.0.0.1 - POST /mappings\n"));
    verify(notifier).info(contains(postHeaderABCName + ": [" + postHeaderABCValue + "]\n"));
    verify(notifier).info(contains(postBody));
  }
}
