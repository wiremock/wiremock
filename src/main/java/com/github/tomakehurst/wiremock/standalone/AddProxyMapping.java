package com.github.tomakehurst.wiremock.standalone;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

public class AddProxyMapping implements MappingsLoader {
    private final String baseUrl;

    public AddProxyMapping(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void loadMappingsInto(StubMappings stubMappingsCollector) {
        RequestPattern requestPattern = newRequestPattern(ANY, anyUrl()).build();
        ResponseDefinition responseDef = responseDefinition()
                .proxiedFrom(baseUrl)
                .build();

        StubMapping proxyBasedMapping = new StubMapping(requestPattern, responseDef);
        proxyBasedMapping.setPriority(10); // Make it low priority so that existing stubs will take precedence
        stubMappingsCollector.addMapping(proxyBasedMapping);
    }
}
