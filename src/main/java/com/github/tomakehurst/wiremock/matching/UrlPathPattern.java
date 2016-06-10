package com.github.tomakehurst.wiremock.matching;

import java.net.URI;

public class UrlPathPattern extends UrlPattern {

    public UrlPathPattern(StringValuePattern testUrl, boolean regex) {
        super(testUrl, regex);
    }

    @Override
    public MatchResult match(String url) {
        String path = URI.create(url).getPath();
        return super.match(path);
    }

    @Override
    public String toString() {
        return "path " + pattern.toString();
    }
}
