package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.http.MultiValue;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;

import static java.util.Collections.min;

public class MultiValuePattern implements ValueMatcher<MultiValue> {

    private final Optional<StringValuePattern> valuePattern;

    public MultiValuePattern(Optional<StringValuePattern> valuePattern) {
        this.valuePattern = valuePattern;
    }

    public static MultiValuePattern of(StringValuePattern valuePattern) {
        return new MultiValuePattern(Optional.of(valuePattern));
    }

    public static MultiValuePattern absent() {
        return new MultiValuePattern(Optional.<StringValuePattern>absent());
    }

    @Override
    public MatchResult match(MultiValue header) {
        if (valuePattern.isPresent() && header.isPresent()) {
            return getBestMatch(valuePattern.get(), header.values());
        } else {
            return MatchResult.of(valuePattern.isPresent() == header.isPresent());
        }
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
}
