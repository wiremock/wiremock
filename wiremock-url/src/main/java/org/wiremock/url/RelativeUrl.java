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

import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * Represents a relative reference as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.2">RFC 3986 Section 4.2</a>.
 *
 * <p>A relative reference is a URI reference that does not have a scheme component. It can have an
 * authority, path, query, and fragment. Relative references are typically resolved against a base
 * URI to produce another URI.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.2">RFC 3986 Section 4.2</a>
 */
public non-sealed interface RelativeUrl extends Url {

  /**
   * {@implSpec} Implementations must ALWAYS return null
   *
   * @deprecated This always returns null so you have no reason to ever call it
   * @return null
   */
  @Override
  @Nullable
  @Deprecated // no point ever calling on this subtype
  default Scheme getScheme() {
    return null;
  }

  @Override
  default boolean isRelative() {
    return true;
  }

  @Override
  default boolean isAbsolute() {
    return false;
  }

  @Override
  default boolean isAbsoluteUrl() {
    return false;
  }

  @Override
  default boolean isOpaqueUri() {
    return false;
  }

  /**
   * Returns a normalised form of this relative reference.
   *
   * @return a normalised relative reference
   */
  @Override
  RelativeUrl normalise();

  /**
   * Parses a string into a relative reference.
   *
   * @param relativeRef the string to parse
   * @return the parsed relative reference
   * @throws IllegalRelativeUrl if the string is not a valid relative reference
   */
  static RelativeUrl parse(String relativeRef) throws IllegalRelativeUrl {
    return RelativeUrlParser.INSTANCE.parse(relativeRef);
  }

  /**
   * Creates a builder initialized with the values from this URL.
   *
   * @return a builder
   */
  default Builder thaw() {
    return builder(this);
  }

  /**
   * Transforms this URL by applying modifications via a builder.
   *
   * @param consumer a function that modifies the builder
   * @return the transformed URL
   */
  default RelativeUrl transform(Consumer<Builder> consumer) {
    var builder = thaw();
    consumer.accept(builder);
    return builder.build();
  }

  /**
   * Creates a new builder
   *
   * @return a new builder
   */
  static Builder builder() {
    return new RelativeUrlBuilder();
  }

  /**
   * Creates a builder initialized with the values from the given URL.
   *
   * @param url the URL to copy values from
   * @return a new builder
   */
  static Builder builder(RelativeUrl url) {
    return new RelativeUrlBuilder(url);
  }

  interface Builder extends Uri.Mutator {

    Builder setAuthority(@Nullable Authority authority);

    Builder setUserInfo(@Nullable UserInfo userInfo);

    Builder setHost(Host host);

    Builder setPort(@Nullable Port port);

    Builder setPath(Path path);

    Builder setQuery(@Nullable Query query);

    Builder setFragment(@Nullable Fragment fragment);

    RelativeUrl build();
  }
}
