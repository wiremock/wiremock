package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class PatternClass {
    public static final Map<String, Class<? extends StringValuePattern>> PATTERNS =
            Map.ofEntries(
                    Map.entry("equalTo", EqualToPattern.class),
                    Map.entry("equalToJson", EqualToJsonPattern.class),
                    Map.entry("matchesJsonPath", MatchesJsonPathPattern.class),
                    Map.entry("matchesJsonSchema", MatchesJsonSchemaPattern.class),
                    Map.entry("equalToXml", EqualToXmlPattern.class),
                    Map.entry("matchesXPath", MatchesXPathPattern.class),
                    Map.entry("contains", ContainsPattern.class),
                    Map.entry("not", NotPattern.class),
                    Map.entry("doesNotContain", NegativeContainsPattern.class),
                    Map.entry("matches", RegexPattern.class),
                    Map.entry("doesNotMatch", NegativeRegexPattern.class),
                    Map.entry("before", BeforeDateTimePattern.class),
                    Map.entry("after", AfterDateTimePattern.class),
                    Map.entry("equalToDateTime", EqualToDateTimePattern.class),
                    Map.entry("anything", AnythingPattern.class),
                    Map.entry("absent", AbsentPattern.class),
                    Map.entry("and", LogicalAnd.class),
                    Map.entry("or", LogicalOr.class),
                    Map.entry("matchesPathTemplate", PathTemplatePattern.class));

    public static Map.Entry<String, JsonNode> findMainFieldEntry(JsonNode rootNode) {
        List<Map.Entry<String, JsonNode>> list = getListFromNode(rootNode);
        return list.stream()
                .filter(input -> PATTERNS.containsKey(input.getKey()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    public static List<Map.Entry<String, JsonNode>> getListFromNode(JsonNode rootNode) {
        List<Map.Entry<String, JsonNode>> list = new LinkedList<>();
        rootNode.fields().forEachRemaining(list::add);
        return list;
    }

    public static Class<? extends StringValuePattern> findPatternClass(JsonNode rootNode)
            throws JsonMappingException {
        for (Map.Entry<String, JsonNode> node : getListFromNode(rootNode)) {
            Class<? extends StringValuePattern> patternClass = PATTERNS.get(node.getKey());
            if (patternClass != null) {
                return patternClass;
            }
        }

        throw new JsonMappingException(rootNode + " is not a valid match operation");
    }
}

