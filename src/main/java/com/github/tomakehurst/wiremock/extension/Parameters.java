package com.github.tomakehurst.wiremock.extension;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class Parameters extends HashMap<String, Object> {

    public Integer getInt(String key) {
        return checkValidityAndCast(key, Integer.class);
    }

    public Boolean getBoolean(String key) {
        return checkValidityAndCast(key, Boolean.class);
    }

    public String getString(String key) {
        return checkValidityAndCast(key, String.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T checkValidityAndCast(String key, Class<T> type) {
        checkArgument(containsKey(key), "Parameter '" + key + "' not present");
        checkArgument(type.isAssignableFrom(get(key).getClass()), "Parameter " + key + " is not of type " + type.getSimpleName());
        return (T) get(key);
    }


    public static Parameters empty() {
        return new Parameters();
    }

    public static Parameters from(Map<String, Object> parameterMap) {
        Parameters parameters = new Parameters();
        parameters.putAll(parameterMap);
        return parameters;
    }

    public static Parameters one(String name, Object value) {
        return from(ImmutableMap.of(name, value));
    }
}
