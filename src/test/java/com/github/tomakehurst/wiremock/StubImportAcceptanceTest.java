package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.admin.model.StubImport;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.stubMappingWithUrl;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StubImportAcceptanceTest extends AcceptanceTestBase {

    private static Admin admin;

    @BeforeClass
    public static void setup() {
        admin = wireMockServer;
    }

    @Test
    public void importsAllStubsWhenNoneAreAlreadyPresent() {
        StubImport stubImport = new StubImport(asList(
                get("/one").willReturn(ok()).build(),
                post("/two").willReturn(ok()).build(),
                put("/three").willReturn(ok()).build()
        ), StubImport.Options.DEFAULTS);

        admin.importStubs(stubImport);

        List<StubMapping> stubs = admin.listAllStubMappings().getMappings();

        assertThat(stubs, hasItem(stubMappingWithUrl("/one")));
        assertThat(stubs, hasItem(stubMappingWithUrl("/two")));
        assertThat(stubs, hasItem(stubMappingWithUrl("/three")));
    }

    @Test
    public void overwritesExistingStubsByDefault() {
        UUID id1 = UUID.randomUUID();
        wm.stubFor(get("/one")
                .withId(id1)
                .willReturn(ok("Original")));

        StubImport stubImport = new StubImport(asList(
                get("/one")
                        .withId(id1)
                        .willReturn(ok("Updated")).build(),
                post("/two").willReturn(ok()).build(),
                put("/three").willReturn(ok()).build()
        ), StubImport.Options.DEFAULTS);

        admin.importStubs(stubImport);

        List<StubMapping> stubs = admin.listAllStubMappings().getMappings();
        assertThat(stubs.size(), is(3));
        assertThat(stubs.get(2).getResponse().getBody(), is("Updated"));
    }

    @Test
    public void ignoresExistingStubsIfConfigured() {
        UUID id1 = UUID.randomUUID();
        wm.stubFor(get("/one")
                .withId(id1)
                .willReturn(ok("Original")));

        StubImport stubImport = new StubImport(asList(
                get("/one")
                        .withId(id1)
                        .willReturn(ok("Updated")).build(),
                post("/two").willReturn(ok()).build(),
                put("/three").willReturn(ok()).build()
        ), new StubImport.Options(StubImport.Options.DuplicatePolicy.IGNORE, false));

        WireMock wireMock = new WireMock(wireMockServer.port());
        wireMock.importStubMappings(stubImport);

        List<StubMapping> stubs = admin.listAllStubMappings().getMappings();
        assertThat(stubs.size(), is(3));
        assertThat(stubs.get(2).getResponse().getBody(), is("Original"));
    }

    @Test
    public void deletesStubsNotInImportIfConfigured() {
        UUID id1 = UUID.randomUUID();
        wm.stubFor(get("/one")
                .withId(id1)
                .willReturn(ok("Original")));
        wm.stubFor(get("/four").willReturn(ok()));
        wm.stubFor(get("/five").willReturn(ok()));

        StubImport stubImport = new StubImport(asList(
                get("/one")
                        .withId(id1)
                        .willReturn(ok("Updated")).build(),
                post("/two").willReturn(ok()).build(),
                put("/three").willReturn(ok()).build()
        ), new StubImport.Options(StubImport.Options.DuplicatePolicy.OVERWRITE, true));

        WireMock.importStubs(stubImport);

        List<StubMapping> stubs = admin.listAllStubMappings().getMappings();
        assertThat(stubs.size(), is(3));
        assertThat(stubs, hasItem(stubMappingWithUrl("/one")));
        assertThat(stubs, hasItem(stubMappingWithUrl("/two")));
        assertThat(stubs, hasItem(stubMappingWithUrl("/three")));
        assertThat(stubs.get(2).getResponse().getBody(), is("Updated"));
    }

    @Test
    public void doesNotDeleteStubsNotInImportIfNotConfigured() {
        UUID id1 = UUID.randomUUID();
        wm.stubFor(get("/one")
                .withId(id1)
                .willReturn(ok("Original")));
        wm.stubFor(get("/four").willReturn(ok()));
        wm.stubFor(get("/five").willReturn(ok()));

        StubImport stubImport = new StubImport(asList(
                get("/one")
                        .withId(id1)
                        .willReturn(ok("Updated")).build(),
                post("/two").willReturn(ok()).build(),
                put("/three").willReturn(ok()).build()
        ), new StubImport.Options(StubImport.Options.DuplicatePolicy.OVERWRITE, false));

        WireMock.importStubs(stubImport);

        List<StubMapping> stubs = admin.listAllStubMappings().getMappings();
        assertThat(stubs.size(), is(5));
    }
}
