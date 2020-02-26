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
import com.github.tomakehurst.wiremock.extension.responsetemplating.RenderCache;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

public class HandlebarsJsonPathHelper extends HandlebarsHelper<Object> {

    @Override
    public Object apply(final Object input, final Options options) throws IOException {
        if (input == null) {
            return "";
        }

        if (options == null || options.param(0, null) == null) {
            return this.handleError("The JSONPath cannot be empty");
        }

        final String jsonPath = options.param(0);

        try {
            final DocumentContext jsonDocument = getJsonDocument(input, options);
            Object result = getValue(jsonPath, jsonDocument, options);
            return JsonData.create(result);
        } catch (InvalidJsonException e) {
            return this.handleError(
                    input + " is not valid JSON",
                    e.getJson(), e);
        } catch (JsonPathException e) {
            return this.handleError(jsonPath + " is not a valid JSONPath expression", e);
        }
    }

    private Object getValue(String jsonPath, DocumentContext jsonDocument, Options options) {
        RenderCache renderCache = getRenderCache(options);
        RenderCache.Key cacheKey = RenderCache.Key.keyFor(Object.class, jsonPath, jsonDocument);
        Object value = renderCache.get(cacheKey);
        if (value == null) {
            value = jsonDocument.read(jsonPath);
            renderCache.put(cacheKey, value);
        }

        return value;
    }

    private DocumentContext getJsonDocument(Object json, Options options) {
        RenderCache renderCache = getRenderCache(options);
        RenderCache.Key cacheKey = RenderCache.Key.keyFor(DocumentContext.class, json);
        DocumentContext document = renderCache.get(cacheKey);
        if (document == null) {
            document = json instanceof String ?
                    JsonPath.parse((String) json) :
                    JsonPath.parse(json);
            renderCache.put(cacheKey, document);
        }

        return document;
    }
}
