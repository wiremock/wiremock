/*
 * Copyright (C) 2018-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Strings.*;

import com.github.jknack.handlebars.Options;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class HandlebarsRandomValuesHelper extends HandlebarsHelper<Void> {

  @Override
  public Object apply(Void context, Options options) throws IOException {
    int length = options.hash("length", 36);
    boolean uppercase = options.hash("uppercase", false);

    String type = options.hash("type", "ALPHANUMERIC");
    String rawValue =
        switch (type) {
          case "ALPHANUMERIC" -> randomAlphanumeric(length);
          case "ALPHABETIC" -> randomAlphabetic(length);
          case "NUMERIC" -> randomNumeric(length);
          case "ALPHANUMERIC_AND_SYMBOLS" -> random(length, 33, 126, false, false);
          case "UUID" -> UUID.randomUUID().toString();
          case "HEXADECIMAL" -> random(length, "ABCDEF0123456789");
          default -> randomAscii(length);
        };
    return uppercase ? rawValue.toUpperCase(Locale.ROOT) : rawValue.toLowerCase(Locale.ROOT);
  }
}
