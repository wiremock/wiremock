/*
 * Copyright (C) 2011-2021 Thomas Akehurst
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
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.filter;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappingCollection;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.google.common.base.Predicate;

import java.util.*;

public class JsonFileMappingsSource implements MappingsSource {

    private static final String WIREMOCK_GUI_KEY = "wiremock-gui";
    private static final String DIR_KEY = "folder";

    private final FileSource mappingsFileSource;
    private final Map<UUID, StubMappingFileMetadata> fileNameMap;

    public JsonFileMappingsSource(FileSource mappingsFileSource) {
        this.mappingsFileSource = mappingsFileSource;
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
            // we use gui folder definition to change path as sub directory from root.
            // Only when not saved yet.
            // TODO: This allows async between folder definition and actual file. Not sure if good or bad yet.
            final String folderDefinition = getFolderDefinition(stubMapping);
            if (folderDefinition != null) {
                fileMetadata = new StubMappingFileMetadata(folderDefinition.replaceFirst("/", "") + "/" + SafeNames.makeSafeFileName(stubMapping), false);
            } else {
                fileMetadata = new StubMappingFileMetadata(SafeNames.makeSafeFileName(stubMapping), false);
            }
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
        return any(
                fileNameMap.values(),
                new Predicate<StubMappingFileMetadata>() {
                    @Override
                    public boolean apply(StubMappingFileMetadata input) {
                        return input.multi;
                    }
                });
    }

    @Override
    public void loadMappingsInto(StubMappings stubMappings) {
        if (!mappingsFileSource.exists()) {
            return;
        }

        Iterable<TextFile> mappingFiles =
                filter(mappingsFileSource.listFilesRecursively(), byFileExtension("json"));
        for (TextFile mappingFile : mappingFiles) {
            try {
                StubMappingCollection stubCollection =
                        Json.read(mappingFile.readContentsAsString(), StubMappingCollection.class);
                for (StubMapping mapping : stubCollection.getMappingOrMappings()) {
                    mapping.setDirty(false);
                    createGuiFolderStructure(mapping, mappingFile);
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

    private boolean hasFolderDefinition(final StubMapping mapping) {
        Metadata metadata = mapping.getMetadata();
        if (metadata == null) {
            return false;
        }
        return metadata.getMap(WIREMOCK_GUI_KEY) != null && metadata.getMap(WIREMOCK_GUI_KEY).get(DIR_KEY) != null &&
                metadata.getMap(WIREMOCK_GUI_KEY).get(DIR_KEY) instanceof String;
    }

    private String getFolderDefinition(final StubMapping mapping) {
        if (hasFolderDefinition(mapping)) {
            return (String) mapping.getMetadata().getMap(WIREMOCK_GUI_KEY).get(DIR_KEY);
        }
        return null;
    }

    private void createGuiFolderStructure(StubMapping mapping, TextFile mappingFile) {
        Metadata metadata = mapping.getMetadata();
        if (metadata == null) {
            metadata = new Metadata();
        } else if (hasFolderDefinition(mapping)) {
            // skip files which contain a folder definition already.
            // TODO: This allows async between folder definition and actual file. Not sure if good or bad yet.
            return;
        }

        String path = mappingFile.getUri().getSchemeSpecificPart()
                .replace(this.mappingsFileSource.getUri().getSchemeSpecificPart(), "")
                .replace(".json", "");
        final int index = path.lastIndexOf('/');
        if (index != -1) {
            path = "/" + path.substring(0, path.lastIndexOf('/'));
        } else {
            return;
        }

        LinkedHashMap<String, String> guiData = new LinkedHashMap<>();
        metadata.put(WIREMOCK_GUI_KEY, guiData);
        guiData.put(DIR_KEY, path);
        mapping.setMetadata(metadata);
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
