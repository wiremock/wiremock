package com.tomakehurst.wiremock.standalone;

import static java.lang.System.out;

import java.io.File;
import java.io.FilenameFilter;

import com.tomakehurst.wiremock.common.TextFile;
import com.tomakehurst.wiremock.mapping.JsonMappingCreator;
import com.tomakehurst.wiremock.mapping.Mappings;

public class JsonFileMappingsLoader implements MappingsLoader {

	private String mappingJsonDirectory;
	
	public JsonFileMappingsLoader(String mappingJsonDirectory) {
		this.mappingJsonDirectory = mappingJsonDirectory;
	}

	@Override
	public void loadMappingsInto(Mappings mappings) {
		JsonMappingCreator jsonMappingCreator = new JsonMappingCreator(mappings);
		
		File jsonDir = new File(mappingJsonDirectory);
		if (jsonDir.exists() && !jsonDir.isDirectory()) {
			throw new RuntimeException(jsonDir + " is not a directory");
		} else if (!jsonDir.exists()) {
			jsonDir.mkdirs();
		}
		
		out.println("Loading mappings from JSON in " + jsonDir.getAbsolutePath());
		
		for (File jsonFile: jsonDir.listFiles(jsonFilenameFilter())) {
			String json = readJsonFrom(jsonFile);
			jsonMappingCreator.addMappingFrom(json);
		}
	}

	private FilenameFilter jsonFilenameFilter() {
		return new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".json");
			}
		};
	}
	
	private String readJsonFrom(File jsonFile) {
		return new TextFile(jsonFile).readContents();
	}
}
