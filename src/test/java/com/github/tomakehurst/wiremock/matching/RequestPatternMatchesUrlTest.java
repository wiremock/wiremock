package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Test;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class RequestPatternMatchesUrlTest {

    @Test
    public void matchesByUrl() {
        assertTrue(matchesOnUrl("/a/b/c", "/a/b/c"));
    }

    @Test
    public void noMatchByUrl() {
        assertFalse(matchesOnUrl("/a/b/c", "/a/b/d"));
    }

    @Test
    public void matchesByQueryParam() {
        assertTrue(matchesOnUrl("/a/b/c?x=1&y=2", "/a/b/c?x=1&y=2"));
    }

    @Test
    public void noMatchByQueryParam() {
        assertFalse(matchesOnUrl("/a/b/c?x=1&y=2", "/a/b/c?x=2&y=1"));
    }

    @Test
    public void matchOnAdditionalQueryParam() {
        assertTrue(matchesOnUrl("/a/b/c?x=1", "/a/b/c?x=1&y=2"));
    }

    @Test
    public void noMatchOnMissingQueryParam() {
        assertFalse(matchesOnUrl("/a/b/c?x=1&y=2", "/a/b/c?x=1"));
    }

    private static boolean matchesOnUrl(String expected, String actual) {
        URI uri = URI.create(expected);
        String path = uri.getRawPath();
        Map<String, ValuePattern> params = new HashMap<String, ValuePattern>();
        for (Map.Entry<String, QueryParameter> e: Urls.splitQuery(uri).entrySet()) {
            params.put(e.getKey(), equalTo(e.getValue().firstValue()).asValuePattern());
        }
        RequestPattern reqPattern = new RequestPattern(RequestMethod.GET, null, new HashMap<String, ValuePattern>(), params);
        reqPattern.setUrlPath(path);
        LoggedRequest req = new LoggedRequest(actual, String.format("http://127.0.0.1:80%s", actual), RequestMethod.GET, HttpHeaders.noHeaders(), "", false, new Date());
        return reqPattern.isMatchedBy(req);
    }

}
