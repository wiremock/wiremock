/*
 * Copyright (C) 2018-2021 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.function.Supplier;

import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminRequestHandlerTest {

  private Notifier notifier = mock(Notifier.class);

  @Captor
  private ArgumentCaptor<Supplier<String>> captor;

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

    verify(notifier, times(3)).info(captor.capture());
    List<Supplier<String>> capturedSuppliers = captor.getAllValues();
    String msg1 = capturedSuppliers.get(0).get();
    String msg2 = capturedSuppliers.get(1).get();
    String msg3 = capturedSuppliers.get(2).get();
    assertTrue(msg1.contains("Admin request received:\n127.0.0.1 - POST /mappings\n"));
    assertTrue(msg2.contains(postHeaderABCName + ": [" + postHeaderABCValue + "]\n"));
    assertTrue(msg3.contains(postBody));
  }
}
