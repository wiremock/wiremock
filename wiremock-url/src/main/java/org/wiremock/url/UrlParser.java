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
  public Url parse(CharSequence url) throws IllegalUrl {
    var urlReference = UrlReferenceParser.INSTANCE.parse(url);
    if (urlReference instanceof Url) {
      return (Url) urlReference;
    } else {
      throw new IllegalUrl(url.toString());
    }
  }

  record Url(
      Scheme scheme,
      Authority authority,
      Path path,
      @Nullable Query query,
      @Nullable Fragment fragment,
      String asString)
      implements org.wiremock.url.Url {

    Url(
        Scheme scheme,
        Authority authority,
        Path path,
        @Nullable Query query,
        @Nullable Fragment fragment) {
      this(
          scheme,
          authority,
          path,
          query,
          fragment,
          stringValue(scheme, authority, path, query, fragment));
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
      public Url build() {
        return new Url(scheme, authority, path, query, fragment);
      }
    }

    @Override
    public String toString() {
      return asString;
    }

    private static String stringValue(
        Scheme scheme,
        Authority authority,
        Path path,
        @Nullable Query query,
        @Nullable Fragment fragment) {
      StringBuilder url =
          new StringBuilder().append(scheme).append("://").append(authority).append(path);
      if (query != null) {
        url.append('?').append(query);
      }
      if (fragment != null) {
        url.append('#').append(fragment);
      }
      return url.toString();
    }
  }
}
