package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.ArrayList;

public class UrlPath extends ArrayList<String> {

    private final String originalPath;

    public UrlPath(String url) {
        originalPath = URI.create(url).getPath();
        Iterable<String> pathNodes = Splitter.on('/').split(originalPath);
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
