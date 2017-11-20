package com.github.tomakehurst.wiremock.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.List;

public class ListOrSingleSerialiser extends JsonSerializer<ListOrSingle<Object>> {

    @Override
    public void serialize(ListOrSingle<Object> value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        if (value.isEmpty()) {
            gen.writeStartArray();
            gen.writeEndArray();
            return;
        }

        Object firstValue = value.first();
        if (value.isSingle()) {
            JsonSerializer<Object> serializer = serializers.findValueSerializer(firstValue.getClass());
            serializer.serialize(firstValue, gen, serializers);
        } else {
            CollectionType type = TypeFactory.defaultInstance().constructCollectionType(List.class, firstValue.getClass());
            JsonSerializer<Object> serializer = serializers.findValueSerializer(type);
            serializer.serialize(value, gen, serializers);
        }
    }
}
