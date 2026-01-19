/*
 * Copyright (C) 2021-2026 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Json;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.AbstractJsonProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.json.JsonMapper;

public class HelperUtils {

  public static Integer coerceToInt(Object value) {
    if (value == null) {
      return null;
    }

    if (Number.class.isAssignableFrom(value.getClass())) {
      return ((Number) value).intValue();
    }

    if (CharSequence.class.isAssignableFrom(value.getClass())) {
      return Integer.parseInt(value.toString());
    }

    return null;
  }

  public static Double coerceToDouble(Object value) {
    if (value == null) {
      return null;
    }

    if (Number.class.isAssignableFrom(value.getClass())) {
      return ((Number) value).doubleValue();
    }

    if (CharSequence.class.isAssignableFrom(value.getClass())) {
      return Double.parseDouble(value.toString());
    }

    return null;
  }

  static final Configuration jsonPathConfig =
      Configuration.defaultConfiguration()
          .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
          // Explicitly use Jackson for JSON parsing because the default provider allows invalid
          // JSON to be parsed as a JSON string in certain circumstances, which, in my opinion,
          // creates a confusing user experience.
          .jsonProvider(new Jackson3JsonProvider(Json.getJsonMapper()));

  // TODO: Replace this if JsonPath adds support for Jackson 3.
  private static class Jackson3JsonProvider extends AbstractJsonProvider {

    private final JsonMapper jsonMapper;

    Jackson3JsonProvider(JsonMapper jsonMapper) {
      this.jsonMapper = jsonMapper;
    }

    @Override
    public Object parse(String json) throws InvalidJsonException {
      try {
        return jsonMapper.readValue(json, Object.class);
      } catch (JacksonException e) {
        throw new InvalidJsonException(e);
      }
    }

    @Override
    public Object parse(InputStream jsonStream, String charset) throws InvalidJsonException {
      try {
        return jsonMapper.readValue(new InputStreamReader(jsonStream, charset), Object.class);
      } catch (JacksonException | UnsupportedEncodingException e) {
        throw new InvalidJsonException(e);
      }
    }

    @Override
    public String toJson(Object obj) {
      StringWriter writer = new StringWriter();
      try {
        JsonGenerator generator = jsonMapper.createGenerator(writer);
        jsonMapper.writeValue(generator, obj);
        writer.flush();
        writer.close();
        generator.close();
        return writer.getBuffer().toString();
      } catch (IOException | JacksonException e) {
        throw new InvalidJsonException(e);
      }
    }

    @Override
    public Object createArray() {
      return new LinkedList<>();
    }

    @Override
    public Object createMap() {
      return new LinkedHashMap<>();
    }
  }
}
