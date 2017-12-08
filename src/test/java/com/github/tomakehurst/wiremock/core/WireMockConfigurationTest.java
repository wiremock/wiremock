package com.github.tomakehurst.wiremock.core;

import com.google.common.base.Optional;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WireMockConfigurationTest {

    @Test
    public void testJettyStopTimeout() {
        Long expectedStopTimeout = 500L;
        WireMockConfiguration wireMockConfiguration = WireMockConfiguration.wireMockConfig().jettyStopTimeout(expectedStopTimeout);
        Optional<Long> jettyStopTimeout = wireMockConfiguration.jettySettings().getStopTimeout();

        assertThat(jettyStopTimeout.isPresent(), is(true));
        assertThat(jettyStopTimeout.get(), is(expectedStopTimeout));
    }

    @Test
    public void testJettyStopTimeoutNotSet() {
        WireMockConfiguration wireMockConfiguration = WireMockConfiguration.wireMockConfig();
        Optional<Long> jettyStopTimeout = wireMockConfiguration.jettySettings().getStopTimeout();
        assertThat(jettyStopTimeout.isPresent(), is(false));
    }

    @Test
    public void shouldUseQueuedThreadPoolByDefault() {
        int maxThreads = 20;
        WireMockConfiguration wireMockConfiguration = WireMockConfiguration.wireMockConfig().containerThreads(maxThreads);

        QueuedThreadPool threadPool = (QueuedThreadPool) wireMockConfiguration.threadPoolFactory().buildThreadPool(wireMockConfiguration);

        assertThat(threadPool.getMaxThreads(), is(maxThreads));
    }
}