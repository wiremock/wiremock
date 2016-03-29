package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class StringValuePatternJsonDeserializer extends JsonDeserializer<StringValuePattern> {

    @Override
    public StringValuePattern deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode rootNode = parser.readValueAsTree();
        Map.Entry<String, JsonNode> entry = rootNode.fields().next();
        String operatorName = entry.getKey();
        String operand = entry.getValue().textValue();

        try {
            Method method = StringValuePattern.class.getDeclaredMethod(operatorName, String.class);
            return (StringValuePattern) method.invoke(null, operand);
        } catch (NoSuchMethodException nsme) {
            throw new JsonMappingException(operatorName + " is not a recognised operator");
        } catch (Exception e) {
            return throwUnchecked(e, StringValuePattern.class);
        }
    }
}
