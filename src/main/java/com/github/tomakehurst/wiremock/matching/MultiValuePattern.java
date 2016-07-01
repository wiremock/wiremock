package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;

import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;
import static java.util.Collections.min;

public class MultiValuePattern implements ValueMatcher<MultiValue> {

    private final StringValuePattern valuePattern;

    public MultiValuePattern(StringValuePattern valuePattern) {
        this.valuePattern = valuePattern;
    }

    @JsonCreator
    public static MultiValuePattern of(StringValuePattern valuePattern) {
        return new MultiValuePattern(valuePattern);
    }

    public static MultiValuePattern absent() {
        return new MultiValuePattern(WireMock.absent());
    }

    @Override
    public MatchResult match(MultiValue multiValue) {
        if (valuePattern.nullSafeIsAbsent()) {
            return MatchResult.of(!multiValue.isPresent());
        }

        if (valuePattern.isPresent() && multiValue.isPresent()) {
            return getBestMatch(valuePattern, multiValue.values());
        }

        return MatchResult.of(valuePattern.isPresent() == multiValue.isPresent());
    }

    @JsonValue
    public StringValuePattern getValuePattern() {
        return valuePattern;
    }

    @Override
    public String getName() {
        return valuePattern.getName();
    }

    @Override
    public String getExpected() {
        return valuePattern.expectedValue;
    }

    private static MatchResult getBestMatch(final StringValuePattern valuePattern, List<String> values) {
        List<MatchResult> allResults = Lists.transform(values, new Function<String, MatchResult>() {
            public MatchResult apply(String input) {
                return valuePattern.match(input);
            }
        });

        return min(allResults, new Comparator<MatchResult>() {
            public int compare(MatchResult o1, MatchResult o2) {
                return new Double(o1.getDistance()).compareTo(o2.getDistance());
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiValuePattern that = (MultiValuePattern) o;
        return Objects.equal(valuePattern, that.valuePattern);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(valuePattern);
    }
}
