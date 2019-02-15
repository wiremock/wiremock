package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.common.FileSource;

public class StubMappingContext {
    private FileSource files;

    public FileSource getFiles() {
        return files;
    }

    public StubMappingContext setFiles(FileSource files) {
        this.files = files;
        return this;
    }
}
