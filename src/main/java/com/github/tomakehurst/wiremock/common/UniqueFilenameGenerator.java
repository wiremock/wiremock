package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.http.Request;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import java.net.URI;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.lang.Math.min;

public class UniqueFilenameGenerator {
    public static String generate(Request request, String prefix, String id) {
        URI uri = URI.create(request.getUrl());
        Iterable<String> uriPathNodes = on("/").omitEmptyStrings().split(uri.getPath());
        int nodeCount = size(uriPathNodes);

        String pathPart = nodeCount > 0 ?
                Joiner.on("-").join(from(uriPathNodes).skip(nodeCount - min(nodeCount, 2))) :
                "(root)";


        return new StringBuilder(prefix)
                .append("-")
                .append(pathPart)
                .append("-")
                .append(id)
                .append(".json")
                .toString();
    }
}
