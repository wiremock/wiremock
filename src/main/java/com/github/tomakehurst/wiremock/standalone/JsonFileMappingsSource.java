/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.common.Json.writePrivate;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.http.MimeType;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappingCollection;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonFileMappingsSource implements MappingsSource {

  private final FileSource mappingsFileSource;
  private final Map<UUID, StubMappingFileMetadata> fileNameMap;
  private final FilenameMaker filenameMaker;

  public JsonFileMappingsSource(FileSource mappingsFileSource, FilenameMaker filenameMaker) {
    this.mappingsFileSource = mappingsFileSource;
    this.filenameMaker = Objects.requireNonNullElseGet(filenameMaker, FilenameMaker::new);
    fileNameMap = new HashMap<>();
  }

  @Override
  public void save(List<StubMapping> stubMappings) {
    for (StubMapping mapping : stubMappings) {
      if (mapping != null && mapping.isDirty()) {
        save(mapping);
      }
    }
  }

  @Override
  public void save(StubMapping stubMapping) {
    StubMappingFileMetadata fileMetadata = fileNameMap.get(stubMapping.getId());
    if (fileMetadata == null) {
      fileMetadata = new StubMappingFileMetadata(filenameMaker.filenameFor(stubMapping), false);
    }

    if (fileMetadata.multi) {
      throw new NotWritableException(
          "Stubs loaded from multi-mapping files are read-only, and therefore cannot be saved");
    }

    mappingsFileSource.writeTextFile(fileMetadata.path, writePrivate(stubMapping));

    fileNameMap.put(stubMapping.getId(), fileMetadata);
    stubMapping.setDirty(false);
  }

  @Override
  public void remove(StubMapping stubMapping) {
    StubMappingFileMetadata fileMetadata = fileNameMap.get(stubMapping.getId());
    if (fileMetadata.multi) {
      throw new NotWritableException(
          "Stubs loaded from multi-mapping files are read-only, and therefore cannot be removed");
    }

    mappingsFileSource.deleteFile(fileMetadata.path);
    fileNameMap.remove(stubMapping.getId());
  }

  @Override
  public void removeAll() {
    if (anyFilesAreMultiMapping()) {
      throw new NotWritableException(
          "Some stubs were loaded from multi-mapping files which are read-only, so remove all cannot be performed");
    }

    for (StubMappingFileMetadata fileMetadata : fileNameMap.values()) {
      mappingsFileSource.deleteFile(fileMetadata.path);
    }
    fileNameMap.clear();
  }

  private boolean anyFilesAreMultiMapping() {
    return fileNameMap.values().stream().anyMatch(input -> input.multi);
  }

  @Override
  public void loadMappingsInto(StubMappings stubMappings) {
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
              new StubMappingFileMetadata(mappingFile.getPath(), stubCollection.isMulti() );
          fileNameMap.put(mapping.getId(), fileMetadata);
        }
      } catch (JsonException e) {
        throw new MappingFileException(mappingFile.getPath(), e.getErrors().first().getDetail());
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
