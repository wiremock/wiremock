package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class XPathPatternJsonSerializer extends PathPatternJsonSerializer<MatchesXPathPattern> {

    @Override
    protected void serializeAdditionalFields(MatchesXPathPattern value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value.getXPathNamespaces() != null && !value.getXPathNamespaces().isEmpty()) {
            gen.writeObjectFieldStart("xPathNamespaces");
            for (Map.Entry<String, String> namespace: value.getXPathNamespaces().entrySet()) {
                gen.writeStringField(namespace.getKey(), namespace.getValue());
            }
            gen.writeEndObject();
        }
    }
}
