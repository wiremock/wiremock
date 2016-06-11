package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.*;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.FluentIterable.from;

public class Diff {

    private final RequestPattern requestPattern;
    private final Request request;
    private final boolean requestIsExpected;

    public Diff(RequestPattern requestPattern, Request request, boolean requestIsExpected) {
        this.requestPattern = requestPattern;
        this.request = request;
        this.requestIsExpected = requestIsExpected;
    }

    @Override
    public String toString() {
        ImmutableList.Builder<Section<?>> builder = ImmutableList.builder();

        builder.add(new Section<>(requestPattern.getMethod(), request.getMethod(), requestPattern.getMethod().getName()));

        builder.add(new Section<>(requestPattern.getUrlMatcher(),
            request.getUrl(),
            requestPattern.getUrlMatcher().getExpected()));

        Map<String, MultiValuePattern> headerPatterns = requestPattern.getHeaders();
        if (headerPatterns != null && !headerPatterns.isEmpty()) {
            for (String key : getCombinedHeaderKeys()) {
                HttpHeader header = request.header(key);
                MultiValuePattern headerPattern = headerPatterns.get(header.key());
                String printedPatternValue = header.key() + ": " + headerPattern.getExpected();
                builder.add(new Section<>(headerPattern, header, printedPatternValue));
            }
        }

        List<StringValuePattern> bodyPatterns = requestPattern.getBodyPatterns();
        if (bodyPatterns != null && !bodyPatterns.isEmpty()) {
            for (StringValuePattern pattern: bodyPatterns) {
                String body = pattern.getClass().equals(EqualToJsonPattern.class) ?
                    Json.prettyPrint(request.getBodyAsString()) :
                    request.getBodyAsString();
                builder.add(new Section<>(pattern, body, pattern.getExpected()));
            }
        }

        ImmutableList<Section<?>> sections =
            from(builder.build())
            .filter(SHOULD_BE_INCLUDED)
            .toList();

        String expected = Joiner.on("\n")
            .join(from(sections).transform(EXPECTED));
        String actual = Joiner.on("\n")
            .join(from(sections).transform(ACTUAL));

        return sections.isEmpty() ? "" : junitStyleDiffMessage(expected, actual);
    }

    private Set<String> getCombinedHeaderKeys() {
        return ImmutableSet.<String>builder()
            .addAll(request.getAllHeaderKeys())
            .addAll(fromNullable(requestPattern.getHeaders())
                .or(Collections.<String, MultiValuePattern>emptyMap()).keySet())
            .build();
    }

    public static String junitStyleDiffMessage(Object expected, Object actual) {
        return "\n" +
            "Expected: " +
            expected +
            "\n" +
            "     but: was " +
            actual +
            "\n\n";
    }

    private class Section<V> {
        private final ValueMatcher<V> pattern;
        private final V value;
        private final String printedPatternValue;

        public Section(ValueMatcher<V> pattern, V value, String printedPatternValue) {
            this.pattern = pattern;
            this.value = value;
            this.printedPatternValue = printedPatternValue;
        }

        public Object getExpected() {
            return requestIsExpected ? value : printedPatternValue;
        }

        public Object getActual() {
            return requestIsExpected ? printedPatternValue : value;
        }

        public boolean shouldBeIncluded() {
            return !pattern.match(value).isExactMatch();
        }
    }

    private static Predicate<Section<?>> SHOULD_BE_INCLUDED = new Predicate<Section<?>>() {
        @Override
        public boolean apply(Section<?> section) {
            return section.shouldBeIncluded();
        }
    };

    private static Function<Section<?>, Object> EXPECTED = new Function<Section<?>, Object>() {
        @Override
        public Object apply(Section<?> input) {
            return input.getExpected();
        }
    };

    private static Function<Section<?>, Object> ACTUAL = new Function<Section<?>, Object>() {
        @Override
        public Object apply(Section<?> input) {
            return input.getActual();
        }
    };
}
