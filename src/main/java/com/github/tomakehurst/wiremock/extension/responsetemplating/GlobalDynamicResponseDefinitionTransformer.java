package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class GlobalDynamicResponseDefinitionTransformer extends ResponseDefinitionTransformer {

    private final Handlebars handlebars = new Handlebars();

    @Override
    public String getName() {
        return "response-template";
    }

    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
        Template bodyTemplate = uncheckedCompileTemplate(responseDefinition.getBody());
        ImmutableMap<String, RequestTemplateModel> model = ImmutableMap.of("request", RequestTemplateModel.from(request));
        String newBody = uncheckedApplyTemplate(bodyTemplate, model);
        return ResponseDefinitionBuilder.like(responseDefinition)
            .withBody(newBody)
            .build();
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
