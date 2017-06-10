package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.ServeEventRequestFilters;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
        ServeEventRequestFilters filters = new ServeEventRequestFilters(RequestPattern.ANYTHING, null);
        assertTrue(filters.apply(toServeEvent(mockRequest())));
    }

    @Test
    public void applyWitMethodPattern() {
        ServeEventRequestFilters filters = new ServeEventRequestFilters(newRequestPattern(GET, anyUrl()).build(), null);
        MockRequest request = mockRequest().method(GET).url("/foo");

        assertTrue(filters.apply(toServeEvent(request)));
        assertTrue(filters.apply(toServeEvent(request.url("/bar"))));
        assertFalse(filters.apply(toServeEvent(request.method(POST))));
    }

    @Test
    public void applyWithIds() {
        List<UUID> ids = Arrays.asList(
            UUID.fromString("00000000-0000-0000-0000-000000000000"),
            UUID.fromString("00000000-0000-0000-0000-000000000001")
        );
        ServeEventRequestFilters filters = new ServeEventRequestFilters(null, ids);

        assertTrue(filters.apply(toServeEvent(ids.get(0))));
        assertTrue(filters.apply(toServeEvent(ids.get(1))));
        assertFalse(filters.apply(toServeEvent(UUID.fromString("00000000-0000-0000-0000-000000000002"))));
    }

    @Test
    public void applyWithMethodAndUrlPattern() {
        ServeEventRequestFilters filters = new ServeEventRequestFilters(newRequestPattern(GET, urlEqualTo("/foo")).build(), null);
        MockRequest request = mockRequest().method(GET).url("/foo");

        assertTrue(filters.apply(toServeEvent(request)));
        assertFalse(filters.apply(toServeEvent(request.url("/bar"))));
        assertFalse(filters.apply(toServeEvent(request.method(POST))));
    }

    @Test
    public void applyWithIdsAndMethodPattern() {
        MockRequest request = mockRequest().method(GET).url("/foo");
        List<UUID> ids = Arrays.asList(
            UUID.fromString("00000000-0000-0000-0000-000000000000"),
            UUID.fromString("00000000-0000-0000-0000-000000000001")
        );
        ServeEventRequestFilters filters = new ServeEventRequestFilters(
            newRequestPattern(GET, anyUrl()).build(),
            ids
        );

        assertTrue(filters.apply(toServeEvent(ids.get(0), request)));
        assertFalse(filters.apply(toServeEvent(UUID.fromString("00000000-0000-0000-0000-000000000002"), request)));
        assertFalse(filters.apply(toServeEvent(ids.get(0), request.method(POST))));
    }

    private ServeEvent toServeEvent(UUID id, MockRequest request) {
        return new ServeEvent(
            id,
            request != null ? request.asLoggedRequest() : null,
            null,
            null,
            null,
            true
        );
    }

    private ServeEvent toServeEvent(MockRequest request) {
        return toServeEvent(null, request);
    }

    private ServeEvent toServeEvent(UUID id) {
        return toServeEvent(id, null);
    }
}
