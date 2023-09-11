/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.standalone;

import static com.github.tomakehurst.wiremock.common.AbstractFileSource.byFileExtension;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.JsonException;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.RequestBodyEqualToPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappingCollection;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteMappingsLoader {

  private final FileSource mappingsFileSource;
  private final FileSource filesFileSource;
  private final WireMock wireMock;

  public RemoteMappingsLoader(FileSource fileSource, WireMock wireMock) {
    this.mappingsFileSource = fileSource.child(MAPPINGS_ROOT);
    this.filesFileSource = fileSource.child(FILES_ROOT);
    this.wireMock = wireMock;
  }

  public void load() {
    List<TextFile> mappingFiles =
        mappingsFileSource.listFilesRecursively().stream()
            .filter(byFileExtension("json"))
            .collect(Collectors.toList());
    for (TextFile mappingFile : mappingFiles) {
      try {
        StubMappingCollection stubCollection =
            Json.read(mappingFile.readContentsAsString(), StubMappingCollection.class);
        for (StubMapping mapping : stubCollection.getMappingOrMappings()) {
          convertRequestBodyFromFileIfNecessary(mapping);
          convertResponseBodyFromFileIfNecessary(mapping);
          wireMock.register(mapping);
        }
      } catch (JsonException e) {
        throw new MappingFileException(mappingFile.getPath(), e.getErrors().first().getDetail());
      }
    }
  }

  private void convertRequestBodyFromFileIfNecessary(StubMapping mapping) {

    List<ContentPattern<?>> bodyPatterns = mapping.getRequest().getBodyPatterns();

    if (bodyPatterns == null) {
      return;
    }

    RequestBodyEqualToPattern originalPattern =
        bodyPatterns.stream()
            .filter(pattern -> pattern instanceof RequestBodyEqualToPattern)
            .map(RequestBodyEqualToPattern.class::cast)
            .filter(pattern -> pattern.getSource() == RequestBodyEqualToPattern.ExpectedSource.FILE)
            .findFirst()
            .orElse(null);

    if (originalPattern == null) {
      return;
    }

    String bodyFileName = originalPattern.getExpected();

    TextFile bodyFile = filesFileSource.getTextFileNamed(bodyFileName);
    RequestBodyEqualToPattern resultPattern =
        originalPattern
            .withExpected(bodyFile.readContentsAsString())
            .withSource(RequestBodyEqualToPattern.ExpectedSource.RAW);

    bodyPatterns.set(bodyPatterns.indexOf(originalPattern), resultPattern);
  }

  private void convertResponseBodyFromFileIfNecessary(StubMapping mapping) {
    String bodyFileName = mapping.getResponse().getBodyFileName();
    if (bodyFileName != null) {
      ResponseDefinitionBuilder responseDefinitionBuilder =
          ResponseDefinitionBuilder.like(mapping.getResponse()).withBodyFile(null);

      String extension = substringAfterLast(bodyFileName, ".");
      String mimeType = getMimeType(mapping);

      if (ContentTypes.determineIsText(extension, mimeType)) {
        TextFile bodyFile = filesFileSource.getTextFileNamed(bodyFileName);
        responseDefinitionBuilder.withBody(bodyFile.readContentsAsString());
      } else {
        BinaryFile bodyFile = filesFileSource.getBinaryFileNamed(bodyFileName);
        responseDefinitionBuilder.withBody(bodyFile.readContents());
      }

      mapping.setResponse(responseDefinitionBuilder.build());
    }
  }

  private String getMimeType(StubMapping mapping) {
    HttpHeaders responseHeaders = mapping.getResponse().getHeaders();
    if (responseHeaders != null) {
      ContentTypeHeader contentTypeHeader = responseHeaders.getContentTypeHeader();
      return contentTypeHeader != null ? contentTypeHeader.mimeTypePart() : null;
    }

    return null;
  }
}
