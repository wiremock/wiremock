package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NotPattern extends StringValuePattern{

    private StringValuePattern unexpectedPattern;

    public NotPattern(@JsonProperty("not") StringValuePattern unexpectedPattern) {
        super(unexpectedPattern.expectedValue);
        this.unexpectedPattern = unexpectedPattern;
    }

    public StringValuePattern getNot(){
        return unexpectedPattern;
    }

    @Override
    public MatchResult match(String value) {
        return invert(unexpectedPattern.match(value));
    }

    private MatchResult invert(final MatchResult matchResult){
        return new MatchResult() {
            @Override
            public boolean isExactMatch() {
                return !matchResult.isExactMatch();
            }

            @Override
            public double getDistance() {
                return 1.0 - matchResult.getDistance();
            }
        };
    }
}
