package com.tomakehurst.wiremock.standalone;

import static com.google.common.collect.Iterables.filter;

import com.google.common.base.Predicate;
import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.TextFile;
import com.tomakehurst.wiremock.mapping.JsonMappingCreator;
import com.tomakehurst.wiremock.mapping.Mappings;

public class JsonFileMappingsLoader implements MappingsLoader {

	private final FileSource mappingsFileSource;
	
	public JsonFileMappingsLoader(FileSource mappingsFileSource) {
		this.mappingsFileSource = mappingsFileSource;
	}

	@Override
	public void loadMappingsInto(Mappings mappings) {
		JsonMappingCreator jsonMappingCreator = new JsonMappingCreator(mappings);
		Iterable<TextFile> mappingFiles = filter(mappingsFileSource.list(), byFileExtension("json"));
		for (TextFile mappingFile: mappingFiles) {
			jsonMappingCreator.addMappingFrom(mappingFile.readContents());
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
