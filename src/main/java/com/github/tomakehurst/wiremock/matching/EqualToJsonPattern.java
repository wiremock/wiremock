package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonDiff;
import com.github.tomakehurst.wiremock.common.Json;

import static com.github.tomakehurst.wiremock.common.Json.deepSize;

public class EqualToJsonPattern extends StringValuePattern {

    private final JsonNode expected;

    public EqualToJsonPattern(String json) {
        super(json);
        expected = Json.read(json, JsonNode.class);
    }

    @Override
    public MatchResult match(String value) {
        JsonNode actual = Json.read(value, JsonNode.class);
        ArrayNode diff = (ArrayNode) JsonDiff.asJson(expected, actual);

        double maxNodes = Math.max(deepSize(expected), deepSize(actual));
        double distance = diffSize(diff) / maxNodes;

        return MatchResult.partialMatch(distance);
    }

    private static int diffSize(ArrayNode diff) {
        int acc = 0;
        for (JsonNode child: diff) {
            JsonNode valueNode = child.findValue("value");
            if (valueNode == null) {
                acc++;
            } else {
                acc += Json.deepSize(valueNode);
            }
        }

        return acc;
    }

    @Override
    public String getName() {
        return "equalToJson";
    }

}
