package com.github.tomakehurst.wiremock.junit;

import com.github.tomakehurst.wiremock.client.MappingBuilder;

public interface Stubbing {

    void givenThat(MappingBuilder mappingBuilder);
    void stubFor(MappingBuilder mappingBuilder);

}
