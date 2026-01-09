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
 * Represents a full URI as defined in <a href="https://datatracker.ietf.org/doc/html/rfc3986">RFC
 * 3986</a>. The name {@link AbsoluteUri} is used in preference to Uri to maintain consistency with
 * the names {@link Url}, {@link AbsoluteUrl} and {@link RelativeUrl} elsewhere in the hierarchy,
 * which are chosen for familiarity to developers.
 *
 * <p>An {@link AbsoluteUri} is either an {@link AbsoluteUrl} or an {@link OpaqueUri}. {@link
 * AbsoluteUri}s are used to identify resources.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.1">RFC 3986 Section 4.1</a>
 */
public sealed interface AbsoluteUri extends Uri permits OpaqueUri, AbsoluteUrl {

  @Override
  Scheme getScheme();

  @Override
  default boolean isRelative() {
    return false;
  }

  @Override
  default boolean isAbsolute() {
    return true;
  }

  @Override
  AbsoluteUri normalise();

  default AbsoluteUri resolve(Uri other) {
    if (other instanceof AbsoluteUri otherUri) {
      return otherUri.normalise();
    } else {
      return transform(
          this,
          builder -> {
            Authority otherAuthority = other.getAuthority();
            Path otherPath = other.getPath();
            Query otherQuery = other.getQuery();
            Fragment otherFragment = other.getFragment();

            if (otherAuthority != null) {
              builder.setAuthority(otherAuthority.normalise());
              Path path = otherPath.isEmpty() ? Path.ROOT : otherPath.normalise();
              builder.setPath(path);
              builder.setQuery(otherQuery != null ? otherQuery.normalise() : null);
            } else {
              if (otherPath.isEmpty()) {
                builder.setPath(this.normalise().getPath());
                if (otherQuery != null) {
                  builder.setQuery(otherQuery.normalise());
                }
              } else {
                if (otherPath.isAbsolute()) {
                  builder.setPath(otherPath.normalise());
                } else {
                  builder.setPath(this.normalise().getPath().resolve(otherPath));
                }
                builder.setQuery(otherQuery != null ? otherQuery.normalise() : null);
              }
            }
            otherFragment = otherFragment != null ? otherFragment.normalise() : null;
            builder.setFragment(otherFragment);
          });
    }
  }

  static Builder builder(Scheme scheme) {
    return new AbsoluteUriBuilder(scheme);
  }

  static Builder builder(AbsoluteUri uri) {
    return new AbsoluteUriBuilder(uri);
  }

  static AbsoluteUri transform(AbsoluteUri uri, Consumer<Builder> consumer) {
    var builder = builder(uri);
    consumer.accept(builder);
    return builder.build();
  }

  static AbsoluteUri parse(String uriString) {
    return AbsoluteUriParser.INSTANCE.parse(uriString);
  }

  interface Builder {

    Builder setScheme(Scheme scheme);

    Builder setAuthority(@Nullable Authority authority);

    Builder setUserInfo(@Nullable UserInfo userInfo);

    Builder setHost(Host host);

    Builder setPort(@Nullable Port port);

    Builder setPath(Path path);

    Builder setQuery(@Nullable Query query);

    Builder setFragment(@Nullable Fragment fragment);

    AbsoluteUri build();
  }
}
