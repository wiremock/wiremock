package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.NearMiss;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import java.util.Collections;
import java.util.List;

public class ServedStub {

    public final LoggedRequest request;
    public final ResponseDefinition responseDefinition;
    public final List<NearMiss> nearMisses;

    public ServedStub(Request request, ResponseDefinition responseDefinition, List<NearMiss> nearMisses) {
        this.request = LoggedRequest.createFrom(request);
        this.responseDefinition = responseDefinition;
        this.nearMisses = nearMisses;
    }

    public static ServedStub noExactMatch(Request request, List<NearMiss> nearMisses) {
        return new ServedStub(request, ResponseDefinition.notConfigured(), nearMisses);
    }

    public static ServedStub exactMatch(Request request, ResponseDefinition responseDefinition) {
        return new ServedStub(request, responseDefinition, Collections.<NearMiss>emptyList());
    }

    public boolean noStubFound() {
        return !responseDefinition.wasConfigured();
    }
}
