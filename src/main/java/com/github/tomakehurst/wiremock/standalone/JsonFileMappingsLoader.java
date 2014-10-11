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
import com.github.tomakehurst.wiremock.stubbing.JsonStubMappingCreator;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.google.common.base.Predicate;

import static com.google.common.collect.Iterables.filter;

public class JsonFileMappingsLoader implements MappingsLoader {

	private final FileSource mappingsFileSource;
	
	public JsonFileMappingsLoader(FileSource mappingsFileSource) {
		this.mappingsFileSource = mappingsFileSource;
	}

	@Override
	public void loadMappingsInto(StubMappings stubMappings) {
		JsonStubMappingCreator jsonStubMappingCreator = new JsonStubMappingCreator(stubMappings);
		Iterable<TextFile> mappingFiles = filter(mappingsFileSource.listFilesRecursively(), byFileExtension("json"));
		for (TextFile mappingFile: mappingFiles) {
			jsonStubMappingCreator.addMappingFrom(mappingFile.readContentsAsString());
		}
	}
	
	private Predicate<TextFile> byFileExtension(final String extension) {
		return new Predicate<TextFile>() {
			public boolean apply(TextFile input) {
				return input.name().endsWith("." + extension);
			}
		};
	}
}
