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

  default Builder thaw() {
    return new UrlValue.Builder(this);
  }

  default Url transform(Consumer<Url.Builder> consumer) {
    Builder builder = this.thaw();
    consumer.accept(builder);
    return builder.build();
  }

  @Override
  Url normalise();

  default Url resolve(CharSequence other) {
    return resolve(parse(other));
  }

  default Url resolve(Path other) {
    return this.transform(builder -> builder.setPath(getPath().resolve(other)));
  }

  default Url resolve(UrlReference other) {
    if (other instanceof Url otherUrl) {
      return otherUrl.normalise();
    } else {
      return this.transform(
          builder -> {
            Authority otherAuthority = other.getAuthority();
            Query otherQuery = other.getQuery();
            if (otherAuthority != null) {
              builder.setAuthority(otherAuthority.normalise());
              Path path = other.getPath().isEmpty() ? Path.ROOT : other.getPath().normalise();
              builder.setPath(path);
              builder.setQuery(otherQuery == null ? null : otherQuery.normalise());
            } else {
              if (other.getPath().isEmpty()) {
                if (otherQuery != null) {
                  builder.setQuery(otherQuery.normalise());
                }
              } else {
                if (other.getPath().isAbsolute()) {
                  builder.setPath(other.getPath().normalise());
                } else {
                  builder.setPath(getPath().resolve(other.getPath()));
                }
                builder.setQuery(otherQuery == null ? null : otherQuery.normalise());
              }
            }
            Fragment otherFragment = other.getFragment();
            otherFragment = otherFragment == null ? null : otherFragment.normalise();
            builder.setFragment(otherFragment);
          });
    }
  }

  static Url parse(CharSequence url) throws IllegalUrl {
    return UrlParser.INSTANCE.parse(url);
  }

  static Builder builder(Scheme scheme, Authority authority) {
    return new UrlValue.Builder(scheme, authority);
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
