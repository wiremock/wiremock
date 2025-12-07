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

final class UrlParser implements CharSequenceParser<Url> {

  static final UrlParser INSTANCE = new UrlParser();

  @Override
  public org.wiremock.url.Url parse(CharSequence url) throws IllegalUrl {
    try {
      var urlReference = UrlReferenceParser.INSTANCE.parse(url);
      if (urlReference instanceof org.wiremock.url.Url) {
        return (org.wiremock.url.Url) urlReference;
      } else {
        throw new IllegalUrl(url.toString());
      }
    } catch (IllegalUrlPart illegalUrlPart) {
      throw new IllegalUrl(url.toString(), illegalUrlPart);
    }
  }

  record Url(
      Scheme scheme,
      Authority authority,
      Path path,
      @Nullable Query query,
      @Nullable Fragment fragment)
      implements org.wiremock.url.Url {

    @Override
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

    static class Builder implements org.wiremock.url.Url.Builder {

      private Scheme scheme;
      private Authority authority;
      private Path path = Path.EMPTY;
      @Nullable Query query = null;
      @Nullable Fragment fragment = null;

      Builder(Scheme scheme, Authority authority) {
        this.scheme = scheme;
        this.authority = authority;
      }

      Builder(org.wiremock.url.Url url) {
        this.scheme = url.scheme();
        this.authority = url.authority();
        this.path = url.path();
        this.query = url.query();
        this.fragment = url.fragment();
      }

      @Override
      public Builder setScheme(Scheme scheme) {
        this.scheme = scheme;
        return this;
      }

      @Override
      public Builder setAuthority(Authority authority) {
        this.authority = authority;
        return this;
      }

      @Override
      public Builder setUserInfo(@Nullable UserInfo userInfo) {
        this.authority = Authority.of(userInfo, authority.host(), authority.port());
        return this;
      }

      @Override
      public org.wiremock.url.Url.Builder setHost(Host host) {
        this.authority = Authority.of(authority.userInfo(), host, authority.port());
        return this;
      }

      @Override
      public org.wiremock.url.Url.Builder setPort(@Nullable Port port) {
        this.authority = Authority.of(authority.userInfo(), authority.host(), port);
        return this;
      }

      @Override
      public Builder setPath(Path path) {
        this.path = path;
        return this;
      }

      @Override
      public Builder setQuery(@Nullable Query query) {
        this.query = query;
        return this;
      }

      @Override
      public Builder setFragment(@Nullable Fragment fragment) {
        this.fragment = fragment;
        return this;
      }

      @Override
      public org.wiremock.url.Url build() {
        if (path.isEmpty() && query == null && fragment == null) {
          return new BaseUrlParser.BaseUrl(scheme, authority);
        } else {
          return new Url(scheme, authority, path, query, fragment);
        }
      }
    }
  }
}
