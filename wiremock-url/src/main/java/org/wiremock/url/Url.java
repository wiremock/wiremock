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

@SuppressWarnings("unused")
public non-sealed interface Url extends UrlReference {

  @Override
  Scheme scheme();

  @Override
  Authority authority();

  default boolean isRelativeRef() {
    return false;
  }

  default boolean isUrl() {
    return true;
  }

  @Override
  default Host host() {
    return authority().host();
  }

  default boolean isAbsolute() {
    return fragment() == null;
  }

  default Url baseUrl() {
    return transform(builder -> builder.setPath(Path.EMPTY).setQuery(null).setFragment(null));
  }

  default PathAndQuery pathAndQuery() {
    return new PathAndQueryParser.PathAndQuery(path(), query());
  }

  default Builder thaw() {
    return new UrlParser.Url.Builder(this);
  }

  default Url transform(Consumer<Url.Builder> consumer) {
    Builder builder = this.thaw();
    consumer.accept(builder);
    return builder.build();
  }

  default Url withPort(@Nullable Port port) {
    return transform(builder -> builder.setAuthority(authority().withPort(port)));
  }

  default Url withoutPort() {
    return withPort(null);
  }

  static Url parse(CharSequence url) throws IllegalUrl {
    return UrlParser.INSTANCE.parse(url);
  }

  static Builder builder(Scheme scheme, Authority authority) {
    return new UrlParser.Url.Builder(scheme, authority);
  }

  interface Builder {

    Builder setScheme(Scheme scheme);

    Builder setAuthority(Authority authority);

    Builder setPath(Path path);

    Builder setQuery(@Nullable Query query);

    Builder setFragment(@Nullable Fragment fragment);

    Url build();
  }
}
