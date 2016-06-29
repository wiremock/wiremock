package com.github.tomakehurst.wiremock.matching;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.DOTALL;

public abstract class AbstractRegexPattern extends StringValuePattern {

    protected final Pattern pattern;

    protected AbstractRegexPattern(String regex) {
        super(regex);
        pattern = Pattern.compile(regex, DOTALL);
    }

    @Override
    public MatchResult match(String value) {
        Matcher matcher = pattern.matcher(value);
        return MatchResult.of(matcher.matches());
    }

}
