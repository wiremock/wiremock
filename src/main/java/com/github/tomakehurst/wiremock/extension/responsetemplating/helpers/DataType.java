package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.common.Json;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
            DataType dataType = getDataTypeIfRequired(nextNode.textValue());
            if (type.equals(JsonNodeType.STRING) && Objects.nonNull(dataType))
                ((ObjectNode) jsonNode).replace(
                        fieldName,
                        JsonNodeFactory.instance.pojoNode(convertValue(nextNode.textValue(), dataType))
                );
            else handle(nextNode);
        }
    }

    private static void handleArray(JsonNode jsonNode) {
        ArrayNode arrayNode = (ArrayNode) jsonNode;
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode nextNode = arrayNode.get(i);
            JsonNodeType type = nextNode.getNodeType();
            DataType dataType = getDataTypeIfRequired(nextNode.textValue());
            if (type.equals(JsonNodeType.STRING) && Objects.nonNull(dataType))
                arrayNode.setPOJO(i, convertValue(nextNode.textValue(), dataType));
            else handle(nextNode);
        }
    }

    private static DataType getDataTypeIfRequired(String value) {
        List<DataType> dataTypes = Arrays.stream(values())
                .filter(type -> value.contains(type.toString()))
                .collect(Collectors.toList());
        return dataTypes.size() == 1 ? dataTypes.get(0) : null;
    }

    private static Object convertValue(String value, DataType dataType) {
        Object result = null;
        if (!dataType.equals(DataType.__null))
            try {
                result = Json.getObjectMapper().convertValue(value.replace(dataType.toString(), ""), dataType.CLASS);
            } catch (IllegalArgumentException exception) {
                result = "ERROR: " + exception.getMessage().replaceFirst("\n.*", "");
            }
        return result;
    }
}
