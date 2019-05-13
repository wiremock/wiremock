/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public void beforeAndAfterMethodsAreCalledForStubCreation() {
        wm.stubFor(get("/test").withName("Created").willReturn(ok()));
        assertThat(listener.events.get(0), is("beforeStubCreated, name: Created"));
        assertThat(listener.events.get(1), is("afterStubCreated, name: Created"));
    }

    @Test
    public void callbackMethodIsCalledForStubEdit() {
        UUID id = UUID.randomUUID();
        wm.stubFor(get("/test").withId(id).withName("Created").willReturn(ok()));
        wm.editStub(get("/test").withId(id).withName("Edited").willReturn(ok()));
        assertThat(listener.events.get(2), is("beforeStubEdited, old name: Created, new name: Edited"));
        assertThat(listener.events.get(3), is("afterStubEdited, old name: Created, new name: Edited"));
    }

    @Test
    public void callbackMethodIsCalledForStubRemove() {
        StubMapping stub = wm.stubFor(get("/test").withName("To remove").willReturn(ok()));
        wm.removeStub(stub);
        assertThat(listener.events.get(2), is("beforeStubRemoved, name: To remove"));
        assertThat(listener.events.get(3), is("afterStubRemoved, name: To remove"));
    }

    @Test
    public void callbackMethodIsCalledForStubsReset() {
        wm.stubFor(get("/test").withName("To remove").willReturn(ok()));
        wm.resetMappings();
        assertThat(listener.events.get(2), is("beforeStubsReset"));
        assertThat(listener.events.get(3), is("afterStubsReset"));
    }

    public static class TestStubLifecycleListener implements StubLifecycleListener {

        public List<String> events = new ArrayList<>();

        @Override
        public void beforeStubCreated(StubMapping stub) {
            events.add("beforeStubCreated, name: " + stub.getName());
        }

        @Override
        public void afterStubCreated(StubMapping stub) {
            events.add("afterStubCreated, name: " + stub.getName());
        }

        @Override
        public void beforeStubEdited(StubMapping oldStub, StubMapping newStub) {
            events.add("beforeStubEdited, old name: " + oldStub.getName() + ", new name: " + newStub.getName());
        }

        @Override
        public void afterStubEdited(StubMapping oldStub, StubMapping newStub) {
            events.add("afterStubEdited, old name: " + oldStub.getName() + ", new name: " + newStub.getName());
        }

        @Override
        public void beforeStubRemoved(StubMapping stub) {
            events.add("beforeStubRemoved, name: " + stub.getName());
        }

        @Override
        public void afterStubRemoved(StubMapping stub) {
            events.add("afterStubRemoved, name: " + stub.getName());
        }

        @Override
        public void beforeStubsReset() {
            events.add("beforeStubsReset");
        }

        @Override
        public void afterStubsReset() {
            events.add("afterStubsReset");
        }

        @Override
        public String getName() {
            return "test-stub-lifecycle-listener";
        }
    }
}
