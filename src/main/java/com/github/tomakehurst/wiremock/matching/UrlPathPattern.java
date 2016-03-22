package com.github.tomakehurst.wiremock.matching;

import java.net.URI;

public class UrlPathPattern extends UrlPattern {

    protected UrlPathPattern(StringValuePattern testUrl) {
        super(testUrl);
    }

    @Override
    public MatchResult match(String url) {
        String path = URI.create(url).getPath();
        return super.match(path);
    }

    public static UrlPathPattern equalTo(String testUrl) {
        return new UrlPathPattern(StringValuePattern.equalTo(testUrl));
    }

    public static UrlPathPattern matching(String urlRegex) {
        return new UrlPathPattern(StringValuePattern.matches(urlRegex));
    }


}
