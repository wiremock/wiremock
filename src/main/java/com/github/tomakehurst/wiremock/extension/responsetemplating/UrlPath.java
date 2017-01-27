package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.google.common.base.Splitter;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.ArrayList;

public class UrlPath extends ArrayList<String> {

    private final String originalPath;

    public UrlPath(String url) {
        HttpUrl parse = HttpUrl.parse(url);
        originalPath = parse.encodedPath();
        Iterable<String> pathNodes = parse.pathSegments();
        for (String pathNode: pathNodes) {
            if (StringUtils.isNotEmpty(pathNode)) {
                add(pathNode);
            }
        }
    }

    @Override
    public String toString() {
        return originalPath;
    }
}
