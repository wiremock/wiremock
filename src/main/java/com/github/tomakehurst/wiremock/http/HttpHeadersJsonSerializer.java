package com.github.tomakehurst.wiremock.http;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class HttpHeadersJsonSerializer extends JsonSerializer<HttpHeaders> {

    @Override
    public void serialize(HttpHeaders headers, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        for (HttpHeader header: headers.all()) {
            if (header.isSingleValued()) {
                jgen.writeStringField(header.key(), header.firstValue());
            } else {
                jgen.writeArrayFieldStart(header.key());
                for (String value: header.values()) {
                    jgen.writeString(value);
                }
                jgen.writeEndArray();
            }
        }
        jgen.writeEndObject();
    }
}
