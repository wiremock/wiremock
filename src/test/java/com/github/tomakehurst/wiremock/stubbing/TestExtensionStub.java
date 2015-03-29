package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.WiremockExtension;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class TestExtensionStub implements WiremockExtension {

    private static int calls = 0;

    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition) {
        calls++;
        return responseDefinition;
    }

    @Override
    public String getName() {
        return "Test Extension";
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    public static int getCalls() {
        return calls;
    }
}
