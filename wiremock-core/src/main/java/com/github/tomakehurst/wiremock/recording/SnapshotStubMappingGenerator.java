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

import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.Map;
import java.util.function.Function;
import org.wiremock.url.PathAndQuery;

/**
 * Transforms ServeEvents to StubMappings using RequestPatternTransformer and
 * LoggedResponseDefinitionTransformer
 */
class SnapshotStubMappingGenerator implements Function<ServeEvent, StubMapping> {
  private final RequestPatternTransformer requestTransformer;
  private final LoggedResponseDefinitionTransformer responseTransformer;
  private final boolean markStubsPersistent;

  SnapshotStubMappingGenerator(
      RequestPatternTransformer requestTransformer,
      LoggedResponseDefinitionTransformer responseTransformer,
      boolean markStubsPersistent) {
    this.requestTransformer = requestTransformer;
    this.responseTransformer = responseTransformer;
    this.markStubsPersistent = markStubsPersistent;
  }

  SnapshotStubMappingGenerator(
      Map<String, CaptureHeadersSpec> captureHeaders,
      boolean captureAllHeaders,
      RequestBodyPatternFactory requestBodyPatternFactory,
      boolean markStubsPersistent) {
    this(
        new RequestPatternTransformer(captureHeaders, captureAllHeaders, requestBodyPatternFactory),
        new LoggedResponseDefinitionTransformer(),
        markStubsPersistent);
  }

  @Override
  public StubMapping apply(ServeEvent event) {
    final RequestPattern requestPattern = requestTransformer.apply(event.getRequest()).build();
    final ResponseDefinition responseDefinition = responseTransformer.apply(event.getResponse());

    PathAndQuery url = event.getRequest().getPathAndQueryWithoutPrefix();
    FilenameMaker filenameMaker = new FilenameMaker();

    return StubMapping.builder()
        .setRequest(requestPattern)
        .setResponse(responseDefinition)
        .setPersistent(markStubsPersistent ? true : null)
        .setName(filenameMaker.sanitizeUrl(url.getPath()))
        .build();
  }
}
