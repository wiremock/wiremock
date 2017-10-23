package com.github.tomakehurst.wiremock.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ListOrStringDeserialiser<T> extends JsonDeserializer<ListOrSingle<T>> {

    @Override
    @SuppressWarnings("unchecked")
    public ListOrSingle<T> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode rootNode = parser.readValueAsTree();
        if (rootNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) rootNode;
            List<T> items = newArrayList();
            for (Iterator<JsonNode> i = arrayNode.elements(); i.hasNext();) {
                JsonNode node = i.next();
                Object value = getValue(node);
                items.add((T) value);
            }

            return new ListOrSingle<>(items);
        }

        return new ListOrSingle<>((T) getValue(rootNode));
    }

    private static Object getValue(JsonNode node) {
        return node.isTextual() ? node.textValue() :
            node.isNumber() ? node.numberValue() :
                node.isBoolean() ? node.booleanValue() : node.textValue();
    }
}
