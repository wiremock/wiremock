package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.ResponseDefinitionBodyMatcherDeserializer;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ResponseDefinitionBodyMatcherDeserializerTest {
    @Test
    public void correctlyParsesFileSize() {
        final Map<String, Long> testCases = ImmutableMap.<String, Long>builder()
            .put("100", 100L)
            .put("1KB", 1024L)
            .put("1 kb", 1024L)
            .put("1024 K", 1024L * 1024)
            .put("10 Mb", 10L * 1024 * 1024)
            .put("10.5 GB", Math.round(10.5 * 1024 * 1024 * 1024))
            .build();

        for (String input : testCases.keySet()) {
            Long expected = testCases.get(input);
            Long actual = ResponseDefinitionBodyMatcherDeserializer.parseFilesize(input);
            assertEquals("Failed with " + input, expected, actual);
        }
    }
}
