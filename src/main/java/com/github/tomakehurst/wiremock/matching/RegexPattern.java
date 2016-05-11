package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexPattern extends AbstractRegexPattern {

    public RegexPattern(@JsonProperty("matches") String regex) {
        super(regex);
    }

    public String getMatches() {
        return expectedValue;
    }

}
