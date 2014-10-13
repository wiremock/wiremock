package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.http.RequestMethod;

public class ScenarioMappingBuilder extends MappingBuilder<ScenarioMappingBuilder> {
	public ScenarioMappingBuilder(RequestMethod method, UrlMatchingStrategy urlMatchingStrategy) {
		super(method, urlMatchingStrategy);
	}

	public ScenarioMappingBuilder whenScenarioStateIs(String stateName) {
		this.requiredScenarioState = stateName;
		return this;
	}

	public ScenarioMappingBuilder willSetStateTo(String stateName) {
		this.newScenarioState = stateName;
		return this;
	}
}
