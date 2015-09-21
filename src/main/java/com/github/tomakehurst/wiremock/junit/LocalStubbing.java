package com.github.tomakehurst.wiremock.junit;

import com.github.tomakehurst.wiremock.client.LocalMappingBuilder;

public interface LocalStubbing extends Stubbing {

    void givenThat(LocalMappingBuilder mappingBuilder);
    void stubFor(LocalMappingBuilder mappingBuilder);
}
