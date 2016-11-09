package com.github.tomakehurst.wiremock.standalone;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static com.github.tomakehurst.wiremock.common.AbstractFileSource.byFileExtension;
import static com.google.common.collect.Iterables.filter;

public class RemoteMappingsLoader {

    private final FileSource fileSource;
    private final WireMock wireMock;

    public RemoteMappingsLoader(FileSource fileSource, WireMock wireMock) {
        this.fileSource = fileSource;
        this.wireMock = wireMock;
    }

    public void load() {
        FileSource mappingsSource = fileSource.child(MAPPINGS_ROOT);
        Iterable<TextFile> mappingFiles = filter(mappingsSource.listFilesRecursively(), byFileExtension("json"));
        for (TextFile mappingFile : mappingFiles) {
            StubMapping mapping = StubMapping.buildFrom(mappingFile.readContentsAsString());
            wireMock.register(mapping);
        }
    }

}
