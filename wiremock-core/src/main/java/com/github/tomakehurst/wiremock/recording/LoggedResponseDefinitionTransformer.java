/*
 * Copyright (C) 2017-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ContentTypes.*;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.EntityMetadata;
import com.github.tomakehurst.wiremock.http.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transforms a LoggedResponse into a ResponseDefinition, which will be used to construct a
 * StubMapping
 */
public class LoggedResponseDefinitionTransformer
    implements Function<LoggedResponse, ResponseDefinition> {

  private static final List<CaseInsensitiveKey> EXCLUDED_HEADERS =
      List.of(
          CaseInsensitiveKey.from(CONTENT_ENCODING),
          CaseInsensitiveKey.from(CONTENT_LENGTH),
          CaseInsensitiveKey.from(TRANSFER_ENCODING));

  @Override
  public ResponseDefinition apply(LoggedResponse response) {
    final ResponseDefinitionBuilder responseDefinitionBuilder =
        new ResponseDefinitionBuilder().withStatus(response.getStatus());

    if (response.getBody() != null && response.getBody().length > 0) {
      final EntityDefinition.Builder bodyBuilder =
          EntityDefinition.builder().setData(response.getBody());
      EntityMetadata.copyFromHeaders(response.getHeaders(), bodyBuilder);
      final EntityDefinition body = bodyBuilder.build();
      responseDefinitionBuilder.withEntityBody(body.decompressIfPossible());
    }

    if (response.getHeaders() != null) {
      responseDefinitionBuilder.withHeaders(withoutContentEncodingAndContentLength(response));
    }

    return responseDefinitionBuilder.build();
  }

  private HttpHeaders withoutContentEncodingAndContentLength(LoggedResponse response) {
    return new HttpHeaders(
        response.getHeaders().all().stream()
            .filter(header -> !EXCLUDED_HEADERS.contains(header.caseInsensitiveKey()))
            .collect(Collectors.toList()));
  }
}
