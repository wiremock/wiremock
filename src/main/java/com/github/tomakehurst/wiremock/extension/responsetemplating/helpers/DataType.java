package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.github.tomakehurst.wiremock.common.Json;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum DataType {

    __double(double.class),
    __long(long.class),
    __integer(int.class),
    __float(float.class),
    __boolean(boolean.class),
    __null(null);

    private final Class<?> CLASS;

    DataType(Class<?> clazz) {
        CLASS = clazz;
    }

    public static void handle(JsonNode jsonNode) {
        JsonNodeType type = jsonNode.getNodeType();
        if (type.equals(JsonNodeType.OBJECT)) handleObject(jsonNode);
        else if (type.equals(JsonNodeType.ARRAY)) handleArray(jsonNode);
    }

    private static void handleObject(JsonNode jsonNode) {
        Iterator<String> fieldNames = jsonNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode nextNode = jsonNode.get(fieldName);
            JsonNodeType type = nextNode.getNodeType();
            if (type.equals(JsonNodeType.STRING) && containsIn(nextNode.textValue()))
                ((ObjectNode) jsonNode).replace(fieldName, JsonNodeFactory.instance.pojoNode(convertValue(nextNode.textValue())));
            else handle(nextNode);
        }
    }

    private static void handleArray(JsonNode jsonNode) {
        ArrayNode arrayNode = (ArrayNode) jsonNode;
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode nextNode = arrayNode.get(i);
            JsonNodeType type = nextNode.getNodeType();
            if (type.equals(JsonNodeType.STRING) && containsIn(nextNode.textValue()))
                arrayNode.setPOJO(i, convertValue(nextNode.textValue()));
            else handle(nextNode);
        }
    }

    private static boolean containsIn(String value) {
        return getFilteredStream(value).count() == 1;
    }

    private static Stream<DataType> getFilteredStream(String value) {
        return Arrays.stream(values()).filter(type -> value.contains(type.toString()));
    }

    private static Object convertValue(String value) {
        Object result = null;
        DataType dataType = getFilteredStream(value).collect(Collectors.toList()).get(0);
        if (!dataType.equals(DataType.__null))
            try {
                result = Json.getObjectMapper().convertValue(value.replace(dataType.toString(), ""), dataType.CLASS);
            } catch (IllegalArgumentException exception) {
                result = "ERROR: " + exception.getMessage().replaceFirst("\n.*", "");
            }
        return result;
    }
}
