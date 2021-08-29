package com.github.tomakehurst.wiremock.junit5;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@WireMockTest(httpPort = 8765)
public class JUnitJupiterExtensionDeclarativeWithHttpPortParameterTest {

    @Test
    void runs_on_the_supplied_port(WireMockRuntimeInfo wmRuntimeInfo) {
        assertThat(wmRuntimeInfo.getHttpPort(), is(8765));
    }

}
