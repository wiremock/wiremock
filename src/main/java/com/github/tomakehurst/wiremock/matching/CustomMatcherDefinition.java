package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.tomakehurst.wiremock.extension.Parameters;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class CustomMatcherDefinition {

    private final String name;
    private final Parameters parameters;

    public CustomMatcherDefinition(@JsonProperty("name") String name,
                                   @JsonProperty("parameters") Parameters parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public Parameters getParameters() {
        return parameters;
    }

}
