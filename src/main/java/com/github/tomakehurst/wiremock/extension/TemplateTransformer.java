package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.http.*;
import org.stringtemplate.v4.ST;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

public class TemplateTransformer extends ResponseTransformer {

    @Override
    public Response transform(Request request, Response originalResponse, FileSource files, Parameters parameters) {
        Response.Builder responseBuilder = Response.Builder.like(originalResponse);
        TemplateRequestAdapter requestForTemplate = new TemplateRequestAdapter(request);

        if (originalResponse.getBody() != null) {
            responseBuilder.body(substitute(originalResponse.getBodyAsString(), requestForTemplate));
        }

        if (originalResponse.getHeaders() != null) {
            responseBuilder.headers(transformHeaders(originalResponse.getHeaders(), requestForTemplate));
        }
        
        return responseBuilder.build();
    }

    private HttpHeaders transformHeaders(HttpHeaders originalHeaders, TemplateRequestAdapter request) {
        Collection<HttpHeader> newHeaders = newArrayList();
        for (HttpHeader oldHeader : originalHeaders.all()) {
            newHeaders.add(new HttpHeader(oldHeader.key(), substitute(oldHeader.firstValue(), request)));
        }
        return new HttpHeaders(newHeaders);
    }

    private String substitute(String input, TemplateRequestAdapter request) {
        ST template = new ST(input, '$', '$');
        template.add("request", request);
        return template.render();
    }

    @Override
    public String name() {
        return "TemplateTransformer";
    }
}
