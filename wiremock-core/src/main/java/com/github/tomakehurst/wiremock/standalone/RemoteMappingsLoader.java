/*
 * Copyright (C) 2016-2026 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.common.Strings.substringAfterLast;
import static com.github.tomakehurst.wiremock.common.entity.Format.BINARY;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.JsonException;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappingOrMappings;
import java.util.List;

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
        mappingsFileSource.listFilesRecursively().stream().filter(byFileExtension("json")).toList();
    for (TextFile mappingFile : mappingFiles) {
      try {
        StubMappingOrMappings stubCollection =
            Json.read(mappingFile.readContentsAsString(), StubMappingOrMappings.class);
        for (StubMapping mapping : stubCollection.getMappingOrMappings()) {
          wireMock.register(convertBodyFromFileIfNecessary(mapping));
        }
      } catch (JsonException e) {
        throw new MappingFileException(mappingFile.getPath(), e.getErrors().first().getDetail());
      }
    }
  }

  private StubMapping convertBodyFromFileIfNecessary(StubMapping mapping) {
    String bodyFileName = mapping.getResponse().getBodyFileName();
    if (bodyFileName != null) {
      var newResponseDefinition =
          mapping
              .getResponse()
              .transform(
                  responseDefinitionBuilder -> {
                    responseDefinitionBuilder.setBodyFileName(null);

                    String extension = substringAfterLast(bodyFileName, ".");
                    String mimeType = getMimeType(mapping);

                    if (ContentTypes.determineIsText(extension, mimeType)) {
                      TextFile bodyFile = filesFileSource.getTextFileNamed(bodyFileName);
                      responseDefinitionBuilder.setBody(bodyFile.readContentsAsString());
                    } else {
                      BinaryFile bodyFile = filesFileSource.getBinaryFileNamed(bodyFileName);
                      responseDefinitionBuilder.setBody(
                          responseDefinitionBuilder
                              .getBody()
                              .transform(
                                  b -> b.setFormat(BINARY).setData(bodyFile.readContents())));
                    }
                  });

      return mapping.transform(sm -> sm.setResponse(newResponseDefinition));
    }

    return mapping;
  }

  private String getMimeType(StubMapping mapping) {
    HttpHeaders responseHeaders = mapping.getResponse().getHeaders();
    ContentTypeHeader contentTypeHeader = responseHeaders.getContentTypeHeader();
    return contentTypeHeader != null ? contentTypeHeader.mimeTypePart() : null;
  }
}
