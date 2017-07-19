package com.github.tomakehurst.wiremock.common;

import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SafeNames {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String makeSafeNameFromUrl(String urlPath) {
        String startingPath = urlPath.replace("/", "_");
        return makeSafeName(startingPath);
    }

    public static String makeSafeName(String name) {
        String nowhitespace = WHITESPACE.matcher(name).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");

        slug = slug.replaceAll("^[_]*", "");
        slug = slug.replaceAll("[_]*$", "");

        slug = StringUtils.truncate(slug, 200);

        return slug.toLowerCase(Locale.ENGLISH);
    }
}
