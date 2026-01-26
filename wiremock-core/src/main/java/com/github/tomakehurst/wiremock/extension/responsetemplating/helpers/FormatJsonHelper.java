/*
 * Copyright (C) 2024-2025 Thomas Akehurst
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
import com.github.tomakehurst.wiremock.common.JsonException;

/**
 * Handlebars helper to allow JSON to be formatted:
 *
 * <p>```handlebars {{#formatJson format='pretty'}} // Badly formatted JSON {{/formatJson}}```
 * `format` can be `pretty` or `compact` and defaults to `pretty`
 */
public class FormatJsonHelper extends AbstractFormattingHelper {

  @Override
  String getName() {
    return "formatJson";
  }

  @Override
  String getDataFormat() {
    return "JSON";
  }

  @Override
  protected String apply(String bodyText, Format format) {
    try {
      switch (format) {
        case pretty:
          return Json.prettyPrint(bodyText);
        case compact:
          return Json.node(bodyText).toString();
        default:
          throw new IllegalStateException();
      }
    } catch (JsonException e) {
      return handleError(
          "There was an error parsing the json. Please make sure the json is valid", e);
    }
  }
}
