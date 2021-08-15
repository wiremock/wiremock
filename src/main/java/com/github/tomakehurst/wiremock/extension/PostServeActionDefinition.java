package com.github.tomakehurst.wiremock.extension;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostServeActionDefinition {

    private final String name;
    private final Parameters parameters;

    public PostServeActionDefinition(@JsonProperty("name") String name,
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
