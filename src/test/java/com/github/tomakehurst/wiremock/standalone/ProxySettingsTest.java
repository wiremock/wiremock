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
package com.github.tomakehurst.wiremock.standalone;

import com.github.tomakehurst.wiremock.common.ProxySettings;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ProxySettingsTest {

    @Test(expected=IllegalArgumentException.class)
    public void throwsExceptionWhenHostPartNotSpecified() {
        ProxySettings.fromString(":8090");
    }

    @Test
    public void defaultsToPort80() {
        assertThat(ProxySettings.fromString("myhost.com").port(), is(80));
    }

    @Test
    public void parsesHostAndPortCorrectly() {
        ProxySettings settings = ProxySettings.fromString("some.host.org:8888");
        assertThat(settings.host(), is("some.host.org"));
        assertThat(settings.port(), is(8888));
    }
}
