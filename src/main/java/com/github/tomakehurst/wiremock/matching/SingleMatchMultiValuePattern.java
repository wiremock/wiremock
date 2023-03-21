package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.google.common.base.Objects;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.min;
import static java.util.Collections.singletonList;

@JsonDeserialize(as = SingleMatchMultiValuePattern.class)
public class SingleMatchMultiValuePattern extends MultiValuePattern {

    private final StringValuePattern valuePattern;


    @JsonCreator
    public SingleMatchMultiValuePattern(StringValuePattern valuePattern) {
        this.valuePattern = valuePattern;
    }

    private static MatchResult getBestMatch(final StringValuePattern valuePattern, List<String> values) {
        List<MatchResult> allResults = values.stream().map(valuePattern::match).collect(Collectors.toList());

        return min(allResults, Comparator.comparingDouble(MatchResult::getDistance));
    }

    @Override
    public MatchResult match(MultiValue multiValue) {
        List<String> values = multiValue.isPresent() ? multiValue.values() : singletonList(null);
        return getBestMatch(valuePattern, values);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleMatchMultiValuePattern that = (SingleMatchMultiValuePattern) o;
        return Objects.equal(valuePattern, that.valuePattern);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(valuePattern);
    }
}