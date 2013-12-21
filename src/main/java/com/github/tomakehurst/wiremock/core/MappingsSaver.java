package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.stubbing.StubMappings;

public interface MappingsSaver {
    void saveMappings(StubMappings stubMappings);
}
