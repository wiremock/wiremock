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

import com.github.tomakehurst.wiremock.extension.GlobalSettingsListener;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GlobalSettingsListenerExtensionTest {

    TestGlobalSettingsListener listener = new TestGlobalSettingsListener();

    @Rule
    public WireMockRule wm = new WireMockRule(options()
            .dynamicPort()
            .extensions(listener));

    @Before
    public void init() {
        listener.events.clear();
    }

    @Test
    public void callsListenerMethodWithBeforeAndAfterStatesWhenSettingsUpdated() {
        wm.updateGlobalSettings(GlobalSettings.builder()
                .fixedDelay(100)
                .build());

        wm.updateGlobalSettings(GlobalSettings.builder()
                .fixedDelay(200)
                .build());

        assertThat(listener.events.size(), is(2));
        assertThat(listener.events.get(1), is("globalSettingsUpdated, old: 100, new: 200"));
    }

    public static class TestGlobalSettingsListener implements GlobalSettingsListener {

        public List<String> events = new ArrayList<>();

        @Override
        public String getName() {
            return "test-settings-listener";
        }

        @Override
        public void globalSettingsUpdated(GlobalSettings oldSettings, GlobalSettings newSettings) {
            events.add("globalSettingsUpdated, old: " + oldSettings.getFixedDelay() + ", new: " + newSettings.getFixedDelay());
        }
    }
}
