package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.Stubbing;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.hasFileContaining;
import static com.google.common.base.Charsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StubMappingPersistenceAcceptanceTest {

    Path rootDir;
    WireMockServer wireMockServer;
    WireMockTestClient testClient;
    Stubbing wm;

    @Before
    public void init() throws Exception {
        rootDir = Files.createTempDirectory("temp-filesource");
        FileSource fileSource = new SingleRootFileSource(rootDir.toAbsolutePath().toString());
        fileSource.createIfNecessary();
        FileSource filesFileSource = fileSource.child(FILES_ROOT);
        filesFileSource.createIfNecessary();
        FileSource mappingsFileSource = fileSource.child(MAPPINGS_ROOT);
        mappingsFileSource.createIfNecessary();

        wireMockServer = new WireMockServer(wireMockConfig().fileSource(fileSource).dynamicPort());
        wireMockServer.start();
        testClient = new WireMockTestClient(wireMockServer.port());
        WireMock.configureFor(wireMockServer.port());
        wm = wireMockServer;
    }

    @Test
    public void savesAllInMemoryStubMappings() {
        wm.stubFor(get(urlEqualTo("/1")).willReturn(aResponse().withBody("one")));
        wm.stubFor(get(urlEqualTo("/2")).willReturn(aResponse().withBody("two")));
        wm.stubFor(get(urlEqualTo("/3")).willReturn(aResponse().withBody("three")));

        wireMockServer.saveMappings();

        assertThat(rootDir.resolve("mappings"), hasFileContaining("one"));
        assertThat(rootDir.resolve("mappings"), hasFileContaining("two"));
        assertThat(rootDir.resolve("mappings"), hasFileContaining("three"));
    }

    @Test
    public void savesEditedStubToTheFileItOriginatedFrom() throws Exception {
        Path mappingsDir = rootDir.resolve("mappings");
        UUID stubId = UUID.randomUUID();

        writeMappingFile("mapping-to-edit.json", get(urlEqualTo("/edit"))
            .withId(stubId)
            .willReturn(aResponse().withBody("initial"))
        );

        wireMockServer.resetToDefaultMappings(); // Loads from the file system

        assertThat(wm.getStubMappings().get(0).getId(), is(stubId));
        assertThat(wm.getStubMappings().get(0).getResponse().getBody(), is("initial"));

        wm.editStub(get(urlEqualTo("/edit"))
            .withId(stubId)
            .willReturn(aResponse().withBody("modified")));

        wireMockServer.saveMappings();

        assertThat(mappingsDir.toFile().list().length, is(1));
        assertThat(mappingsDir, hasFileContaining("modified"));
    }

    private void writeMappingFile(String name, MappingBuilder stubBuilder) throws IOException {
        Path mappingsDir = rootDir.resolve("mappings");
        byte[] json = Json.write(stubBuilder.build()).getBytes(UTF_8);
        Files.write(mappingsDir.resolve(name), json);
    }
}
