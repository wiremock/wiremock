package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.io.Resources;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RemoteMappingsLoaderAcceptanceTest extends AcceptanceTestBase {

    static WireMock wmClient;
    static File rootDir;

    @BeforeClass
    public static void initWithTempDir() throws Exception {
        setupServerWithTempFileRoot();
        wmClient = new WireMock(wireMockServer.port());
        rootDir = new File(Resources.getResource("remoteloader").toURI());
    }

    @Test
    public void loadsTheMappingsFromTheDirectorySpecifiedIntoTheRemoteWireMockServer() {
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
    public void convertsBodyFileToStringBodyWhenAKnownTextTypeFromFileExtension() {
        wmClient.loadMappingsFrom(rootDir);

        assertThat(testClient.get("/text-file").content(), is("Some text"));
    }

    @Test
    public void convertsBodyFileToStringBodyWhenAKnownImageTypeFromFileExtension() {
        wmClient.loadMappingsFrom(rootDir);

        assertThat(testClient.get("/text-file").content(), is("Some text"));
    }
}
