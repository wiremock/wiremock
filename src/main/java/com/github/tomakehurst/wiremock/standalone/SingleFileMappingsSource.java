/*
 * Copyright (C) 2024 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.MappingFileException;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * A MappingsLoader implementation that loads stub mappings from a single JSON
 * file.
 * This differs from the default JsonFileMappingsSource which loads from a
 * directory.
 * 
 * The file can contain either:
 * - A single stub mapping as a JSON object
 * - Multiple stub mappings as a JSON array
 */
public class SingleFileMappingsSource implements MappingsLoader {
    private final Path mappingsFile;

    public SingleFileMappingsSource(Path mappingsFile) {
        this.mappingsFile = validateFile(mappingsFile);
    }

    private Path validateFile(Path file) {
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Not a regular file: " + file);
        }
        return file;
    }

    @Override
    public void loadMappingsInto(StubMappings stubMappings) {
        try {
            byte[] content = Files.readAllBytes(mappingsFile);
            Object parsed = Json.read(content, Object.class);

            List<StubMapping> mappings;
            if (parsed instanceof List) {
                mappings = Json.getObjectMapper().readValue(
                    content, 
                    Json.getObjectMapper().getTypeFactory().constructCollectionType(
                        List.class, 
                        StubMapping.class
                    )
                );
            } else {
                mappings = Collections.singletonList(Json.read(content, StubMapping.class));
            }

            for (StubMapping mapping : mappings) {
                mapping.setDirty(false);
                stubMappings.addMapping(mapping);
            }
        } catch (Exception e) {
            throw new MappingFileException(mappingsFile.toString(), e);
        }
    }
}