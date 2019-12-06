package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Diff;
import net.javacrumbs.jsonunit.core.internal.Node;
import net.javacrumbs.jsonunit.core.listener.Difference;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.ARRAY;
import static net.javacrumbs.jsonunit.core.internal.Node.NodeType.OBJECT;

public class EqualToJsonPattern extends StringValuePattern {

    static {
        System.setProperty("json-unit.libraries", "jackson2");
    }

    private final Boolean ignoreArrayOrder;
    private final Boolean ignoreExtraElements;
    private final Boolean serializeAsString;

    public EqualToJsonPattern(@JsonProperty("equalToJson") String json,
                              @JsonProperty("ignoreArrayOrder") Boolean ignoreArrayOrder,
                              @JsonProperty("ignoreExtraElements") Boolean ignoreExtraElements) {
        super(json);
        this.ignoreArrayOrder = ignoreArrayOrder;
        this.ignoreExtraElements = ignoreExtraElements;
        this.serializeAsString = true;
    }

    public EqualToJsonPattern(JsonNode jsonNode,
                              Boolean ignoreArrayOrder,
                              Boolean ignoreExtraElements) {
        super(Json.write(jsonNode));
        this.ignoreArrayOrder = ignoreArrayOrder;
        this.ignoreExtraElements = ignoreExtraElements;
        this.serializeAsString = false;
    }

    EqualToJsonPattern(String json) {
        this(json, false, false);
    }

    @Override
    public MatchResult match(String value) {
        final CountingDiffListener diffListener = new CountingDiffListener();
        Configuration diffConfig = Configuration.empty()
                .withDifferenceListener(diffListener);

        if (shouldIgnoreArrayOrder()) {
            diffConfig = diffConfig.withOptions(Option.IGNORING_ARRAY_ORDER);
        }

        if (shouldIgnoreExtraElements()) {
            diffConfig = diffConfig.withOptions(Option.IGNORING_EXTRA_ARRAY_ITEMS, Option.IGNORING_EXTRA_FIELDS);
        }

        final Diff diff;
        try {

            diff = Diff.create(
                    expectedValue,
                    value,
                    "",
                    "",
                    diffConfig
            );
        } catch (Exception e) {
            return MatchResult.noMatch();
        }

        return new MatchResult() {
            @Override
            public boolean isExactMatch() {
                return diff.similar();
            }

            @Override
            public double getDistance() {
                diff.similar();
                Node expected = getNodeFromDiff("expectedRoot", diff);
                Node actual = getNodeFromDiff("actualRoot", diff);
                double maxNodes = maxDeepSize(expected, actual);
                return diffListener.count / maxNodes;
            }
        };
    }

    @JsonProperty("equalToJson")
    public Object getSerializedEqualToJson() {
        return serializeAsString ? getValue() : Json.read(getValue(), JsonNode.class);
    }

    private boolean shouldIgnoreArrayOrder() {
        return ignoreArrayOrder != null && ignoreArrayOrder;
    }

    public Boolean isIgnoreArrayOrder() {
        return ignoreArrayOrder;
    }

    private boolean shouldIgnoreExtraElements() {
        return ignoreExtraElements != null && ignoreExtraElements;
    }

    public Boolean isIgnoreExtraElements() {
        return ignoreExtraElements;
    }

    @Override
    public String getExpected() {
        return Json.prettyPrint(getValue());
    }

    private static class CountingDiffListener implements DifferenceListener {

        public int count = 0;

        @Override
        public void diff(Difference difference, DifferenceContext context) {
            final int expectedSize = difference.getExpected() != null ?
                    deepSize(difference.getExpected()) :
                    0;
            final int actualSize = difference.getActual() != null ?
                    deepSize(difference.getActual()) :
                    0;

//            final int delta = expectedSize - actualSize;
            final int delta = maxDeepSize(difference.getExpected(), difference.getActual());
            count += delta == 0 ? 1 : Math.abs(delta);
        }
    }

    public static int maxDeepSize(Object one, Object two) {
        return Math.max(
                one != null ? deepSize(one) : 0,
                two != null ? deepSize(two) : 0
        );
    }

    private static int deepSize(Object nodeObj) {
        if (nodeObj == null) {
            return 0;
        }

        if (nodeObj instanceof String && ((String) nodeObj).isEmpty()) {
            return 0;
        }

        if (!(nodeObj instanceof Node)) {
            return 1;
        }

        Node node = (Node) nodeObj;

        int acc = 1;
        if (isContainerNode(nodeObj)) {
            for (Object child: allValues(node)) {
                acc++;
                if (isContainerNode(child)) {
                    acc += deepSize(child);
                }
            }
        }

        return acc;
    }

    private static boolean isContainerNode(Object nodeObj) {
        if (!(nodeObj instanceof Node)) {
            return false;
        }

        Node node = (Node) nodeObj;
        final Node.NodeType nodeType = node.getNodeType();
        return nodeType == ARRAY || nodeType == OBJECT;
    }

    private static Iterable<Object> allValues(Node containerNode) {
        final Node.NodeType nodeType = containerNode.getNodeType();
        if (nodeType == ARRAY) {
            return toNodeList(containerNode);
        } else if (nodeType == OBJECT) {
            return toNodeMap(containerNode).values();
        }

        return Collections.<Object>singletonList(containerNode);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> toNodeList(Node node) {
        return ((List<Object>) ARRAY.getValue(node));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toNodeMap(Node node) {
        return ((Map<String, Object>) OBJECT.getValue(node));
    }

    private static Node getNodeFromDiff(String fieldName, Diff diff) {
        try {
            final Field field = Diff.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Node) field.get(diff);
        } catch (Exception e) {
            return throwUnchecked(e, Node.class);
        }
    }
}
