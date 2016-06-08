package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UrlPatternTest {

    @Test
    public void matchesExactUrlWithQuery() {
        UrlPattern urlPattern = WireMock.urlEqualTo("/my/exact/url?one=1&two=2&three=3333333");
        assertTrue(urlPattern.match( "/my/exact/url?one=1&two=2&three=3333333").isExactMatch());
        assertFalse(urlPattern.match("/my/wrong/url?one=1&three=3333333").isExactMatch());
    }

    @Test
    public void matchesOnRegexWithQuery() {
        UrlPattern urlPattern = WireMock.urlMatching("/my/([a-z]*)/url\\?one=1&two=([0-9]*)&three=3333333");
        assertTrue(urlPattern.match("/my/regex/url?one=1&two=123456&three=3333333").isExactMatch());
        assertFalse(urlPattern.match("/my/BAD/url?one=1&two=123456&three=3333333").isExactMatch());
    }

    @Test
    public void matchesExactlyOnPathOnly() {
        UrlPathPattern urlPathPattern = WireMock.urlPathEqualTo("/the/exact/path");
        assertTrue(urlPathPattern.match("/the/exact/path").isExactMatch());
        assertFalse(urlPathPattern.match("/totally/incorrect/path").isExactMatch());
    }

    @Test
    public void matchesOnPathWithRegex() {
        UrlPathPattern urlPathPattern = WireMock.urlPathMatching("/my/([a-z]*)/path");
        assertTrue(urlPathPattern.match("/my/regex/path?one=not_looked_at").isExactMatch());
        assertFalse(urlPathPattern.match("/my/12345/path").isExactMatch());
    }


}
