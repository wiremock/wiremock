package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import java.util.Collections;
import java.util.List;

public class ServedStub {

    public final LoggedRequest request;
    public final ResponseDefinition responseDefinition;
    public final List<StubMapping> nearMisses;

    public ServedStub(Request request, ResponseDefinition responseDefinition, List<StubMapping> nearMisses) {
        this.request = LoggedRequest.createFrom(request);
        this.responseDefinition = responseDefinition;
        this.nearMisses = nearMisses;
    }

    public static ServedStub noNearMisses(Request request, ResponseDefinition responseDefinition) {
        return new ServedStub(request, responseDefinition, Collections.<StubMapping>emptyList());
    }

    public boolean noStubFound() {
        return !responseDefinition.wasConfigured();
    }
}
