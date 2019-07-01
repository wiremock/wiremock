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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.AccessControlException;

import static org.junit.Assert.*;

public class SystemEnvHelperTest {

    private SystemEnvHelper helper;

    @Before
    public void init() {
        helper = new SystemEnvHelper();

        LocalNotifier.set(new ConsoleNotifier(true));
    }

    @Test
    public void getExistingEnvironmentVariableShouldNotNull() throws Exception {
        String output = render("OS");
        assertNotNull(output);
        assertTrue(output.length() > 0);
    }

    @Test
    public void getNonExistingEnvironmentVariableShouldNull() throws Exception {
        String output = render("NON_EXISTING_VAR");
        assertNull(output);
    }

    @Test
    public void getForbiddenEnvironmentVariableShouldReturnError() throws Exception {
        System.setSecurityManager(new SecurityManager() {
            public void checkSecurityAccess(String target) {
                if (StringUtils.equals(target, "TEST_VAR"))
                    throw new AccessControlException("Access denied");
            }
        });

        String value = render("TEST_VAR");
        assertEquals("[ERROR: Access to variable TEST_VAR is denied]", value);
    }

    private String render(String variableName) throws IOException {
        return helper.apply(variableName,
                new Options.Builder(null, null, null, null, null)
                        .build()
        );
    }

}
