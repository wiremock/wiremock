package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class MultiValuePatternDeserializer extends JsonDeserializer<MultiValuePattern> {

  @Override
  public MultiValuePattern deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException {
    JsonNode rootNode = parser.readValueAsTree();
    final ObjectMapper mapper = (ObjectMapper) parser.getCodec();
    if (rootNode.has("hasExactly")) {
      return mapper.treeToValue(rootNode, ExactMatchMultiValuePattern.class);
    } else {
      return mapper.treeToValue(rootNode, SingleMatchMultiValuePattern.class);
    }
  }
}
