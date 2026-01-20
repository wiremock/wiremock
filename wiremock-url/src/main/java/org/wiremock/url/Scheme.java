/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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

import org.jspecify.annotations.Nullable;
import org.wiremock.stringparser.ParsedString;

/**
 * Represents a valid URI scheme.
 *
 * <p>An implementation must be immutable (and hence threadsafe)
 *
 * <p>Implementations must enforce that the scheme is valid
 *
 * <p>Implementations should be equal to any other Scheme implementation with the same text
 * representation. Implementations are <b>NOT</b> equal to a scheme with different casing - so
 * {@code Scheme.parse("http").equals(Scheme.parse("HTTP")) == false}. However, their canonical
 * representations are equal, so {@code
 * Scheme.parse("http").canonical().equals(Scheme.parse("HTTP").canonical()) == true }.
 *
 * <p>An implementation's toString should return the String used when it was created.
 */
public interface Scheme extends ParsedString {

  Scheme normalise();

  @Nullable Port getDefaultPort();

  default boolean isNormalForm() {
    return normalise().equals(this);
  }

  default AbsoluteUri resolve(Uri other) {
    return asUri().resolve(other);
  }

  default AbsoluteUrl resolve(UrlWithAuthority other) {
    return asUri().resolve(other);
  }

  private OpaqueUri asUri() {
    return OpaqueUri.of(this, Path.EMPTY);
  }

  /**
   * Parses &amp; registers a scheme
   *
   * <p>Unlike register, returns a scheme with the same casing as the scheme param, but {@code
   * canonical()} will return the canonical (lower case) version, which may already have been
   * registered.
   *
   * <p>If the scheme parameter is canonical (lower case), and a matching Scheme with a default port
   * has already been registered, that Scheme will be returned.
   *
   * @param scheme - the raw scheme
   * @return a Scheme object representing the scheme
   * @throws IllegalScheme if the raw scheme is not a legal Scheme, matching {@code
   *     [a-zA-Z][a-zA-Z0-9+\-.]{0,255}}
   */
  static Scheme parse(String scheme) throws IllegalScheme {
    return SchemeRegistry.INSTANCE.parse(scheme);
  }

  /**
   * Registers a canonical scheme with no default port.
   *
   * <p>The registered and returned scheme will be canonical (i.e. lower case) regardless of the
   * case of the input.
   *
   * <p>If the scheme is already registered, returns the existing instance with the existing
   * instances default port (or none).
   *
   * @param schemeString - the raw scheme
   * @return a canonical Scheme object representing the scheme
   * @throws IllegalScheme if the raw scheme is not a legal Scheme, matching {@code
   *     [a-zA-Z][a-zA-Z0-9+\-.]{0,255}}
   */
  static Scheme register(String schemeString) throws IllegalScheme {
    return register(schemeString, null);
  }

  /**
   * Registers a scheme with an optional default port.
   *
   * <p>The registered and returned scheme will be canonical (i.e. lower case) regardless of input.
   *
   * <p>If the scheme is already registered, returns the existing instance and ignores the provided
   * default port.
   *
   * <p>The returned scheme will be canonical (i.e. lower case)
   *
   * @param schemeString - the raw scheme
   * @return a canonical Scheme object representing the scheme
   * @throws IllegalScheme if the raw scheme is not a legal Scheme, matching {@code
   *     [a-zA-Z][a-zA-Z0-9+\-.]{0,255}}
   */
  static Scheme register(String schemeString, @Nullable Port defaultPort) throws IllegalScheme {
    return SchemeRegistry.INSTANCE.register(schemeString, defaultPort);
  }
}
