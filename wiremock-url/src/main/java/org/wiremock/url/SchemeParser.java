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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

final class SchemeParser implements CharSequenceParser<Scheme> {

  static final SchemeParser INSTANCE = new SchemeParser();

  final Pattern schemeRegex = Pattern.compile("[a-zA-Z][a-zA-Z0-9+\\-.]{0,255}");

  private final Map<String, Scheme> knownSchemes = new ConcurrentHashMap<>();

  Scheme register(String schemeString, @Nullable Port defaultPort) throws IllegalScheme {
    if (!schemeRegex.matcher(schemeString).matches()) {
      throw new IllegalScheme(schemeString);
    }
    String canonicalSchemeString = schemeString.toLowerCase();
    return knownSchemes.computeIfAbsent(
        canonicalSchemeString, (key) -> new Scheme(key, null, defaultPort));
  }

  @Override
  public Scheme parse(CharSequence scheme) throws IllegalScheme {
    String schemeString = scheme.toString();
    Scheme canonicalScheme = getCanonicalScheme(schemeString);
    if (canonicalScheme.scheme.equals(schemeString)) {
      return canonicalScheme;
    } else {
      return new Scheme(schemeString, canonicalScheme, null);
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
      return new Scheme(canonicalSchemeString, null, null);
    }
  }

  static final class Scheme implements org.wiremock.url.Scheme {

    private final String scheme;

    @Nullable private final Scheme canonical;

    @Nullable private final Port defaultPort;

    Scheme(String scheme, @Nullable Scheme canonical, @Nullable Port defaultPort) {
      this.scheme = scheme;
      this.canonical = canonical;
      this.defaultPort = defaultPort;
    }

    @Override
    public org.wiremock.url.Scheme canonical() {
      return Objects.requireNonNullElse(canonical, this);
    }

    @Override
    public @Nullable Port defaultPort() {
      if (defaultPort != null) {
        return defaultPort;
      } else if (canonical != null) {
        return canonical.defaultPort();
      } else {
        return null;
      }
    }

    @Override
    public String toString() {
      return scheme;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof org.wiremock.url.Scheme other)) {
        return false;
      }
      return Objects.equals(this.scheme, other.toString());
    }

    @Override
    public int hashCode() {
      return scheme.hashCode();
    }
  }
}
