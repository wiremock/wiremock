package com.github.tomakehurst.wiremock.matching;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexPattern extends StringValuePattern {

    private final Pattern pattern;

    public RegexPattern(String regex) {
        super(regex);
        pattern = Pattern.compile(regex);
    }

    @Override
    public MatchResult match(String value) {
        Matcher matcher = pattern.matcher(value);
        return MatchResult.of(matcher.matches());
    }
}
