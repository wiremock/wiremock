package com.tomakehurst.wiremock.standalone;

import static java.lang.System.out;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import com.google.common.io.CharStreams;
import com.tomakehurst.wiremock.mapping.JsonMappingCreator;
import com.tomakehurst.wiremock.mapping.Mappings;

public class JsonFileMappingLoader {

	private JsonMappingCreator jsonMappingCreator;
	private String mappingJsonDirectory;
	
	public JsonFileMappingLoader(Mappings mappings, String mappingJsonDirectory) {
		this.jsonMappingCreator = new JsonMappingCreator(mappings);
		this.mappingJsonDirectory = mappingJsonDirectory;
	}

	public void loadMappings() {
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
		try {
			String json = CharStreams.toString(new FileReader(jsonFile));
			return json;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
}
