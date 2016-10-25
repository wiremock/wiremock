/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.common.Json.maxDeepSize;

public class EqualToJsonPattern extends StringValuePattern {

    private final JsonNode expected;
    private final Boolean ignoreArrayOrder;
    private final Boolean ignoreExtraElements;

    public EqualToJsonPattern(@JsonProperty("equalToJson") String json,
                              @JsonProperty("ignoreArrayOrder") Boolean ignoreArrayOrder,
                              @JsonProperty("ignoreExtraElements") Boolean ignoreExtraElements) {
        super(json);
        expected = Json.read(json, JsonNode.class);
        this.ignoreArrayOrder = ignoreArrayOrder;
        this.ignoreExtraElements = ignoreExtraElements;
    }

    public String getEqualToJson() {
        return expectedValue;
    }

    private boolean shouldIgnoreArrayOrder() {
        return ignoreArrayOrder != null && ignoreArrayOrder;
    }

    public Boolean isIgnoreArrayOrder() {
        return ignoreArrayOrder;
    }

    private boolean shouldIgnoreExtraElements() {
        return ignoreArrayOrder != null && ignoreExtraElements;
    }

    public Boolean isIgnoreExtraElements() {
        return ignoreExtraElements;
    }

    @Override
    public String getExpected() {
        return Json.prettyPrint(getValue());
    }

    @Override
    public MatchResult match(final String value) {
        try {
            final JsonNode actual = Json.read(value, JsonNode.class);

            return new MatchResult() {
                @Override
                public boolean isExactMatch() {
                    // Try to do it the fast way first, then fall back to doing the full diff
                    return (!shouldIgnoreArrayOrder() && !shouldIgnoreExtraElements() && Objects.equals(actual, expected))
                        || getDistance() == 0.0;
                }

                @Override
                public double getDistance() {
                    JSONCompareMode mode = getJsonCompareMode();
                    try {
                        JSONCompareResult result = JSONCompare.compareJSON(expectedValue, value, mode);
                        if (failureAtRootNode(result)) {
                            return 1.0;
                        }
                        int failedNodes = result.getFieldFailures().size()
                                + result.getFieldMissing().size();
                        if (!shouldIgnoreExtraElements()) {
                            failedNodes += result.getFieldUnexpected().size();
                        }
                        double maxNodes = maxDeepSize(expected, actual);
                        return failedNodes / maxNodes;
                    } catch (JSONException e) {
                        return 1.0;
                    }

                }
            };
        } catch (Exception e) {
            return MatchResult.noMatch();
        }
    }

    private boolean failureAtRootNode(JSONCompareResult result) {
        return result.failed() && result.getFieldFailures().size() == 1
                && result.getFieldFailures().get(0).getField().isEmpty();
    }

    private JSONCompareMode getJsonCompareMode() {
        if (shouldIgnoreArrayOrder() && shouldIgnoreExtraElements()) {
            return JSONCompareMode.LENIENT;
        } else if (shouldIgnoreArrayOrder()) {
            return JSONCompareMode.NON_EXTENSIBLE;
        } else if (shouldIgnoreExtraElements()) {
            return JSONCompareMode.STRICT_ORDER;
        } else {
            return JSONCompareMode.STRICT;
        }
    }
}
