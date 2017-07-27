package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.Map;

public class ContentPatternDeserialiser extends JsonDeserializer<ContentPattern<?>> {

    @Override
    public ContentPattern<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        JsonNode rootNode = parser.readValueAsTree();

        if (isAbsent(rootNode)) {
            return StringValuePattern.ABSENT;
        }

        if (!rootNode.has("binaryEqualTo")) {
            return new StringValuePatternJsonDeserializer().buildStringValuePattern(rootNode);
        }

        return deserializeBinaryEqualTo(rootNode);
    }

    private BinaryEqualToPattern deserializeBinaryEqualTo(JsonNode rootNode) throws JsonMappingException {
        String operand = rootNode.findValue("binaryEqualTo").textValue();

        return new BinaryEqualToPattern(operand);
    }

    private static boolean isAbsent(JsonNode rootNode) {
        for (Map.Entry<String, JsonNode> node: ImmutableList.copyOf(rootNode.fields())) {
            if (node.getKey().equals("absent")) {
                return true;
            }
        }

        return false;
    }
}
