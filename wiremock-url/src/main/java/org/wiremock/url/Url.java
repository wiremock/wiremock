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
public sealed interface Url extends Uri permits RelativeUrl, UrlWithAuthority {

  /**
   * Returns the path and query components combined.
   *
   * @return the path and query
   */
  default PathAndQuery getPathAndQuery() {
    return new PathAndQueryValue(getPath(), getQuery());
  }

  /**
   * Creates a transformer initialized with the values from this URL.
   *
   * @return a transformer
   */
  default Url.Transformer<?> thaw() {
    return new UrlTransformer(this);
  }

  /**
   * Convenience method for transforming a Url
   *
   * @param mutator a function to mutate the transformer
   * @return the transformed Url
   * @throws IllegalUrl if scheme is set and Authority is not set
   */
  default Url transform(Consumer<Uri.Transformer<?>> mutator) {
    var transformer = thaw();
    mutator.accept(transformer);
    return transformer.build();
  }

  /**
   * Creates a new builder
   *
   * @return a new builder
   */
  static Url.Builder builder() {
    return new UrlBuilder();
  }

  /**
   * Creates a builder initialized with the values from the given URL.
   *
   * @param url the URL to copy values from
   * @return a new builder
   */
  static Url.Builder builder(Url url) {
    return new UrlBuilder(url);
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

  interface Builder extends UriBaseBuilder<Builder> {

    Url.Builder setScheme(@Nullable Scheme scheme);

    @Override
    Url.Builder setAuthority(@Nullable Authority authority);

    /**
     * @return the built Url
     * @throws IllegalUrl if scheme is set and Authority is not set
     */
    @Override
    Url build() throws IllegalUrl;
  }

  interface Transformer<SELF extends Transformer<SELF>> extends Uri.Transformer<SELF> {

    /**
     * @return the transformed Url
     * @throws IllegalUrl if scheme is set and Authority is not set
     */
    @Override
    Url build() throws IllegalUrl;
  }
}
