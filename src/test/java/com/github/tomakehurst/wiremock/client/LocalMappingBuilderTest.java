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