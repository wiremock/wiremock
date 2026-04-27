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
package com.github.tomakehurst.wiremock.testsupport;

import com.github.tomakehurst.wiremock.extension.RecorderServeEventTransformer;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

public class HeaderModifyingRecorderServeEventTransformer implements RecorderServeEventTransformer {

  @Override
  public ServeEvent transform(ServeEvent serveEvent) {
    return serveEvent
        .withRequest(
            serveEvent
                .getRequest()
                .transform(
                    builder ->
                        builder
                            .withBody("transformed request body")
                            .withHeaders(
                                replaceContentType(
                                    serveEvent.getRequest().getHeaders(), "text/plain"))))
        .withResponse(
            serveEvent
                .getResponse()
                .transform(
                    builder ->
                        builder
                            .withBody("transformed response body".getBytes())
                            .withHeaders(
                                replaceContentType(
                                    serveEvent.getResponse().getHeaders(), "text/plain"))));
  }

  private static HttpHeaders replaceContentType(HttpHeaders headers, String contentType) {
    return getFirstNonNull(headers, HttpHeaders.noHeaders()).transform(it -> it
                    .remove(ContentTypeHeader.KEY)
                    .add(ContentTypeHeader.KEY, contentType)
    );
  }

  @Override
  public String getName() {
    return "header-modifying-recorder-serve-event-transformer";
  }
}
