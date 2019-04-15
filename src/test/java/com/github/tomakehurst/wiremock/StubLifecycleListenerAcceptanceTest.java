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
    public void callbackMethodIsCalledForStubCreation() {
        wm.stubFor(get("/test").withName("Created").willReturn(ok()));
        assertThat(listener.events.get(0), is("stubCreated, name: Created"));
    }

    @Test
    public void callbackMethodIsCalledForStubEdit() {
        UUID id = UUID.randomUUID();
        wm.stubFor(get("/test").withId(id).withName("Created").willReturn(ok()));
        wm.editStub(get("/test").withId(id).withName("Edited").willReturn(ok()));
        assertThat(listener.events.get(1), is("stubEdited, old name: Created, new name: Edited"));
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

    public static class TestStubLifecycleListener implements StubLifecycleListener {

        public List<String> events = new ArrayList<>();

        @Override
        public void stubCreated(StubMapping stub) {
            events.add("stubCreated, name: " + stub.getName());
        }

        @Override
        public void stubEdited(StubMapping oldStub, StubMapping newStub) {
            events.add("stubEdited, old name: " + oldStub.getName() + ", new name: " + newStub.getName());
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
        public String getName() {
            return "test-stub-lifecycle-listener";
        }
    }
}
