package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class LogicalOr extends StringValuePattern {

    private final List<StringValuePattern> operands;

    public LogicalOr(StringValuePattern... operands) {
        this(asList(operands));
    }

    public LogicalOr(@JsonProperty("or") List<StringValuePattern> operands) {
        super(operands.stream()
                .findFirst()
                .map(ContentPattern::getValue)
                .orElseThrow(() -> new IllegalArgumentException("Logical OR must be constructed with at least two matchers")));
        this.operands = operands;
    }

    @Override
    public String getExpected() {
        return operands.stream()
                .map(contentPattern -> contentPattern.getName() + " " + contentPattern.getExpected())
                .collect(Collectors.joining(" OR "));
    }

    public List<StringValuePattern> getOr() {
        return operands;
    }

    @Override
    public MatchResult match(String value) {
        final List<MatchResult> matchResults = operands.stream()
                .map(matcher -> matcher.match(value))
                .collect(Collectors.toList());

        return new MatchResult() {
            @Override
            public boolean isExactMatch() {
                return matchResults.stream().anyMatch(MatchResult::isExactMatch);
            }

            @Override
            public double getDistance() {
                return matchResults.stream()
                        .map(MatchResult::getDistance)
                        .sorted()
                        .findFirst().get();
            }
        };
    }
}
