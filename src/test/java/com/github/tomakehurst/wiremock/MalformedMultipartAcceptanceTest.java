/*
 * Copyright (C) 2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.http.client.apache5.ApacheHttpClientFactory;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.List;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Test;

public class MalformedMultipartAcceptanceTest extends AcceptanceTestBase {

  CloseableHttpClient httpClient = ApacheHttpClientFactory.createClient();

  @Test
  public void servesAndRecordsARequestWhoseMultipartBodyCannotBeParsed() throws Exception {
    stubFor(post("/batch").willReturn(ok("batched")));

    // LF-only line endings, so the body does not parse as valid multipart content
    String body =
        "--batch_123\n"
            + "Content-Type: application/http\n"
            + "\n"
            + "GET https://example.com/things HTTP/1.1\n"
            + "\n"
            + "--batch_123--\n";

    ClassicHttpRequest request =
        ClassicRequestBuilder.post(wireMockServer.baseUrl() + "/batch")
            .setEntity(
                new StringEntity(
                    body, ContentType.parse("multipart/mixed; boundary=batch_123")))
            .build();

    ClassicHttpResponse response = httpClient.execute(request);

    assertThat(response.getCode(), is(200));
    assertThat(EntityUtils.toString(response.getEntity()), is("batched"));

    List<LoggedRequest> journal = findAll(postRequestedFor(urlEqualTo("/batch")));
    assertThat(journal.size(), is(1));
  }
}
