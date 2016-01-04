package com.github.tomakehurst.wiremock.client;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

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
}