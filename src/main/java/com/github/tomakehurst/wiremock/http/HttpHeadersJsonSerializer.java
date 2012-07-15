package com.github.tomakehurst.wiremock.http;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

public class HttpHeadersJsonSerializer extends JsonSerializer<HttpHeaders> {

    @Override
    public void serialize(HttpHeaders headers, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        for (HttpHeader header: headers.all()) {
        }
    }
}
