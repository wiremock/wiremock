package com.github.tomakehurst.wiremock.common;

import com.google.common.base.Optional;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JettySettingsTest {

    private static final int number = 1234;

    @Test
    public void testBuilderWithValues() {


        JettySettings.Builder builder = JettySettings.Builder.aJettySettings();
        builder.withAcceptors(number)
                .withAcceptQueueSize(number)
                .withRequestHeaderSize(number);
        JettySettings jettySettings = builder.build();

        ensurePresent(jettySettings.getAcceptors());
        ensurePresent(jettySettings.getAcceptQueueSize());
        ensurePresent(jettySettings.getRequestHeaderSize());
    }

    @Test
    public void testBuilderWithNoValues() {


        JettySettings.Builder builder = JettySettings.Builder.aJettySettings();
        JettySettings jettySettings = builder.build();

        assertFalse(jettySettings.getAcceptors().isPresent());
        assertFalse(jettySettings.getAcceptQueueSize().isPresent());
        assertFalse(jettySettings.getRequestHeaderSize().isPresent());
    }

    private void ensurePresent(Optional<Integer> optional) {
        assertTrue(optional.isPresent());
        assertEquals(new Integer(number), optional.get());
    }
}