package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.recording.ResponseDefinitionBodyMatcher;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResponseDefinitionBodyMatcherTest {
    @Test
    public void noThresholds() {
        ResponseDefinitionBodyMatcher matcher = new ResponseDefinitionBodyMatcher(0, 0);
        assertFalse(matcher.match(new ResponseDefinition()).isExactMatch());
        assertTrue(matcher.match(textResponseDefinition("a")).isExactMatch());
        assertTrue(matcher.match(binaryResponseDefinition(new byte[] { 0x1 })).isExactMatch());
    }

    @Test
    public void textBodyMatchingWithThreshold() {
        ResponseDefinitionBodyMatcher matcher = new ResponseDefinitionBodyMatcher(2, 0);
        assertFalse(matcher.match(textResponseDefinition("f")).isExactMatch());
        assertFalse(matcher.match(textResponseDefinition("fo")).isExactMatch());
        assertTrue(matcher.match(textResponseDefinition("foo")).isExactMatch());
    }

    @Test
    public void binaryBodyMatchingWithThreshold() {
        ResponseDefinitionBodyMatcher matcher = new ResponseDefinitionBodyMatcher(0, 2);
        assertFalse(matcher.match(binaryResponseDefinition(new byte[] { 0x1 })).isExactMatch());
        assertFalse(matcher.match(binaryResponseDefinition(new byte[] { 0x1, 0xc })).isExactMatch());
        assertTrue(matcher.match(binaryResponseDefinition(new byte[] { 0x1, 0xc, 0xf })).isExactMatch());
    }

    private static ResponseDefinition textResponseDefinition(String body) {
        return new ResponseDefinitionBuilder()
            .withHeader("Content-Type", "text/plain")
            .withBody(body)
            .build();
    }

    private static ResponseDefinition binaryResponseDefinition(byte[] body) {
        return new ResponseDefinitionBuilder()
            .withBody(body)
            .build();
    }
}
