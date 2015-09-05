/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatcherObserver;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternMatcher;
import com.github.tomakehurst.wiremock.matching.ValuePattern;
import com.google.common.collect.ImmutableMap;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static com.github.tomakehurst.wiremock.http.RequestMethod.*;
import static com.github.tomakehurst.wiremock.matching.ValuePattern.equalTo;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JMock.class)
public class RequestPatternMatcherTest {

    private Mockery context;
    private Map<String, ValuePattern> headerPatterns;
    private Notifier notifier;
    private MatcherObserver matcherObserver;

    @Before
    public void init() {
        context = new Mockery();
        headerPatterns = newHashMap();
        notifier = context.mock(Notifier.class);
        matcherObserver = context.mock(MatcherObserver.class);
        LocalNotifier.set(notifier);
    }

    @After
    public void cleanUp() {
        LocalNotifier.set(null);
    }

    @Test
    public void matchesOnExactMethodAndUrl() {
        final Request request = aRequest(context)
                .withUrl("/some/resource/path")
                .withMethod(POST)
                .build();
        final RequestPattern requestPattern = new RequestPattern(RequestMethod.POST, "/some/resource/path");
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(true, true, true, true, true, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertTrue(requestPatternMatcher.matches(request, requestPattern));
    }

    @Test
    public void shouldNotMatchWhenMethodIsSameButUrlIsDifferent() {
        final RequestPattern requestPattern = new RequestPattern(RequestMethod.POST, "/some/resource/path");
        final Request request = aRequest(context)
                .withUrl("/wrong/path")
                .withMethod(POST)
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(false, false, true, true, true, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertFalse(requestPatternMatcher.matches(request, requestPattern));
    }

    @Test
    public void shouldMatchWhenSpecifiedHeadersArePresent() {
        headerPatterns.put("Accept", equalTo("text/plain"));
        headerPatterns.put("Content-Type", equalTo("application/json"));
        final RequestPattern requestPattern = new RequestPattern(RequestMethod.GET, "/header/dependent/resource", headerPatterns);

        final Request request = aRequest(context)
                .withUrl("/header/dependent/resource")
                .withMethod(GET)
                .withHeader("Accept", "text/plain")
                .withHeader("Content-Type", "application/json")
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(true, true, true, true, true, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertTrue(requestPatternMatcher.matches(request, requestPattern));
    }

    @Test
    public void shouldNotMatchWhenASpecifiedHeaderIsAbsent() {
        ignoringNotifier();

        headerPatterns.put("Accept", equalTo("text/plain"));
        headerPatterns.put("Content-Type", equalTo("application/json"));
        final RequestPattern requestPattern = new RequestPattern(RequestMethod.GET, "/header/dependent/resource", headerPatterns);

        final Request request = aRequest(context)
                .withUrl("/header/dependent/resource")
                .withMethod(GET)
                .withHeader("Accept", "text/plain")
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(false, true, true, true, false, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertFalse(requestPatternMatcher.matches(request, requestPattern));
    }

    @Test
    public void shouldNotMatchWhenASpecifiedHeaderHasAnIncorrectValue() {
        ignoringNotifier();

        headerPatterns.put("Accept", equalTo("text/plain"));
        headerPatterns.put("Content-Type", equalTo("application/json"));
        final RequestPattern requestPattern = new RequestPattern(RequestMethod.GET, "/header/dependent/resource", headerPatterns);

        final Request request = aRequest(context)
                .withUrl("/header/dependent/resource")
                .withMethod(GET)
                .withHeader("Accept", "text/plain")
                .withHeader("Content-Type", "text/xml")
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(false, true, true, true, false, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertFalse(requestPatternMatcher.matches(request, requestPattern));
    }

    @Test
    public void shouldMatchHeaderWithMultipleValues() {
        ignoringNotifier();

        final RequestPattern requestPattern1 = new RequestPattern(RequestMethod.GET,
                "/multi/header",
                ImmutableMap.of("X-Multi", equalTo("one")));
        final RequestPattern requestPattern2 = new RequestPattern(RequestMethod.GET,
                "/multi/header",
                ImmutableMap.of("X-Multi", equalTo("two")));

        final Request request = aRequest(context)
                .withUrl("/multi/header")
                .withMethod(GET)
                .withHeader("X-Multi", "one")
                .withHeader("X-Multi", "two")
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(true, true, true, true, true, true, true, request, requestPattern1);
            one(matcherObserver).onMatchingResult(true, true, true, true, true, true, true, request, requestPattern2);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertTrue("Request should match request pattern with header X-Multi:one", requestPatternMatcher.matches(request, requestPattern1));
        assertTrue("Request should match request pattern with header X-Multi:two", requestPatternMatcher.matches(request, requestPattern2));
    }

    @Test
    public void shouldMatchUrlPatternWithRegexes() {
        final RequestPattern requestPattern = new RequestPattern(RequestMethod.GET);
        requestPattern.setUrlPattern("/resource/(.*?)/subresource");

        final Request request = aRequest(context)
                .withUrl("/resource/1234-abcd/subresource")
                .withMethod(GET)
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(true, true, true, true, true, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertTrue(requestPatternMatcher.matches(request, requestPattern));
    }

    @Test
    public void shouldNotMatchUrlWhenUsingRegexButCandidateIsNotMatch() {
        final RequestPattern requestPattern = new RequestPattern(RequestMethod.GET);
        requestPattern.setUrlPattern("/resource/([A-Z]+?)/subresource");

        final Request request = aRequest(context)
                .withUrl("/resource/12340987/subresource")
                .withMethod(GET)
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(false, false, true, true, true, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertFalse(requestPatternMatcher.matches(request, requestPattern));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotPermitBothUrlAndUrlPattern() {
        final RequestPattern requestPattern = new RequestPattern();
        requestPattern.setUrlPattern("/(.*?)");
        requestPattern.setUrl("/some/url");

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertTrue(requestPatternMatcher.matches(aRequest(context).build(), requestPattern));
    }

    @Test
    public void shouldMatchUrlPathPatternWithRegexes() {
        final RequestPattern requestPattern = new RequestPattern(RequestMethod.GET);
        requestPattern.setUrlPathPattern("/resource/(.*?)/subresource");

        final Request request = aRequest(context)
                .withUrl("/resource/1234-abcd/subresource")
                .withQueryParameter("foo", "bar")
                .withMethod(GET)
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(true, true, true, true, true, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertTrue(requestPatternMatcher.matches(request, requestPattern));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotPermitBothUrlPathAndUrlPathPattern() {
        final RequestPattern requestPattern = new RequestPattern();
        requestPattern.setUrlPathPattern("/(.*?)");
        requestPattern.setUrlPath("/some/url");

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertTrue(requestPatternMatcher.matches(aRequest(context).build(), requestPattern));
    }

    private static final String XML_SAMPLE =
            "<document>							\n" +
                    "	<important>Value</important>	\n" +
                    "</document>		  				";

    @Test
    public void shouldMatchOnBodyPattern() {
        final RequestPattern requestPattern = new RequestPattern(GET, "/with/body");
        requestPattern.setBodyPatterns(asList(ValuePattern.matches(".*<important>Value</important>.*")));

        final Request request = aRequest(context)
                .withUrl("/with/body")
                .withMethod(GET)
                .withBody(XML_SAMPLE)
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(true, true, true, true, true, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertTrue(requestPatternMatcher.matches(request, requestPattern));
    }

    @Test
    public void shouldNotMatchWhenBodyDoesNotMatchPattern() {
        ignoringNotifier();

        final RequestPattern requestPattern = new RequestPattern(GET, "/with/body");
        requestPattern.setBodyPatterns(asList(ValuePattern.matches(".*<important>Value</important>.*")));

        final Request request = aRequest(context)
                .withUrl("/with/body")
                .withMethod(GET)
                .withBody("<important>Wrong value</important>")
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(false, true, true, true, true, true, false, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertFalse(requestPatternMatcher.matches(request, requestPattern));
    }

    @Test
    public void shouldMatchAnyMethod() {
        final RequestPattern requestPattern = new RequestPattern(ANY, "/any/method");

        for (RequestMethod method : RequestMethod.values()) {
            context = new Mockery();
            matcherObserver = context.mock(MatcherObserver.class);
            final Request request = aRequest(context)
                    .withUrl("/any/method")
                    .withMethod(method)
                    .build();
            context.checking(new Expectations() {{
                one(matcherObserver).onMatchingResult(true, true, true, true, true, true, true, request, requestPattern);
            }});

            RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
            assertTrue("Method in request pattern is ANY so any method should match", requestPatternMatcher.matches(request, requestPattern));
        }
    }

    @Test
    public void supportsMatchingOnAbsentHeader() {
        ignoringNotifier();

        final RequestPattern requestPattern = new RequestPattern(GET, "/without/header");
        requestPattern.addHeader("X-My-Header", ValuePattern.absent());
        final Request request = aRequest(context)
                .withUrl("/without/header")
                .withMethod(GET)
                .withHeader("X-Another-Header", "value")
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(true, true, true, true, true, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertTrue("Request is not a match for the request pattern", requestPatternMatcher.matches(request, requestPattern));
    }

    @Test
    public void shouldFailMatchWhenRequiredAbsentHeaderIsPresent() {
        ignoringNotifier();

        final RequestPattern requestPattern = new RequestPattern(GET, "/without/header/fail");
        requestPattern.addHeader("X-My-Header", ValuePattern.absent());
        final Request request = aRequest(context)
                .withUrl("/without/header/fail")
                .withMethod(GET)
                .withHeader("X-My-Header", "value")
                .build();
        context.checking(new Expectations() {{
            one(matcherObserver).onMatchingResult(false, true, true, false, true, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        assertFalse("Request is a match for the request pattern and should not be", requestPatternMatcher.matches(request, requestPattern));
    }

    @Test
    public void shouldLogMessageIndicatingFailedMethodMatch() {
        final RequestPattern requestPattern = new RequestPattern(POST, "/for/logging");

        final Request request = aRequest(context)
                .withUrl("/for/logging")
                .withMethod(GET)
                .build();
        context.checking(new Expectations() {{
            one(notifier).info("URL /for/logging is match, but method GET is not");
            one(matcherObserver).onMatchingResult(false, true, false, true, true, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        requestPatternMatcher.matches(request, requestPattern);
    }

    @Test
    public void shouldLogMessageIndicatingFailedHeaderMatch() {
        final RequestPattern requestPattern = new RequestPattern(POST, "/for/logging");
        ValuePattern headerPattern = new ValuePattern();
        headerPattern.setEqualTo("text/xml");
        requestPattern.addHeader("Content-Type", headerPattern);

        final Request request = aRequest(context)
                .withUrl("/for/logging")
                .withMethod(POST)
                .withHeader("Content-Type", "text/plain")
                .build();

        context.checking(new Expectations() {{
            one(notifier).info("URL /for/logging is match, but header Content-Type is not. For a match, value should equal text/xml");
            one(matcherObserver).onMatchingResult(false, true, true, true, false, true, true, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        requestPatternMatcher.matches(request, requestPattern);
    }

    @Test
    public void shouldLogMessageIndicatingFailedBodyMatch() {
        final RequestPattern requestPattern = new RequestPattern(POST, "/for/logging");
        requestPattern.setBodyPatterns(singletonList(ValuePattern.matches("Expected content")));

        final Request request = aRequest(context)
                .withUrl("/for/logging")
                .withMethod(POST)
                .withBody("Actual Content")
                .build();
        context.checking(new Expectations() {{
            one(notifier).info("URL /for/logging is match, but body is not: Actual Content");
            one(matcherObserver).onMatchingResult(false, true, true, true, true, true, false, request, requestPattern);
        }});

        RequestPatternMatcher requestPatternMatcher = new RequestPatternMatcher(matcherObserver);
        requestPatternMatcher.matches(request, requestPattern);
    }

    private void ignoringNotifier() {
        context.checking(new Expectations() {{
            ignoring(notifier);
        }});
    }

}
