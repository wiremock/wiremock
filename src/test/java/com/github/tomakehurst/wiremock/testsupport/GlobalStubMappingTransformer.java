package com.github.tomakehurst.wiremock.testsupport;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class GlobalStubMappingTransformer  extends StubMappingTransformer {
    @Override
    public StubMapping transform(StubMapping stubMapping, FileSource files, Parameters parameters) {
        return WireMock
            .get(urlEqualTo(stubMapping.getRequest().getUrl() + "?transformed=global"))
            .build();
    }

    @Override
    public String getName() {
        return "stub-transformer";
    }
}
