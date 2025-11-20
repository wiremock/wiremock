package com.github.tomakehurst.wiremock.stubbing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = StubMapping.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StubMappingCollection.class),
        @JsonSubTypes.Type(StubMapping.class)
})
public interface StubMappingOrMappings {

    @JsonIgnore
    List<StubMapping> getMappingOrMappings();

    @JsonIgnore
    boolean isMulti();
}
