package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.extension.Parameters;

public class CustomMatcherDefinition {

    private String name;
    private Parameters parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }
}
