/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

class SnapshotStubMappingBodyExtractor {
  private final BlobStore filesBlobStore;

  SnapshotStubMappingBodyExtractor(BlobStore filesBlobStore) {
    this.filesBlobStore = filesBlobStore;
  }

  /**
   * Extracts body of the ResponseDefinition to a file written to the files source. Modifies the
   * ResponseDefinition to point to the file in-place.
   *
   * @param stubMapping Stub mapping to extract
   */
  void extractInPlace(StubMapping stubMapping) {
    byte[] body = stubMapping.getResponse().getByteBody();
    HttpHeaders responseHeaders = stubMapping.getResponse().getHeaders();
    String extension =
        ContentTypes.determineFileExtension(
            getFirstNonNull(
                stubMapping.getRequest().getUrl(), stubMapping.getRequest().getUrlPath()),
            responseHeaders != null
                ? responseHeaders.getContentTypeHeader()
                : ContentTypeHeader.absent(),
            body);

    FilenameMaker filenameMaker = new FilenameMaker("default", extension);
    String bodyFileName = filenameMaker.filenameFor(stubMapping);

    // used to prevent ambiguous method call error for withBody()
    String noStringBody = null;
    byte[] noByteBody = null;

    stubMapping.setResponse(
        ResponseDefinitionBuilder.like(stubMapping.getResponse())
            .withBodyFile(bodyFileName)
            .withBody(noStringBody)
            .withBody(noByteBody)
            .withBase64Body(null)
            .build());

    filesBlobStore.put(bodyFileName, body);
  }
}
