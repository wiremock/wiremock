package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.common.Json;

import java.util.Collections;
import java.util.List;

public class StubMappingJsonParser {

    public List<StubMapping> parse(String json) {
        StubMappingCollection stubs = Json.read(json, StubMappingCollection.class);

        return stubs.isMulti() ? stubs.getMappings() : Collections.singletonList((StubMapping) stubs);
    }


}
