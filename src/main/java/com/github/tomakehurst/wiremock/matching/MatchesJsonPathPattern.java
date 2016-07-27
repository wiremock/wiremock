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
