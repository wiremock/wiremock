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
import java.util.function.Consumer;

/**
 * Represents a URL reference, which is the subset of URI references that are <b>not</b> full
 * (absolute) URIs without an authority - what we call {@link OpaqueUri}s. The name {@link Url} is
 * used in preference to UrlReference because it is more familiar to developers, who habitually
 * think of both relative and complete URL references as URLs.
 *
 * <p>An {@link Url} is either an {@link AbsoluteUrl} or an {@link RelativeUrl}. An {@link
 * AbsoluteUrl} is guaranteed to resolve to an {@link AbsoluteUrl} if resolved against an {@link
 * Url}, whereas it may resolve to an {@link OpaqueUri} if resolved against an {@link Uri}.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see Uri
 * @see AbsoluteUrl
 * @see RelativeUrl
 */
public sealed interface Url extends Uri permits RelativeUrl, AbsoluteUrl {

  /**
   * Returns the path and query components combined.
   *
   * @return the path and query
   */
  default PathAndQuery getPathAndQuery() {
    return new PathAndQueryValue(getPath(), getQuery());
  }

  /**
   * Creates a builder initialized with the values from this URL.
   *
   * @return a builder
   */
  default Builder thaw() {
    return builder(this);
  }

  default Url transform(Consumer<Mutator> mutator) {
    var builder = thaw();
    mutator.accept(builder);
    return builder.build();
  }

  /**
   * Creates a new builder with the given scheme and authority.
   *
   * @return a new builder
   */
  static Builder builder() {
    return new UrlBuilder();
  }

  /**
   * Creates a builder initialized with the values from the given URL.
   *
   * @param url the URL to copy values from
   * @return a new builder
   */
  static Builder builder(Url url) {
    return new UrlBuilder(url);
  }

  interface Mutator extends Uri.Mutator {

    Mutator setScheme(Scheme scheme);

    Mutator setAuthority(Authority authority);

    Mutator setUserInfo(@Nullable UserInfo userInfo);

    Mutator setHost(Host host);

    Mutator setPort(@Nullable Port port);

    Mutator setPath(Path path);

    Mutator setQuery(@Nullable Query query);

    Mutator setFragment(@Nullable Fragment fragment);
  }

  interface Builder extends Url.Mutator {

    Builder setUserInfo(@Nullable UserInfo userInfo);

    Builder setHost(Host host);

    Builder setPort(@Nullable Port port);

    Builder setPath(Path path);

    Builder setQuery(@Nullable Query query);

    Builder setFragment(@Nullable Fragment fragment);

    Url build();
  }

  /**
   * Parses a string into a URI reference.
   *
   * @param url the string to parse
   * @return the parsed URI reference
   * @throws IllegalUri if the string is not a valid URI reference
   */
  static Url parse(String url) throws IllegalUri {
    return UrlParser.INSTANCE.parse(url);
  }
}
