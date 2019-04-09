package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.common.ListOrSingle;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexExtractHelper extends HandlebarsHelper<Object> {

    @Override
    public Object apply(Object context, Options options) {
        String regexString = options.param(0);
        Pattern regex = Pattern.compile(regexString);
        Matcher matcher = regex.matcher(context.toString());
        if (!matcher.find()) {
            return handleError("Nothing matched " + regexString);
        }

        if (options.params.length == 1) {
            return matcher.group();
        }

        List<String> groups = new ArrayList<>(matcher.groupCount());
        for (int i = 1; i <= matcher.groupCount(); i++) {
            groups.add(matcher.group(i));
        }

        String variableName = options.param(1);
        options.context.data(variableName, new ListOrSingle<>(groups));

        return null;

    }
}
