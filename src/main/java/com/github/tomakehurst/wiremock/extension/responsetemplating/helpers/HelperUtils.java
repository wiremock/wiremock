package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

public class HelperUtils {

    public static Integer coerceToInt(Object value) {
        if (Number.class.isAssignableFrom(value.getClass())) {
            return ((Number) value).intValue();
        }

        if (CharSequence.class.isAssignableFrom(value.getClass())) {
            return Integer.parseInt(value.toString());
        }

        return null;
    }
}
