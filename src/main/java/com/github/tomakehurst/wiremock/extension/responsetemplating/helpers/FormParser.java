package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.google.common.collect.ImmutableMap;

import java.net.URLDecoder;
import java.util.Map;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class FormParser {

    public static Map<String, String> parse(String formText, boolean urlDecode) {
        return parse(formText, urlDecode, "utf-8");
    }

    public static Map<String, String> parse(String formText, boolean urlDecode, String encoding) {
        ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
        for (String formField: formText.split("&")) {
            String[] parts = formField.split("=");
            if (parts.length > 1) {
                String key = parts[0];
                String value = urlDecode ?
                        urlDecode(parts[1].trim(), encoding) :
                        parts[1].trim();

                mapBuilder.put(key, value);
            }
        }

        return mapBuilder.build();
    }

    private static String urlDecode(String text, String encoding) {
        try {
            return URLDecoder.decode(text, encoding);
        } catch (Exception e) {
            return throwUnchecked(e, String.class);
        }
    }
}
