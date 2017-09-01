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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.common.Json;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

import java.io.IOException;
import java.util.Map;

public class HandlebarsJsonPathHelper extends HandlebarsHelper<String> {

    @Override
    public Object apply(final String inputJson, final Options options) throws IOException {
        if (inputJson == null) {
            return "";
        }

        if (options == null || options.param(0, null) == null) {
            return this.handleError("The JSONPath cannot be empty");
        }

        final String jsonPath = options.param(0);
        try {
            Object result = JsonPath.read(inputJson, jsonPath);
            if (result instanceof Map) {
                return Json.write(result);
            }
            return String.valueOf(result);
        } catch (InvalidJsonException e) {
            return this.handleError(
                    inputJson + " is not valid JSON",
                    e.getJson(), e);
        } catch (JsonPathException e) {
            return this.handleError(jsonPath + " is not a valid JSONPath expression", e);
        }
    }
}
