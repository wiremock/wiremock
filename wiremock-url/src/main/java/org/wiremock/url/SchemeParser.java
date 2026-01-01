/*
 * Copyright (C) 2025 Thomas Akehurst
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
package org.wiremock.url;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

final class SchemeParser implements StringParser<Scheme> {

  static final SchemeParser INSTANCE = new SchemeParser();

  static final Pattern schemeRegex = Pattern.compile("[a-zA-Z][a-zA-Z0-9+\\-.]{0,255}");

  private final Map<String, Scheme> knownSchemes = new ConcurrentHashMap<>();

  Scheme register(String schemeString, @Nullable Port defaultPort) throws IllegalScheme {
    if (!schemeRegex.matcher(schemeString).matches()) {
      throw new IllegalScheme(schemeString);
    }
    String canonicalSchemeString = schemeString.toLowerCase();
    return knownSchemes.computeIfAbsent(
        canonicalSchemeString, (key) -> new SchemeValue(key, null, defaultPort));
  }

  @Override
  public Scheme parse(String schemeString) throws IllegalScheme {
    Scheme canonicalScheme = getCanonicalScheme(schemeString);
    if (canonicalScheme.toString().equals(schemeString)) {
      return canonicalScheme;
    } else {
      return new SchemeValue(schemeString, canonicalScheme, null);
    }
  }

  private Scheme getCanonicalScheme(String schemeString) throws IllegalScheme {
    String canonicalSchemeString = schemeString.toLowerCase();
    var existingCanonical = knownSchemes.get(canonicalSchemeString);
    if (existingCanonical != null) {
      return existingCanonical;
    } else if (!schemeRegex.matcher(schemeString).matches()) {
      throw new IllegalScheme(schemeString);
    } else {
      return new SchemeValue(canonicalSchemeString, null, null);
    }
  }
}
