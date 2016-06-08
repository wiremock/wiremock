package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.WireMock;

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

}
