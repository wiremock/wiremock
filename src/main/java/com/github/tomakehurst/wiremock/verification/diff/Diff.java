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
import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.verification.diff.SpacerLine.SPACER;
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
        return new JUnitStyleDiffRenderer().render(this);
    }

    public List<DiffLine<?>> getLines() {
        ImmutableList.Builder<DiffLine<?>> builder = ImmutableList.builder();

        DiffLine<RequestMethod> methodSection = new DiffLine<>("HTTP method", requestPattern.getMethod(), request.getMethod(), requestPattern.getMethod().getName());
        builder.add(methodSection);

        DiffLine<String> urlSection = new DiffLine<>("URL", requestPattern.getUrlMatcher(),
            request.getUrl(),
            requestPattern.getUrlMatcher().getExpected());
        builder.add(urlSection);

        builder.add(SPACER);

        boolean anyHeaderSections = false;
        Map<String, MultiValuePattern> headerPatterns = requestPattern.combineBasicAuthAndOtherHeaders();
        if (headerPatterns != null && !headerPatterns.isEmpty()) {
            anyHeaderSections = true;
            for (String key : headerPatterns.keySet()) {
                HttpHeader header = request.header(key);
                MultiValuePattern headerPattern = headerPatterns.get(header.key());
                String printedPatternValue = header.key() + ": " + headerPattern.getExpected();
                DiffLine<MultiValue> section = new DiffLine<>("Header", headerPattern, header, printedPatternValue);
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
                DiffLine<String> section = new DiffLine<>(
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
                    builder.add(new DiffLine<>("Body", stringValuePattern, body, pattern.getExpected()));
                } else {
                    BinaryEqualToPattern nonStringPattern = (BinaryEqualToPattern) pattern;
                    builder.add(new DiffLine<>("Body", nonStringPattern, body.getBytes(), pattern.getExpected()));
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


    public boolean hasCustomMatcher() {
        return requestPattern.hasCustomMatcher();
    }
}
