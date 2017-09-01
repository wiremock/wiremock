package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public abstract class PathPatternJsonSerializer<T extends PathPattern> extends JsonSerializer<T> {

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        if (value.isSimple()) {
            gen.writeStringField(value.getName(), value.getExpected());
        } else {
            gen.writeObjectFieldStart(value.getName());
            gen.writeStringField("expression", value.getExpected());
            gen.writeStringField(value.getValuePattern().getName(), value.getValuePattern().getExpected());
            gen.writeEndObject();
        }

        serializeAdditionalFields(value, gen, serializers);

        gen.writeEndObject();
    }

    protected abstract void serializeAdditionalFields(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException;

}
