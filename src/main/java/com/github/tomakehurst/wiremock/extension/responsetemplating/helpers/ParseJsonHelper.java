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

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.github.tomakehurst.wiremock.common.Json;

import java.io.IOException;
import java.util.Map;

public class ParseJsonHelper extends HandlebarsHelper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        CharSequence json;
        String variableName;

        boolean hasContext = context != options.context.model();

        if (options.tagType == TagType.SECTION) {
            json =  options.apply(options.fn);
            variableName = hasContext ? context.toString() : null;
        } else {
            if (!hasContext) {
                return handleError("Missing required JSON string parameter");
            }

            json = context.toString();
            variableName = options.params.length > 0 ? options.param(0) : null;
        }

        Map<String, Object> map = json != null ?
                Json.read(json.toString(), new TypeReference<Map<String, Object>>() {}) :
                null;

        if (variableName != null) {
            options.context.data(variableName, map);
            return null;
        }

        return map;
    }
}
