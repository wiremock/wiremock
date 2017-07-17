package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonMatchingFlags {

    private final Boolean ignoreArrayOrder;
    private final Boolean ignoreExtraElements;

    public JsonMatchingFlags(@JsonProperty("ignoreArrayOrder") Boolean ignoreArrayOrder,
                             @JsonProperty("ignoreExtraElements") Boolean ignoreExtraElements) {
        this.ignoreArrayOrder = ignoreArrayOrder;
        this.ignoreExtraElements = ignoreExtraElements;
    }

    public Boolean isIgnoreArrayOrder() {
        return ignoreArrayOrder;
    }

    public Boolean isIgnoreExtraElements() {
        return ignoreExtraElements;
    }
}
