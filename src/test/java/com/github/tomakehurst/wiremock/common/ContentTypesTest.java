package com.github.tomakehurst.wiremock.common;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContentTypesTest {

    @Test
    public void detectsTextTypesCorrectlyFromFileExtension() {
        assertTrue(ContentTypes.determineIsTextFromExtension("txt"));
        assertTrue(ContentTypes.determineIsTextFromExtension("json"));
        assertTrue(ContentTypes.determineIsTextFromExtension("xml"));
        assertTrue(ContentTypes.determineIsTextFromExtension("html"));
        assertTrue(ContentTypes.determineIsTextFromExtension("htm"));
        assertTrue(ContentTypes.determineIsTextFromExtension("yaml"));
        assertTrue(ContentTypes.determineIsTextFromExtension("csv"));

        assertFalse(ContentTypes.determineIsTextFromExtension("jpg"));
        assertFalse(ContentTypes.determineIsTextFromExtension("png"));
        assertFalse(ContentTypes.determineIsTextFromExtension(null));
        assertFalse(ContentTypes.determineIsTextFromExtension(""));
    }

    @Test
    public void detectsTextTypesCorrectlyFromMimeType() {
        assertTrue(ContentTypes.determineIsTextFromMimeType("text/plain"));
        assertTrue(ContentTypes.determineIsTextFromMimeType("text/html"));
        assertTrue(ContentTypes.determineIsTextFromMimeType("application/json"));
        assertTrue(ContentTypes.determineIsTextFromMimeType("some-preamble; application/json"));
        assertTrue(ContentTypes.determineIsTextFromMimeType("application/blah.something+json"));
        assertTrue(ContentTypes.determineIsTextFromMimeType("application/xml"));
        assertTrue(ContentTypes.determineIsTextFromMimeType("text/xml"));
        assertTrue(ContentTypes.determineIsTextFromMimeType("text/xml; utf-8"));
        assertTrue(ContentTypes.determineIsTextFromMimeType("application/csv"));
        assertTrue(ContentTypes.determineIsTextFromMimeType("application/x-www-form-urlencoded"));

        assertFalse(ContentTypes.determineIsTextFromMimeType("application/octet-stream"));
        assertFalse(ContentTypes.determineIsTextFromMimeType("image/jpeg"));
        assertFalse(ContentTypes.determineIsTextFromMimeType("application/pdf"));
    }

    @Test
    public void detectsTextTypesCorrectlyFromExtensionOrMimeType() {
        assertTrue(ContentTypes.determineIsText("txt", "text/plain"));
        assertTrue(ContentTypes.determineIsText("xml", ""));
        assertTrue(ContentTypes.determineIsText("json", null));

        assertFalse(ContentTypes.determineIsText("png", null));
        assertFalse(ContentTypes.determineIsText(null, "image/jpeg"));
    }
}
