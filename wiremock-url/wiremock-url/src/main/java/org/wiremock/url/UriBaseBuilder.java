/*
 * Copyright (C) 2026 Thomas Akehurst
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

public interface UriBaseBuilder<SELF extends UriBaseBuilder<SELF>> {

  @Nullable Authority getAuthority();

  SELF setAuthority(Authority authority);

  @Nullable UserInfo getUserInfo();

  SELF setUserInfo(@Nullable UserInfo userInfo);

  @Nullable Host getHost();

  SELF setHost(Host host);

  @Nullable Port getPort();

  SELF setPort(@Nullable Port port);

  Path getPath();

  SELF setPath(Path path);

  @Nullable Query getQuery();

  SELF setQuery(@Nullable Query query);

  Query.Builder getQueryBuilder();

  SELF setQueryBuilder(Query.Builder query);

  @Nullable Fragment getFragment();

  SELF setFragment(@Nullable Fragment fragment);

  Uri build();
}
