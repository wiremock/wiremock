package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WireMockServerTests {

    @Test
    public void instantiationWithEmptyFileSource() throws IOException {
        Path tempDir = Files.createTempDirectory("forwiremock");
        Options options = new WireMockConfiguration().fileSource(new SingleRootFileSource(tempDir.toFile()));

        WireMockServer wireMockServer = null;
        try {
            wireMockServer = new WireMockServer(options);
            wireMockServer.start();
        } finally {
            if (wireMockServer != null) {
                wireMockServer.stop();
            }
        }
    }
}
