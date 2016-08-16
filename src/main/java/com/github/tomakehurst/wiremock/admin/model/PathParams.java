package com.github.tomakehurst.wiremock.admin.model;

import java.util.LinkedHashMap;
import java.util.UUID;

public class PathParams extends LinkedHashMap<String, String> {

    public static PathParams empty() {
        return new PathParams();
    }

    public PathParams add(String key, String value) {
        put(key, value);
        return this;
    }

    public static PathParams single(String key, Object value) {
        return new PathParams().add(key, value.toString());
    }
}
