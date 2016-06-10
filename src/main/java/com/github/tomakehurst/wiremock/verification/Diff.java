package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

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
        builder.add(new Section<>(requestPattern.getUrlMatcher(), request.getUrl(), requestPattern.getUrlMatcher().getExpected()));

        for (HttpHeader header: request.getHeaders().all()) {
            MultiValuePattern headerPattern = requestPattern.getHeaders().get(header.key());
            String printedPatternValue = header.key() + ": " + headerPattern.getExpected();
            builder.add(new Section<>(headerPattern, header, printedPatternValue));
        }

        ImmutableList<Section<?>> sections = from(builder.build()).filter(SHOULD_BE_INCLUDED).toList();

        String expected = Joiner.on("\n").join(
            from(sections).transform(EXPECTED)
        );
        String actual = Joiner.on("\n").join(
            from(sections).transform(ACTUAL)
        );

        return sections.isEmpty() ? "" : junitStyleDiffMessage(expected, actual);
    }

    public static String junitStyleDiffMessage(Object expected, Object actual) {
        return "\n" +
            "Expected: is \"\n" +
            expected +
            "\"\n" +
            "     but: was \"\n" +
            actual +
            "\"\n\n";
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
