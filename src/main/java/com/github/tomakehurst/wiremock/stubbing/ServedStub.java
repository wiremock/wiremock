package com.github.tomakehurst.wiremock.stubbing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.NearMiss;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMissCalculator;

import java.util.List;

import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include = NON_NULL)
public class ServedStub {

    private final LoggedRequest request;
    private final ResponseDefinition responseDefinition;

    public ServedStub(LoggedRequest request, ResponseDefinition responseDefinition) {
        this.request = request;
        this.responseDefinition = responseDefinition;
    }

    public static ServedStub noExactMatch(LoggedRequest request) {
        return new ServedStub(request, ResponseDefinition.notConfigured());
    }

    public static ServedStub exactMatch(LoggedRequest request, ResponseDefinition responseDefinition) {
        return new ServedStub(request, responseDefinition);
    }

    public boolean isNoExactMatch() {
        return !responseDefinition.wasConfigured();
    }

    public LoggedRequest getRequest() {
        return request;
    }

    public ResponseDefinition getResponseDefinition() {
        return responseDefinition;
    }

    public List<NearMiss> getNearMisses() {
        return null;
    }
}
