package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class LogicalAnd extends StringValuePattern {

    private final List<StringValuePattern> operands;

    public LogicalAnd(StringValuePattern... operands) {
        this(asList(operands));
    }

    public LogicalAnd(@JsonProperty("and") List<StringValuePattern> operands) {
        super(operands.stream()
                .findFirst()
                .map(ContentPattern::getValue)
                .orElseThrow(() -> new IllegalArgumentException("Logical AND must be constructed with at least two matchers")));
        this.operands = operands;
    }

    @Override
    public String getExpected() {
        return operands.stream()
                .map(contentPattern -> contentPattern.getName() + " " + contentPattern.getExpected())
                .collect(Collectors.joining(" AND "));
    }

    public List<StringValuePattern> getAnd() {
        return operands;
    }

    @Override
    public MatchResult match(String value) {
        return MatchResult.aggregate(
            operands.stream()
                    .map(matcher -> matcher.match(value))
                    .collect(Collectors.toList())
        );
    }
}
