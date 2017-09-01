package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class JsonPathPatternJsonSerializer extends PathPatternJsonSerializer<MatchesJsonPathPattern> {
    @Override
    protected void serializeAdditionalFields(MatchesJsonPathPattern value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    }
}
