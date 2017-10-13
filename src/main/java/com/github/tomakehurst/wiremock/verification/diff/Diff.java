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
package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Xml;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.*;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.FluentIterable.from;

public class Diff {

    private final String stubMappingName;
    private final RequestPattern requestPattern;
    private final Request request;

    public Diff(RequestPattern expected, Request actual) {
        this.requestPattern = expected;
        this.request = actual;
        this.stubMappingName = null;
    }

    public Diff(StubMapping expected, Request actual) {
        this.requestPattern = expected.getRequest();
        this.request = actual;
        this.stubMappingName = expected.getName();
    }

    @Override
    public String toString() {
        if (requestPattern.hasCustomMatcher()) {
            return "(Request pattern had a custom matcher so no diff can be shown)";
        }

        List<DiffSection<?>> sections = getSections();

        String expected = Joiner.on("\n")
            .join(from(sections).transform(EXPECTED));
        String actual = Joiner.on("\n")
            .join(from(sections).transform(ACTUAL));

        return sections.isEmpty() ? "" : junitStyleDiffMessage(expected, actual);
    }

    public List<DiffSection<?>> getSections() {
        ImmutableList.Builder<DiffSection<?>> builder = ImmutableList.builder();

        DiffSection<RequestMethod> methodSection = new DiffSection<>("HTTP method", requestPattern.getMethod(), request.getMethod(), requestPattern.getMethod().getName());
        builder.add(methodSection);

        DiffSection<String> urlSection = new DiffSection<>("URL", requestPattern.getUrlMatcher(),
            request.getUrl(),
            requestPattern.getUrlMatcher().getExpected());
        builder.add(urlSection);

        if (methodSection.shouldBeIncluded() || urlSection.shouldBeIncluded()) {
            builder.add(SPACER);
        }

        boolean anyHeaderSections = false;
        Map<String, MultiValuePattern> headerPatterns = requestPattern.combineBasicAuthAndOtherHeaders();
        if (headerPatterns != null && !headerPatterns.isEmpty()) {
            for (String key : headerPatterns.keySet()) {
                HttpHeader header = request.header(key);
                MultiValuePattern headerPattern = headerPatterns.get(header.key());
                String printedPatternValue = header.key() + ": " + headerPattern.getExpected();
                DiffSection<MultiValue> section = new DiffSection<>("Header", headerPattern, header, printedPatternValue);
                if (section.shouldBeIncluded()) {
                    anyHeaderSections = true;
                }
                builder.add(section);
            }
        }

        if (anyHeaderSections) {
            builder.add(SPACER);
        }

        boolean anyCookieSections = false;
        if (requestPattern.getCookies() != null) {
            Map<String, Cookie> cookies = firstNonNull(request.getCookies(), Collections.<String, Cookie>emptyMap());
            for (Map.Entry<String, StringValuePattern> entry: requestPattern.getCookies().entrySet()) {
                String key = entry.getKey();
                StringValuePattern pattern = entry.getValue();
                Cookie cookie = firstNonNull(cookies.get(key), Cookie.absent());
                DiffSection<String> section = new DiffSection<>(
                    "Cookie",
                    pattern,
                    cookie.isPresent() ? "Cookie: " + key + "=" + cookie.getValue() : "",
                    "Cookie: " + key + "=" + pattern.getValue()
                );
                builder.add(section);
                anyCookieSections = true;
            }
        }

        if (anyCookieSections) {
            builder.add(SPACER);
        }

        List<ContentPattern<?>> bodyPatterns = requestPattern.getBodyPatterns();
        if (bodyPatterns != null && !bodyPatterns.isEmpty()) {
            for (ContentPattern<?> pattern: bodyPatterns) {
                String body = formatIfJsonOrXml(pattern);
                if (StringValuePattern.class.isAssignableFrom(pattern.getClass())) {
                    StringValuePattern stringValuePattern = (StringValuePattern) pattern;
                    builder.add(new DiffSection<>("Body", stringValuePattern, body, pattern.getExpected()));
                } else {
                    BinaryEqualToPattern nonStringPattern = (BinaryEqualToPattern) pattern;
                    builder.add(new DiffSection<>("Body", nonStringPattern, body.getBytes(), pattern.getExpected()));
                }

            }
        }

        return builder.build();
    }

    public String getStubMappingName() {
        return stubMappingName;
    }

    private String formatIfJsonOrXml(ContentPattern<?> pattern) {
        try {
            return pattern.getClass().equals(EqualToJsonPattern.class) ?
                Json.prettyPrint(request.getBodyAsString()) :
                pattern.getClass().equals(EqualToXmlPattern.class) ?
                    Xml.prettyPrint(request.getBodyAsString()) :
                    pattern.getClass().equals(BinaryEqualToPattern.class) ?
                        BaseEncoding.base64().encode(request.getBody()):
                        request.getBodyAsString();
        } catch (Exception e) {
            return request.getBodyAsString();
        }
    }

    public static String junitStyleDiffMessage(Object expected, Object actual) {
        return String.format(" expected:<\n%s> but was:<\n%s>", expected, actual);
    }

    final DiffSection<String> SPACER = new DiffSection<>("", new EqualToPattern(""), "", "");

    private static Function<DiffSection<?>, Object> EXPECTED = new Function<DiffSection<?>, Object>() {
        @Override
        public Object apply(DiffSection<?> input) {
            return input.getExpected();
        }
    };

    private static Function<DiffSection<?>, Object> ACTUAL = new Function<DiffSection<?>, Object>() {
        @Override
        public Object apply(DiffSection<?> input) {
            return input.getActual();
        }
    };
}
