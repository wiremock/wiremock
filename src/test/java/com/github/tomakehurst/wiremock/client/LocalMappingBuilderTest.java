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
package com.github.tomakehurst.wiremock.client;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LocalMappingBuilderTest {
    private Mockery context = new Mockery();

    @Test
    public void callingStandardMappingMethodReturnsLocalMappingBuilder() {
        final LocalMappingBuilder mockBuilder = context.mock(LocalMappingBuilder.class);
        context.checking(new Expectations() {{
            oneOf(mockBuilder).willReturn(null);
        }});

        // No assertions necessary: we're just checking that the compiler agrees with the typing - i.e. we're getting
        // a LocalMappingBuilder, not a RemoteMappingBuilder
        LocalMappingBuilder resultingBuilder = mockBuilder.willReturn(null);
    }

    @Test
    public void callingScenarioMappingMethodReturnsScenarioMappingBuilder() {
        final LocalMappingBuilder mockBuilder = context.mock(LocalMappingBuilder.class);
        context.checking(new Expectations() {{
            oneOf(mockBuilder).inScenario("foo");
        }});

        // No assertions necessary: we're just checking that the compiler agrees with the typing
        ScenarioMappingBuilder resultingBuilder = mockBuilder.inScenario("foo");
    }

    @Test
    public void localMappingBuilderSpecifiesSameInterfaceAsRemoteMappingBuilder() {
        Method[] localMethods = LocalMappingBuilder.class.getDeclaredMethods();
        Method[] remoteMethods = RemoteMappingBuilder.class.getDeclaredMethods();

        assertThat("LocalMappingBuilder and RemoteMappingBuilder must declare the same methods",
                localMethods.length, is(remoteMethods.length));

        // Note: the following assumes the methods are in the same order, which isn't strictly necessary
        for (int i = 0; i < localMethods.length; i++) {
            Method localMethod = localMethods[i];
            Method remoteMethod = remoteMethods[i];

            assertThat(localMethod.getName(), is(remoteMethod.getName()));
            assertThat(localMethod.getParameterTypes(), is(remoteMethod.getParameterTypes()));
        }
    }
}