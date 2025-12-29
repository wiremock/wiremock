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

import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public non-sealed interface Url extends Uri, UrlReference {

  @Override
  Authority getAuthority();

  default boolean isRelativeRef() {
    return false;
  }

  default boolean isUrl() {
    return true;
  }

  default boolean isUrn() {
    return false;
  }

  @Override
  default Host getHost() {
    return getAuthority().getHost();
  }

  default boolean isAbsolute() {
    return getFragment() == null;
  }

  default Origin origin() {
    return Origin.of(getScheme(), getAuthority().getHostAndPort());
  }

  default PathAndQuery getPathAndQuery() {
    return new PathAndQueryValue(getPath(), getQuery());
  }

  @Override
  Url normalise();

  default Url resolve(CharSequence other) {
    return resolve(parse(other));
  }

  default Url resolve(Path other) {
    return transform(this, builder -> builder.setPath(getPath().resolve(other)));
  }

  default Url resolve(UrlReference other) {
    return (Url) resolve((UriReference) other);
  }

  default Builder thaw() {
    return builder(this);
  }

  default Url transform(Consumer<Builder> consumer) {
    return transform(this, consumer);
  }

  static Url parse(CharSequence url) throws IllegalUrl {
    return UrlParser.INSTANCE.parse(url);
  }

  static Builder builder(Scheme scheme, Authority authority) {
    return new UrlBuilder(scheme, authority);
  }

  static Builder builder(Url url) {
    return new UrlBuilder(url);
  }

  static Url transform(Url uri, Consumer<Builder> consumer) {
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

    Url build();
  }
}
