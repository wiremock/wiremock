package com.github.tomakehurst.wiremock.matching.optional;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.MatchesXPathPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import java.util.Map;

public class OptionalMatchesXPathPattern extends OptionalPattern {

    public OptionalMatchesXPathPattern(@JsonProperty("matchesOrAbsentXPath") final String expectedValue,
                                       @JsonProperty("namespaces") final Map<String, String> namespaces) {
        super(new MatchesXPathPattern(expectedValue, namespaces));
    }

    @JsonGetter("xPathNamespaces")
    public Map<String, String> getXPathNamespaces() {
        return ((MatchesXPathPattern) pattern).getXPathNamespaces();
    }

    public String getMatchesOrAbsentXPath() {
        return expectedValue;
    }

}
