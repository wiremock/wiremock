package com.github.tomakehurst.wiremock.stubbing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.NearMiss;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import java.util.List;

import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include = NON_NULL)
public class ServedStub {

    private final LoggedRequest request;
    private final ResponseDefinition responseDefinition;
    private final List<NearMiss> nearMisses;

    public ServedStub(Request request, ResponseDefinition responseDefinition, List<NearMiss> nearMisses) {
        this.request = LoggedRequest.createFrom(request);
        this.responseDefinition = responseDefinition;
        this.nearMisses = nearMisses;
    }

    public static ServedStub noExactMatch(Request request, List<NearMiss> nearMisses) {
        return new ServedStub(request, ResponseDefinition.notConfigured(), nearMisses);
    }

    public static ServedStub exactMatch(Request request, ResponseDefinition responseDefinition) {
        return new ServedStub(request, responseDefinition, null);
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
        return nearMisses;
    }
}
