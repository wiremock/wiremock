package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class GlobalDynamicResponseDefinitionTransformer extends ResponseDefinitionTransformer {

    private final Handlebars handlebars = new Handlebars();

    @Override
    public String getName() {
        return "response-template";
    }

    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
        ResponseDefinitionBuilder newResponseDefBuilder = ResponseDefinitionBuilder.like(responseDefinition);
        final ImmutableMap<String, RequestTemplateModel> model = ImmutableMap.of("request", RequestTemplateModel.from(request));

        if (responseDefinition.getBody() != null) {
            Template bodyTemplate = uncheckedCompileTemplate(responseDefinition.getBody());
            String newBody = uncheckedApplyTemplate(bodyTemplate, model);
            newResponseDefBuilder.withBody(newBody);
        }

        if (responseDefinition.getHeaders() != null) {
            Iterable<HttpHeader> newResponseHeaders = Iterables.transform(responseDefinition.getHeaders().all(), new Function<HttpHeader, HttpHeader>() {
                @Override
                public HttpHeader apply(HttpHeader input) {
                    List<String> newValues = Lists.transform(input.values(), new Function<String, String>() {
                        @Override
                        public String apply(String input) {
                            Template template = uncheckedCompileTemplate(input);
                            return uncheckedApplyTemplate(template, model);
                        }
                    });

                    return new HttpHeader(input.key(), newValues);
                }
            });
            newResponseDefBuilder.withHeaders(new HttpHeaders(newResponseHeaders));
        }

        return newResponseDefBuilder.build();
    }

    private String uncheckedApplyTemplate(Template template, Object context) {
        try {
            return template.apply(context);
        } catch (IOException e) {
            return throwUnchecked(e, String.class);
        }
    }

    private Template uncheckedCompileTemplate(String content) {
        try {
            return handlebars.compileInline(content);
        } catch (IOException e) {
            return throwUnchecked(e, Template.class);
        }
    }
}
