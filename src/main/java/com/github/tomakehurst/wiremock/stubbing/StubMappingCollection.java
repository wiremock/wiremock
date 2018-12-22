package com.github.tomakehurst.wiremock.stubbing;

import java.util.List;

public class StubMappingCollection extends StubMapping {

    private List<StubMapping> mappings;

    public boolean isMulti() {
        return mappings != null;
    }

    public List<StubMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<StubMapping> mappings) {
        this.mappings = mappings;
    }
}
