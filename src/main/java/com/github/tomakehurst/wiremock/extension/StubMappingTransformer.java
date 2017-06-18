package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public abstract class StubMappingTransformer implements Extension {
    public abstract StubMapping transform(StubMapping stubMapping, FileSource files, Parameters parameters);

    public boolean applyGlobally() {
        return true;
    }
}
