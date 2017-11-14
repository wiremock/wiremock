/*
 * Copyright (C) 2011 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.AbstractFileSource;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SafeNames;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.common.Json.write;
import static com.google.common.collect.Iterables.filter;

public class JsonFileMappingsSource implements MappingsSource {

	private final FileSource mappingsFileSource;
	private final Map<UUID, String> fileNameMap;

	public JsonFileMappingsSource(FileSource mappingsFileSource) {
		this.mappingsFileSource = mappingsFileSource;
		fileNameMap = new HashMap<>();
	}

	@Override
	public void save(List<StubMapping> stubMappings) {
		for (StubMapping mapping: stubMappings) {
			if (mapping != null && mapping.isDirty()) {
				save(mapping);
			}
		}
	}

	@Override
	public void save(StubMapping stubMapping) {
		String mappingFileName = fileNameMap.get(stubMapping.getId());
		if (mappingFileName == null) {
			mappingFileName = SafeNames.makeSafeFileName(stubMapping);
		}
		mappingsFileSource.writeTextFile(mappingFileName, write(stubMapping));
        fileNameMap.put(stubMapping.getId(), mappingFileName);
		stubMapping.setDirty(false);
	}

    @Override
    public void remove(StubMapping stubMapping) {
        String mappingFileName = fileNameMap.get(stubMapping.getId());
        mappingsFileSource.deleteFile(mappingFileName);
        fileNameMap.remove(stubMapping.getId());
    }

	@Override
	public void removeAll() {
		for (String filename: fileNameMap.values()) {
			mappingsFileSource.deleteFile(filename);
		}
		fileNameMap.clear();
	}

	@Override
	public void loadMappingsInto(StubMappings stubMappings) {
		if (!mappingsFileSource.exists()) {
			return;
		}
		Iterable<TextFile> mappingFiles = filter(mappingsFileSource.listFilesRecursively(), AbstractFileSource.byFileExtension("json"));
		for (TextFile mappingFile: mappingFiles) {
            StubMapping mapping = StubMapping.buildFrom(mappingFile.readContentsAsString());
            mapping.setDirty(false);
			stubMappings.addMapping(mapping);
			fileNameMap.put(mapping.getId(), mappingFile.getPath());
		}
	}

}
