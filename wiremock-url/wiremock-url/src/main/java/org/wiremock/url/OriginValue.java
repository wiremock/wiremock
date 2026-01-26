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

import static java.util.Objects.requireNonNull;

import org.jspecify.annotations.Nullable;

final class OriginValue extends AbstractAbsoluteUrlValue<ServersideAbsoluteUrl> implements Origin {

  private final HostAndPort hostAndPort;

  OriginValue(@Nullable String stringValue, Scheme scheme, HostAndPort authority) {
    super(stringValue, scheme, authority, Path.EMPTY, null, null);
    this.hostAndPort = requireNonNull(authority);
  }

  OriginValue(Scheme scheme, HostAndPort authority) {
    this(null, scheme, authority);
  }

  @Override
  public ServersideAbsoluteUrl normalise() {
    return new ServersideAbsoluteUrlValue(nonNullScheme, hostAndPort, Path.ROOT, null);
  }

  @Override
  public HostAndPort getAuthority() {
    return hostAndPort;
  }
}
