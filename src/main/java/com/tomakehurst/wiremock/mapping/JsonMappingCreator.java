package com.tomakehurst.wiremock.mapping;


public class JsonMappingCreator {

	private Mappings mappings;

	public JsonMappingCreator(Mappings mappings) {
		this.mappings = mappings;
	}
	
	public void addMappingFrom(String mappingSpecJson) {
		RequestResponseMapping mapping = JsonMappingBinder.buildMappingFrom(mappingSpecJson);
		mappings.addMapping(mapping);
	}
}