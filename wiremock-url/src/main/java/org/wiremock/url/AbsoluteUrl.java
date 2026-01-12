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
 * Represents a Uniform Resource Locator (URL) as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986">RFC 3986</a>. The name {@link AbsoluteUrl}
 * is used in preference to Url because it is more familiar to developers, who habitually think of
 * both relative and complete URL references as URLs and use absolute URL to mean the latter. Note
 * that this is <b>not</b> an absolute URL in the RFC 3986 sense of a complete URI with no fragment.
 *
 * <p>An {@link AbsoluteUrl} consists of a scheme, authority (host and optional port and user info),
 * path, optional query, and optional fragment. {@link AbsoluteUrl}s always have both a scheme and
 * an authority component.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986">RFC 3986</a>
 */
public non-sealed interface AbsoluteUrl extends AbsoluteUri, Url {

  /**
   * Returns the authority component of this URL.
   *
   * <p>URLs always have an authority component (unlike relative references and URNs).
   *
   * @return the authority component, never {@code null}
   */
  @Override
  Authority getAuthority();

  @Override
  default boolean isRelative() {
    return false;
  }

  @Override
  default boolean isAbsoluteUrl() {
    return true;
  }

  @Override
  default boolean isOpaqueUri() {
    return false;
  }

  @Override
  default Host getHost() {
    return getAuthority().getHost();
  }

  /**
   * Returns the origin of this URL, consisting of the scheme, host, and port.
   *
   * @return the origin
   */
  default Origin getOrigin() {
    return Origin.of(getScheme(), getAuthority().getHostAndPort());
  }

  /**
   * Returns a normalised form of this URL.
   *
   * @return a normalised URL
   */
  @Override
  AbsoluteUrl normalise();

  /**
   * Resolves the given string as a URI reference against this URL.
   *
   * @param other the URI reference to resolve
   * @return the resolved URL
   * @throws IllegalAbsoluteUrl if the string is not a valid URI reference
   */
  default AbsoluteUrl resolve(String other) {
    return resolve(parse(other));
  }

  /**
   * Resolves the given path against this URL.
   *
   * @param other the path to resolve
   * @return the URL with the resolved path
   */
  default AbsoluteUrl resolve(Path other) {
    return transform(this, builder -> builder.setPath(getPath().resolve(other)));
  }

  /**
   * Resolves the given URL reference against this URL.
   *
   * @param other the URL reference to resolve
   * @return the resolved URL
   */
  default AbsoluteUrl resolve(Url other) {
    return (AbsoluteUrl) resolve((Uri) other);
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
  default AbsoluteUrl transform(Consumer<Builder> consumer) {
    return transform(this, consumer);
  }

  /**
   * Parses a string into a URL.
   *
   * @param url the string to parse
   * @return the parsed URL
   * @throws IllegalAbsoluteUrl if the string is not a valid URL
   */
  static AbsoluteUrl parse(String url) throws IllegalAbsoluteUrl {
    return UrlParser.INSTANCE.parse(url);
  }

  /**
   * Creates a new builder with the given scheme and authority.
   *
   * @param scheme the scheme
   * @param authority the authority
   * @return a new builder
   */
  static Builder builder(Scheme scheme, Authority authority) {
    return new AbsoluteUrlBuilder(scheme, authority);
  }

  /**
   * Creates a builder initialized with the values from the given URL.
   *
   * @param url the URL to copy values from
   * @return a new builder
   */
  static Builder builder(AbsoluteUrl url) {
    return new AbsoluteUrlBuilder(url);
  }

  /**
   * Transforms a URL by applying modifications via a builder.
   *
   * @param uri the URL to transform
   * @param consumer a function that modifies the builder
   * @return the transformed URL
   */
  static AbsoluteUrl transform(AbsoluteUrl uri, Consumer<Builder> consumer) {
    var builder = builder(uri);
    consumer.accept(builder);
    return builder.build();
  }

  interface Builder {

    Builder setScheme(Scheme scheme);

    Builder setAuthority(Authority authority);

    Builder setUserInfo(@Nullable UserInfo userInfo);

    Builder setHost(Host host);

    Builder setPort(@Nullable Port port);

    Builder setPath(Path path);

    Builder setQuery(@Nullable Query query);

    Builder setFragment(@Nullable Fragment fragment);

    AbsoluteUrl build();
  }
}
