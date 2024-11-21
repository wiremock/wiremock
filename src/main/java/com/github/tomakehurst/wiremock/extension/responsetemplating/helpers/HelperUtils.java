/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;

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
          .jsonProvider(new JacksonJsonProvider(Json.getObjectMapper()));
}
