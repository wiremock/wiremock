package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public class SnapshotStubMappingBodyExtractor {
    private final FileSource fileSource;
    private final IdGenerator idGenerator;

    public SnapshotStubMappingBodyExtractor(FileSource fileSource) {
        this(fileSource, new VeryShortIdGenerator());
    }

    public SnapshotStubMappingBodyExtractor(FileSource fileSource, IdGenerator idGenerator) {
        this.fileSource = fileSource;
        this.idGenerator = idGenerator;
    }

    /**
     * Extracts body of the ResponseDefinition to a file written to the FILES_ROOT.
     * Modifies the ResponseDefinition to point to the file in-place
     *
     * @fixme Generates multiple files for stub mappings with identical responses
     * @param stubMapping Stub mapping to extract
     */
    public void extractInPlace(StubMapping stubMapping) {
        String fileId = idGenerator.generate();
        byte[] body = stubMapping.getResponse().getByteBody();
        String extension = ContentTypes.determineFileExtension(
            stubMapping.getRequest().getUrl(),
            stubMapping.getResponse().getHeaders().getContentTypeHeader(),
            body);
        String bodyFileName = UniqueFilenameGenerator.generate(
            stubMapping.getRequest().getUrl(),
            WireMockApp.FILES_ROOT + "/body",
            fileId,
            extension
        );

         // used to prevent ambiguous method call error for withBody()
        String noStringBody = null;
        byte[] noByteBody = null;

        stubMapping.setResponse(
            ResponseDefinitionBuilder
                .like(stubMapping.getResponse())
                .withBodyFile(bodyFileName)
                .withBody(noStringBody)
                .withBody(noByteBody)
                .withBase64Body(null)
                .build()
        );

        fileSource.writeBinaryFile(bodyFileName, body);
    }
}
