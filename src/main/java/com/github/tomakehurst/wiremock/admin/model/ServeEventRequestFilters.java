package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Predicate;

/**
 * A collection a RequestPattern filters to apply to ServeEvents
 */
public class ServeEventRequestFilters implements Predicate<ServeEvent> {
    @JsonUnwrapped
    private final RequestPattern filters;

    @JsonCreator
    public ServeEventRequestFilters(@JsonProperty("filters") RequestPattern filters) {
        this.filters = filters;
    }

    @Override
    public boolean apply(ServeEvent serveEvent) {
        return filters
            .match(serveEvent.getRequest())
            .isExactMatch();
    }
}
