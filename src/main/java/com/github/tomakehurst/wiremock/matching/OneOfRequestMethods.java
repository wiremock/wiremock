package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import java.util.Set;
import java.util.stream.Collectors;

public class OneOfRequestMethods implements NamedValueMatcher<RequestMethod> {

    private final Set<RequestMethod> methods;

    public OneOfRequestMethods(@JsonProperty("methods") Set<RequestMethod> methods) {
        this.methods = methods.stream().filter(e -> !RequestMethod.ANY.equals(e)).collect(Collectors.toSet());
    }

    public Set<RequestMethod> getMethods() {
        return methods;
    }

    @Override
    public String getName() {
        return "oneOf: " + methods.toString();
    }

    @Override
    public String getExpected() {
        return getName();
    }

    @Override
    public MatchResult match(RequestMethod value) {
        return MatchResult.of(
                this.methods.contains(value));
    }
}
