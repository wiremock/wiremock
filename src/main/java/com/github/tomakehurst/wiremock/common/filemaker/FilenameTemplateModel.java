package com.github.tomakehurst.wiremock.common.filemaker;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public class FilenameTemplateModel {
    private final StubMapping stubMapping;
    public FilenameTemplateModel(StubMapping stubMapping) {
        this.stubMapping = stubMapping;
    }

    public StubMapping getStubMapping() {
        return stubMapping;
    }
}
