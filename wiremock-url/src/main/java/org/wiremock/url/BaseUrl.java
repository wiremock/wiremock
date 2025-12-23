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

public interface BaseUrl extends Url {

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default PathAndQuery pathAndQuery() {
    return PathAndQuery.EMPTY;
  }

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  default Path path() {
    return Path.EMPTY;
  }

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  @Nullable
  default Query query() {
    return null;
  }

  @Override
  @Deprecated(forRemoval = true) // not actually for removal, just no point ever calling
  @Nullable
  default Fragment fragment() {
    return null;
  }

  @Override
  Url normalise();

  static BaseUrl of(Scheme scheme, Authority authority) {
    return new BaseUrlParser.BaseUrl(scheme, authority);
  }

  static BaseUrl parse(String baseUrl) {
    return BaseUrlParser.INSTANCE.parse(baseUrl);
  }
}

class BaseUrlParser implements CharSequenceParser<BaseUrl> {

  static final BaseUrlParser INSTANCE = new BaseUrlParser();

  @Override
  public org.wiremock.url.BaseUrl parse(CharSequence url) throws IllegalBaseUrl {
    try {
      var urlReference = UrlReferenceParser.INSTANCE.parse(url);
      if (urlReference instanceof org.wiremock.url.BaseUrl) {
        return (org.wiremock.url.BaseUrl) urlReference;
      } else {
        throw new IllegalBaseUrl(url.toString());
      }
    } catch (IllegalUrlPart illegalUrlPart) {
      throw new IllegalBaseUrl(url.toString(), illegalUrlPart);
    }
  }

  record BaseUrl(@Override Scheme scheme, @Override Authority authority)
      implements org.wiremock.url.BaseUrl {

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

    @Override
    public Url normalise() {
      Scheme canonicalScheme = scheme.canonical();
      Authority normalisedAuthority = authority.normalise(canonicalScheme);
      return Url.builder(canonicalScheme, normalisedAuthority).build();
    }
  }
}
