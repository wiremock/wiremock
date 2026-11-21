/*
 * Copyright (C) 2026 Thomas Akehurst
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
import java.util.Map;

/**
 * Looks up a literal map key without interpreting dots, brackets or other path syntax.
 *
 * <p>Use {@code {{lookupSimple request.query 'things.something'}}} to read a value, or add {@code
 * assign='value'} to store it without converting its type. Missing and null values render as empty.
 */
public class LookupSimpleHelper extends HandlebarsHelper<Object> {

  @Override
  public Object apply(Object context, Options options) {
    if (options.params.length != 1) {
      return handleError("A single lookup key must be supplied");
    }

    Object key = options.param(0);
    if (key != null && !(key instanceof String)) {
      return handleError("Lookup key must be a string");
    }

    Object value = null;
    if (context != null) {
      if (!(context instanceof Map<?, ?> map)) {
        return handleError("Lookup input must be a map");
      }
      value = key == null ? null : map.get(key);
    }

    if (options.hash("assign") != null) {
      options.context.data(options.hash("assign"), value);
      return null;
    }

    return value;
  }
}
