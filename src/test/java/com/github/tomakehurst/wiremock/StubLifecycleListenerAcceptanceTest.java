package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StubLifecycleListenerAcceptanceTest {

    TestStubLifecycleListener listener = new TestStubLifecycleListener();

    @Rule
    public WireMockRule wm = new WireMockRule(options()
            .dynamicPort()
            .extensions(listener));

    @Before
    public void init() {
        listener.events.clear();
    }

    @Test
    public void callbackMethodIsCalledForStubCreation() {
        wm.stubFor(get("/test").withName("Created").willReturn(ok()));
        assertThat(listener.events.get(0), is("stubCreated, name: Created"));
    }

    @Test
    public void callbackMethodIsCalledForStubEdit() {
        UUID id = UUID.randomUUID();
        wm.stubFor(get("/test").withId(id).withName("Created").willReturn(ok()));
        wm.editStub(get("/test").withId(id).withName("Edited").willReturn(ok()));
        assertThat(listener.events.get(1), is("stubEdited, name: Edited"));
    }

    @Test
    public void callbackMethodIsCalledForStubRemove() {
        StubMapping stub = wm.stubFor(get("/test").withName("To remove").willReturn(ok()));
        wm.removeStub(stub);
        assertThat(listener.events.get(1), is("stubRemoved, name: To remove"));
    }

    @Test
    public void callbackMethodIsCalledForStubsReset() {
        wm.stubFor(get("/test").withName("To remove").willReturn(ok()));
        wm.resetMappings();
        assertThat(listener.events.get(1), is("stubsReset"));
    }

    @Test
    public void callbackMethodIsCalledForStubsResetToDefault() {
        wm.stubFor(get("/test").withName("To remove").willReturn(ok()));
        wm.resetToDefaultMappings();
        assertThat(listener.events.get(1), is("stubsResetToDefaults"));
    }

    public static class TestStubLifecycleListener implements StubLifecycleListener {

        public List<String> events = new ArrayList<>();

        @Override
        public void stubCreated(StubMapping stub) {
            events.add("stubCreated, name: " + stub.getName());
        }

        @Override
        public void stubEdited(StubMapping stub) {
            events.add("stubEdited, name: " + stub.getName());
        }

        @Override
        public void stubRemoved(StubMapping stub) {
            events.add("stubRemoved, name: " + stub.getName());
        }

        @Override
        public void stubsReset() {
            events.add("stubsReset");
        }

        @Override
        public void stubsResetToDefaults() {
            events.add("stubsResetToDefaults");
        }

        @Override
        public String getName() {
            return "test-stub-lifecycle-listener";
        }
    }
}
