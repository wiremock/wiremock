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

final class OriginValue implements Origin {

  private final Scheme scheme;
  private final HostAndPort authority;

  OriginValue(Scheme scheme, HostAndPort authority) {
    this.scheme = scheme;
    this.authority = authority;
  }

  @Override
  @SuppressWarnings("EqualsDoesntCheckParameterClass")
  public boolean equals(Object obj) {
    return UrlReferenceParser.equals(this, obj);
  }

  @Override
  public int hashCode() {
    return UrlReferenceParser.hashCode(this);
  }

  @Override
  public String toString() {
    return UrlReferenceParser.toString(this);
  }

  @Override
  public Url normalise() {
    Scheme canonicalScheme = scheme.canonical();
    Authority normalisedAuthority = authority.normalise(canonicalScheme);
    return Url.builder(canonicalScheme, normalisedAuthority).build();
  }

  @Override
  public Scheme scheme() {
    return scheme;
  }

  @Override
  public HostAndPort authority() {
    return authority;
  }
}
