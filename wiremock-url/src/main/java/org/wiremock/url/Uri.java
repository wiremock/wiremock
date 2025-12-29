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

public sealed interface Uri extends UriReference permits Urn, Url {

  @Override
  Scheme getScheme();

  @Override
  default boolean isRelativeRef() {
    return false;
  }

  @Override
  default boolean isUri() {
    return true;
  }

  @Override
  Uri normalise();

  default Uri resolve(UriReference other) {
    if (other instanceof Uri otherUri) {
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
                if (otherQuery != null) {
                  builder.setQuery(otherQuery.normalise());
                }
              } else {
                if (otherPath.isAbsolute()) {
                  builder.setPath(otherPath.normalise());
                } else {
                  builder.setPath(getPath().resolve(otherPath));
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
    return new UriBuilder(scheme);
  }

  static Builder builder(Uri uri) {
    return new UriBuilder(uri);
  }

  static Uri transform(Uri uri, Consumer<Builder> consumer) {
    var builder = builder(uri);
    consumer.accept(builder);
    return builder.build();
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

    Uri build();
  }
}
