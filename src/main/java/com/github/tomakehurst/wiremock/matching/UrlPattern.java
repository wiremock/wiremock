package com.github.tomakehurst.wiremock.matching;

public class UrlPattern implements ValueMatcher<String> {

    protected final StringValuePattern testUrl;

    protected UrlPattern(StringValuePattern testUrl) {
        this.testUrl = testUrl;
    }

    @Override
    public MatchResult match(String url) {
        return testUrl.match(url);
    }

    public static UrlPattern equalsTo(String testUrl) {
        return new UrlPattern(StringValuePattern.equalTo(testUrl));
    }

    public static UrlPattern matching(String urlRegex) {
        return new UrlPattern(StringValuePattern.matches(urlRegex));
    }


}
