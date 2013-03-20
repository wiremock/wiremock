package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

public class WireMockServerTests {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void instantiationWithEmptyFileSource() throws IOException {
        Options options = new WireMockConfiguration().fileSource(new SingleRootFileSource(tempDir.getRoot()));

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
