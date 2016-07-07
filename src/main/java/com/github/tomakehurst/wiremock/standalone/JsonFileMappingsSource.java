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

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.common.VeryShortIdGenerator;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.google.common.base.Predicate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.common.Json.write;
import static com.google.common.collect.Iterables.filter;

public class JsonFileMappingsSource implements MappingsSource {

	private final FileSource mappingsFileSource;
	private final VeryShortIdGenerator idGenerator;
	private final Map<UUID, String> fileNameMap;

	public JsonFileMappingsSource(FileSource mappingsFileSource) {
		this.mappingsFileSource = mappingsFileSource;
		idGenerator = new VeryShortIdGenerator();
		fileNameMap = new HashMap<>();
	}

	@Override
	public void saveMappings(StubMappings stubMappings) {
		Iterable<StubMapping> transientStubs = filter(stubMappings.getAll(), new Predicate<StubMapping>() {
			public boolean apply(StubMapping input) {
				return input != null && input.isTransient();
			}
		});

		for (StubMapping mapping : transientStubs) {
			String mappingFileName = fileNameMap.get(mapping.getUuid());
			if (mappingFileName == null) {
				mappingFileName = "saved-mapping-" + idGenerator.generate() + ".json";
			}
			mappingsFileSource.writeTextFile(mappingFileName, write(mapping));
			mapping.setTransient(false);
		}
	}

	@Override
	public void loadMappingsInto(StubMappings stubMappings) {
		if (!mappingsFileSource.exists()) {
			return;
		}
		Iterable<TextFile> mappingFiles = filter(mappingsFileSource.listFilesRecursively(), byFileExtension("json"));
		for (TextFile mappingFile: mappingFiles) {
            StubMapping mapping = StubMapping.buildFrom(mappingFile.readContentsAsString());
            mapping.setTransient(false);
			stubMappings.addMapping(mapping);
			fileNameMap.put(mapping.getUuid(), getFileName(mappingFile));
		}
	}
	
	private Predicate<TextFile> byFileExtension(final String extension) {
		return new Predicate<TextFile>() {
			public boolean apply(TextFile input) {
				return input.name().endsWith("." + extension);
			}
		};
	}

	private String getFileName(TextFile mappingFile) {
		return mappingFile.getUri().toString().replaceAll("^.*/", "");
	}
}
