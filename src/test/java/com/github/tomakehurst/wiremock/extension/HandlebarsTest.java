package com.github.tomakehurst.wiremock.extension;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ListOrSingle;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HandlebarsTest {

    @Test
    public void singleOrList() throws Exception {
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelper("get", new Helper<List<?>>() {
                @Override
                public Object apply(List<?> context, Options options) throws IOException {
                    Integer index = options.param(0, 0);
                    return context.get(index);
                }
            }
        );
        handlebars.registerHelper("join", StringHelpers.join);
        Template singleTemplate = handlebars.compileInline("{{model}}");
        Template manyTemplate = handlebars.compileInline("{{join model ','}}");
        Template getTemplate = handlebars.compileInline("{{model.[2]}}");

        ListOrSingle<String> many = new ListOrSingle<>("one", "two", "three");
        ListOrSingle<Object> none = new ListOrSingle<>();

        System.out.println(singleTemplate.apply(ImmutableMap.of("model", many)));
        System.out.println(singleTemplate.apply(ImmutableMap.of("model", none)));
        System.out.println(manyTemplate.apply(ImmutableMap.of("model", many)));
        System.out.println(getTemplate.apply(ImmutableMap.of("model", many)));
    }

    @Test
    public void awkwardKeyNames() throws IOException {
        Handlebars handlebars = new Handlebars();

        Map<String, ListOrSingle<String>> headers = ImmutableMap.of(
            "X-Header_One", ListOrSingle.of("one", "two"),
            "$%Myheader", ListOrSingle.of("three", "four")
        );

        System.out.println(
            handlebars.compileInline("{{model.X-Header_One}}")
            .apply(ImmutableMap.of("model", headers)));

        System.out.println(
            handlebars.compileInline("{{model.[$%Myheader]}}")
            .apply(ImmutableMap.of("model", headers)));
    }
}
