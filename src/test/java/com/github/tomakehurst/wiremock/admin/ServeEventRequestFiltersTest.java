package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.ServeEventRequestFilters;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.junit.Assert.*;

public class ServeEventRequestFiltersTest {
    @Test
    public void applyWithUniversalRequestPattern() {
        ServeEventRequestFilters filters = new ServeEventRequestFilters(RequestPattern.ANYTHING);
        assertTrue(filters.apply(toServeEvent(mockRequest())));
    }

    @Test
    public void applyWithUrlPattern() {
        ServeEventRequestFilters filters = new ServeEventRequestFilters(newRequestPattern(GET, anyUrl()).build());
        MockRequest request = mockRequest().method(GET).url("/foo");

        assertTrue(filters.apply(toServeEvent(request)));
        assertTrue(filters.apply(toServeEvent(request.url("/bar"))));
        assertFalse(filters.apply(toServeEvent(request.method(POST))));
    }

    @Test
    public void applyWithMethodAndUrlPattern() {
        ServeEventRequestFilters filters = new ServeEventRequestFilters(newRequestPattern(GET, urlEqualTo("/foo")).build());
        MockRequest request = mockRequest().method(GET).url("/foo");

        assertTrue(filters.apply(toServeEvent(request)));
        assertFalse(filters.apply(toServeEvent(request.url("/bar"))));
        assertFalse(filters.apply(toServeEvent(request.method(POST))));
    }

    private ServeEvent toServeEvent(MockRequest request) {
        return ServeEvent.forUnmatchedRequest(request.asLoggedRequest());
    }
}
