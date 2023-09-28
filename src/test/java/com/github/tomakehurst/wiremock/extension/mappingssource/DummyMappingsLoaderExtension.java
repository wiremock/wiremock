/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.mappingssource;

import static com.github.tomakehurst.wiremock.common.AbstractFileSource.byFileExtension;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.JsonException;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.extension.MappingsLoaderExtension;
import com.github.tomakehurst.wiremock.standalone.MappingFileException;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappingCollection;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import java.util.*;
import java.util.stream.Collectors;

public class DummyMappingsLoaderExtension implements MappingsLoaderExtension {

  private final FileSource mappingsFileSource;
  private final Map<UUID, StubMappingFileMetadata> fileNameMap;
  private final FilenameMaker filenameMaker;

  public DummyMappingsLoaderExtension(FileSource mappingsFileSource, FilenameMaker filenameMaker) {
    this.mappingsFileSource = mappingsFileSource;
    this.filenameMaker = Objects.requireNonNullElseGet(filenameMaker, FilenameMaker::new);
    fileNameMap = new HashMap<>();
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public void loadMappingsInto(StubMappings stubMappings) {
    {
      if (!mappingsFileSource.exists()) {
        return;
      }

      List<TextFile> mappingFiles =
          mappingsFileSource.listFilesRecursively().stream()
              .filter(byFileExtension("json"))
              .collect(Collectors.toList());
      for (TextFile mappingFile : mappingFiles) {
        try {
          StubMappingCollection stubCollection =
              Json.read(mappingFile.readContentsAsString(), StubMappingCollection.class);
          for (StubMapping mapping : stubCollection.getMappingOrMappings()) {
            mapping.setDirty(false);
            stubMappings.addMapping(mapping);
            StubMappingFileMetadata fileMetadata =
                new StubMappingFileMetadata(mappingFile.getPath(), stubCollection.isMulti());
            fileNameMap.put(mapping.getId(), fileMetadata);
          }
        } catch (JsonException e) {
          throw new MappingFileException(mappingFile.getPath(), e.getErrors().first().getDetail());
        }
      }
    }
  }

  private static class StubMappingFileMetadata {
    final String path;
    final boolean multi;

    public StubMappingFileMetadata(String path, boolean multi) {
      this.path = path;
      this.multi = multi;
    }
  }
}
