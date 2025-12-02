package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.IS_PERSISTENT;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.StubImport.stubImport;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;

public class StubImportPeristenceAcceptanceTest {

    public static final StubLifecycleListener PERSISTENT_SETTING_LISTENER = new StubLifecycleListener() {

        @Override
        public StubMapping beforeStubCreated(StubMapping stub) {
            return stub.transform(b -> b.setPersistent(true));
        }

        @Override
        public StubMapping beforeStubEdited(StubMapping oldStub, StubMapping newStub) {
            return newStub.transform(b -> b.setPersistent(true));
        }

        @Override
        public String getName() {
            return "persist-flag-setter";
        }
    };

    @TempDir
    public static Path tempDir;

    @RegisterExtension
    public WireMockExtension wm =
            WireMockExtension.newInstance()
                    .options(wireMockConfig()
                            .dynamicPort()
                            .withRootDirectory(tempDir.toAbsolutePath().toString())
                            .extensions(PERSISTENT_SETTING_LISTENER))
                    .build();


    @Test
    void persistsStubsWhenPersistenceFlagSetByListener() {
        UUID stub1Id = UUID.randomUUID();
        UUID stub2Id = UUID.randomUUID();
        UUID stub3Id = UUID.randomUUID();

        wm.importStubs(
                stubImport()
                        .stub(get("/one").withId(stub1Id).willReturn(ok()))
                        .stub(post("/two").withId(stub2Id).willReturn(ok()))
                        .stub(put("/three").withId(stub3Id).willReturn(ok()))
                        .build());

        wm.resetToDefaultMappings();
        List<StubMapping> stubs = wm.listAllStubMappings().getMappings();
        assertThat(stubs.size(), is(3));
        assertThat(stubs, everyItem(IS_PERSISTENT));

        assertThat(wm.getStubMapping(stub1Id).getItem().getInsertionIndex(), is(3L));
        assertThat(wm.getStubMapping(stub2Id).getItem().getInsertionIndex(), is(2L));
        assertThat(wm.getStubMapping(stub3Id).getItem().getInsertionIndex(), is(1L));

        wm.importStubs(
            stubImport()
                    .stub(post("/two").withId(stub2Id).willReturn(ok("Updated")))
                    .build());

        wm.resetToDefaultMappings();
        assertThat(wm.getStubMapping(stub2Id).getItem().getResponse().getBody(), is("Updated"));
    }
}
