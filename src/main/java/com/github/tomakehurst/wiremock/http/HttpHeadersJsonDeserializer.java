package com.github.tomakehurst.wiremock.http;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.collect.Iterables.transform;

public class HttpHeadersJsonDeserializer extends JsonDeserializer<HttpHeaders> {

    @Override
    public HttpHeaders deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode rootNode = parser.readValueAsTree();
        return new HttpHeaders(transform(all(rootNode.getFields()), toHttpHeaders()));
    }

    private static Function<Map.Entry<String, JsonNode>, HttpHeader> toHttpHeaders() {
        return new Function<Map.Entry<String, JsonNode>, HttpHeader>() {
            @Override
            public HttpHeader apply(Map.Entry<String, JsonNode> field) {
                String key = field.getKey();
                if (field.getValue().isArray()) {
                    Collection<String> values =
                            ImmutableList.copyOf(transform(all(field.getValue().getElements()), toStringValues()));
                    return new HttpHeader(key, values);
                } else {
                    return new HttpHeader(key, field.getValue().getTextValue());
                }
            }
        };
    }

    private static Function<JsonNode, String> toStringValues() {
        return new Function<JsonNode, String>() {
            public String apply(JsonNode node) {
                return node.getTextValue();
            }
        };
    }

    public static <T> IteratorWrapper<T> all(Iterator<T> underlyingIterator) {
        IteratorWrapper<T> wrapper = new IteratorWrapper<T>();
        wrapper.underlyingIterator = underlyingIterator;
        return wrapper;
    }

    private static class IteratorWrapper<T> implements Iterable<T> {

        private Iterator<T> underlyingIterator;

        @Override
        public Iterator<T> iterator() {
            return underlyingIterator;
        }
    }
}
