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
package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.common.*;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class OptionsTest {

    @Test
    public void proxySettingsEqualsIsCorrect() {
        EqualsVerifier.forClass(ProxySettings.class).allFieldsShouldBeUsed().verify();
    }

    @Test
    public void fileSourceEqualsIsCorrect() {
        EqualsVerifier.forClass(SingleRootFileSource.class)
                .withRedefinedSuperclass()
                .allFieldsShouldBeUsed()
                .verify();
        EqualsVerifier.forClass(ServletContextFileSource.class)
                .withRedefinedSuperclass()
                .allFieldsShouldBeUsedExcept("rootDirectory")
                .verify();
    }

    @Test
    public void notifierEqualsIsCorrect() {
        EqualsVerifier.forClass(Slf4jNotifier.class).verify();
        EqualsVerifier.forClass(ConsoleNotifier.class).verify();
    }

    @Test
    public void wireMockConfigurationEqualsIsCorrect() {
        EqualsVerifier.forClass(WireMockConfiguration.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .allFieldsShouldBeUsedExcept("httpServerFactory")
                .verify();
    }


}
