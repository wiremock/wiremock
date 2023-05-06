/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ContentTypes.determineIsTextFromMimeType;
import static com.google.common.collect.Iterables.filter;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.BodyDecompressor;
import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.google.common.base.Function;
import java.nio.charset.Charset;

/**
 * Transforms a LoggedResponse into a ResponseDefinition, which will be used to construct a
 * StubMapping
 */
public class LoggedResponseDefinitionTransformer
    implements Function<LoggedResponse, ResponseDefinition> {

  @Override
  public ResponseDefinition apply(LoggedResponse response) {
    final ResponseDefinitionBuilder responseDefinitionBuilder =
        new ResponseDefinitionBuilder().withStatus(response.getStatus());

    BodyDecompressor decompressor = new BodyDecompressor(response.getBody(), response.getHeaders());
    if (response.getBody() != null && response.getBody().length > 0) {
      String mimeType = response.getMimeType();
      Charset charset = response.getCharset();
      if (determineIsTextFromMimeType(mimeType)) {
        responseDefinitionBuilder.withBody(
            Strings.stringFromBytes(decompressor.getBytes(), charset));
      } else {
        responseDefinitionBuilder.withBody(decompressor.getBytes());
      }
    }
    if (response.getHeaders() != null) {
      HttpHeaders headers =
          new HttpHeaders(filter(response.getHeaders().all(), decompressor::shouldRetain));
      responseDefinitionBuilder.withHeaders(headers);
    }

    return responseDefinitionBuilder.build();
  }
}
