package com.github.tomakehurst.wiremock.junit;

import com.github.tomakehurst.wiremock.client.LocalMappingBuilder;
import com.github.tomakehurst.wiremock.client.LocalRequestPatternBuilder;
import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;

public interface LocalStubbing extends Stubbing {

    void givenThat(LocalMappingBuilder mappingBuilder);
    void stubFor(LocalMappingBuilder mappingBuilder);

    void verify(LocalRequestPatternBuilder requestPatternBuilder);
    void verify(int count, LocalRequestPatternBuilder requestPatternBuilder);
}
