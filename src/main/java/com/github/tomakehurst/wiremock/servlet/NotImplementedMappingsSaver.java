package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.core.MappingsSaver;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;

public class NotImplementedMappingsSaver implements MappingsSaver {
    @Override
    public void saveMappings(StubMappings stubMappings) {
        throw new UnsupportedOperationException("Saving mappings is not supported");
    }
}
