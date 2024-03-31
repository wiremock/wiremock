/*
 * Copyright (C) 2018-2024 Thomas Akehurst
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
import java.util.Map;
import java.util.UUID;

public class HandlebarsRandomValuesHelper extends HandlebarsHelper<Void> {

  @Override
  public Object apply(Void context, Options options) throws IOException {
    int length = options.hash("length", 36);
    boolean uppercase = options.hash("uppercase", false);

    String type = options.hash("type", "ALPHANUMERIC");

    String rawValue = getString(type, length);

    return uppercase ? rawValue.toUpperCase() : rawValue.toLowerCase();
  }

  private static String getString(String type, int length) {
    Map<String, RandomStringGenerator> generators = Map.ofEntries(
            Map.entry("ALPHANUMERIC", new AlphanumericGenerator()),
            Map.entry("ALPHABETIC", new AlphabeticGenerator()),
            Map.entry("NUMERIC", new NumericGenerator()),
            Map.entry("ALPHANUMERIC_AND_SYMBOLS", new AplhanumericAndSymbolsGenerator()),
            Map.entry("UUID", new UUIDGenerator()),
            Map.entry("HEXADECIMAL", new HexadecimalGenerator()),
            Map.entry("DEFAULT", new DefautGenerator())
    );

    RandomStringGenerator stringGenerator = generators.get(type);
    String rawValue = stringGenerator.generate(length);
    return rawValue;
  }
}
