package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexPattern extends StringValuePattern {

    private final Pattern pattern;

    public RegexPattern(@JsonProperty("matches") String regex) {
        super(regex);
        pattern = Pattern.compile(regex);
    }

    public String getMatches() {
        return expectedValue;
    }

    @Override
    public MatchResult match(String value) {
        Matcher matcher = pattern.matcher(value);
        return MatchResult.of(matcher.matches());
    }

}
