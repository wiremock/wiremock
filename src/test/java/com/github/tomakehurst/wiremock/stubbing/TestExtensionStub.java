package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.WiremockExtension;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class TestExtensionStub implements WiremockExtension {

    private static int calls = 0;

    @Override
    public ResponseDefinition filter(Request request, ResponseDefinition responseDefinition) {
        calls++;
        return responseDefinition;
    }

    public static int getCalls() {
        return calls;
    }
}
