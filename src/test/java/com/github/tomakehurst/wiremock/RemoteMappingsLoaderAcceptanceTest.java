package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.File;

import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RemoteMappingsLoaderAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void loadsTheMappingsFromTheDirectorySpecifiedIntoTheRemoteWireMockServer() throws Exception {
        setupServerWithTempFileRoot();

        WireMock wmClient = new WireMock(wireMockServer.port());

        File rootDir = new File(Resources.getResource("remoteloader").toURI());
        wmClient.loadMappingsFrom(rootDir);

        assertThat(
            testClient.get("/remote-load/1").content(), is("Remote load 1")
        );
        assertThat(
            testClient.get(
                "/remote-load/2", withHeader("Accept", "text/plain")
            ).content(), is("Remote load 2")
        );
    }

    @Test
    public void convertsBodyFileToStringBodyWhenAKnownTextType() {
        
    }
}
