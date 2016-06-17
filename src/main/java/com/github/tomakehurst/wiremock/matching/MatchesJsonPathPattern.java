package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.jsonpath.JsonPath;

import java.util.Collection;
import java.util.Map;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

public class MatchesJsonPathPattern extends StringValuePattern {

    public MatchesJsonPathPattern(String value) {
        super(value);
    }

    public String getMatchesJsonPath() {
        return expectedValue;
    }

    @Override
    public MatchResult match(@JsonProperty("matchesJsonPath") String value) {
        return MatchResult.of(isJsonPathMatch(value));
    }

    private boolean isJsonPathMatch(String value) {
        try {
            Object obj = JsonPath.read(value, expectedValue);
            if (obj instanceof Collection) {
                return !((Collection) obj).isEmpty();
            }

            if (obj instanceof Map) {
                return !((Map) obj).isEmpty();
            }

            return obj != null;
        } catch (Exception e) {
            String error;
            if (e.getMessage().equalsIgnoreCase("invalid path")) {
                error = "the JSON path didn't match the document structure";
            }
            else if (e.getMessage().equalsIgnoreCase("invalid container object")) {
                error = "the JSON document couldn't be parsed";
            } else {
                error = "of error '" + e.getMessage() + "'";
            }

            String message = String.format(
                "Warning: JSON path expression '%s' failed to match document '%s' because %s",
                expectedValue, value, error);
            notifier().info(message);
            return false;
        }
    }
}
