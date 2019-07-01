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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SystemPropHelperTest {

    private SystemPropHelper helper;

    @Before
    public void init() {
        helper = new SystemPropHelper();

        LocalNotifier.set(new ConsoleNotifier(true));
    }

    @Test
    public void getEmptyKeyShouldReturnError() throws Exception {
        String value = render("");
        assertEquals("[ERROR: The property name cannot be empty]", value);
    }

    @Test
    public void getAllowedPropertyShouldSuccess() throws Exception {
        System.setProperty("test.key", "aaa");
        assertEquals("aaa", System.getProperty("test.key"));

        String value = render("test.key");
        assertEquals("aaa", value);
    }

    @Test
    public void getForbiddenPropertyShouldReturnError() throws Exception {
        System.setProperty("test.key", "aaa");
        System.setSecurityManager(new SecurityManager() {
            public void checkPropertyAccess(String key) {
                if (StringUtils.equals(key, "test.key"))
                    throw new AccessControlException("Access denied");
            }
        });
        String value = render("test.key");
        assertEquals("[ERROR: Access to property test.key is denied]", value);
    }

    @Test
    public void getNonExistingSystemPropertyShouldNull() throws Exception {
        String output = render("not.existing.prop");
        assertNull(output);
    }

    private String render(String variableName) throws IOException {
        return helper.apply(variableName,
                new Options.Builder(null, null, null, null, null)
                        .build()
        );
    }

}
