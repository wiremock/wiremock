package com.github.tomakehurst.wiremock.junit5;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WireMockTest(httpsEnabled = true, httpsPort = 8766)
public class JUnitJupiterExtensionDeclarativeWithFixedHttpsPortParameterTest {

    @Test
    void runs_on_the_supplied_port(WireMockRuntimeInfo wmRuntimeInfo) {
        assertTrue(wmRuntimeInfo.isHttpsEnabled(), "Expected HTTPS to be enabled");
        assertThat(wmRuntimeInfo.getHttpsPort(), is(8766));
    }

}
