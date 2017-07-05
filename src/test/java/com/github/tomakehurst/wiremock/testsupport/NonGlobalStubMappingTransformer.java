package com.github.tomakehurst.wiremock.testsupport;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

public class NonGlobalStubMappingTransformer extends StubMappingTransformer {
    @Override
    public StubMapping transform(StubMapping stubMapping, FileSource files, Parameters parameters) {
        return WireMock
            .get(stubMapping.getRequest().getUrl() + "?transformed=nonglobal")
            .withHeader("Accept", equalTo("B"))
            .build();
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    @Override
    public String getName() {
        return "nonglobal-transformer";
    }
}
