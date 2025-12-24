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

import org.jspecify.annotations.Nullable;

public sealed interface UriReference permits Uri, UrlReference {

  @Nullable Scheme getScheme();

  @Nullable Authority getAuthority();

  Path getPath();

  @Nullable Query getQuery();

  @Nullable Fragment getFragment();

  boolean isRelativeRef();

  boolean isUri();

  boolean isUrl();

  boolean isUrn();

  default @Nullable UserInfo getUserInfo() {
    Authority authority = getAuthority();
    return authority != null ? authority.getUserInfo() : null;
  }

  default @Nullable Host getHost() {
    Authority authority = getAuthority();
    return authority != null ? authority.getHost() : null;
  }

  default @Nullable Port getPort() {
    Authority authority = getAuthority();
    return authority != null ? authority.getPort() : null;
  }

  default @Nullable Port getResolvedPort() {
    Port definedPort = getPort();
    Scheme scheme = getScheme();
    return definedPort != null ? definedPort : (scheme != null ? scheme.getDefaultPort() : null);
  }

  UriReference normalise();

  static UriReference parse(CharSequence urlReference) throws IllegalUriReference {
    return UriReferenceParser.INSTANCE.parse(urlReference);
  }
}
