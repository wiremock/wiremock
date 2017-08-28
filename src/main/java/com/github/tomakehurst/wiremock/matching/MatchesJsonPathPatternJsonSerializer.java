package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class MatchesJsonPathPatternJsonSerializer extends JsonSerializer<MatchesJsonPathPattern> {

    @Override
    public void serialize(MatchesJsonPathPattern value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        if (value.isSimple()) {
            gen.writeStringField(value.getName(), value.getExpected());
        } else {
            gen.writeObjectFieldStart(value.getName());
            gen.writeStringField("expression", value.getExpected());
            gen.writeStringField(value.getValuePattern().getName(), value.getValuePattern().getExpected());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }
}
