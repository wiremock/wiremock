package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class StringValuePatternJsonSerializer extends JsonSerializer<StringValuePattern> {

    @Override
    public void serialize(StringValuePattern value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        if (value.nullSafeIsAbsent()) {
            gen.writeBooleanField("absent", true);
        } else {
            gen.writeStringField(value.getName(), value.getValue());
        }
        gen.writeEndObject();
    }
}
