package com.github.tomakehurst.wiremock.extension;

import java.util.LinkedHashMap;

public class Parameters extends LinkedHashMap<String, Object> {

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) super.get(key);
    }

    public static Parameters empty() {
        return new Parameters();
    }

    public static Parameters of(String key, Object value) {
        Parameters parameters = new Parameters();
        parameters.put(key, value);
        return parameters;
    }

    public Parameters and(String key, Object value) {
        put(key, value);
        return this;
    }
}
