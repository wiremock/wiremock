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

import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

class RelativeRefParser implements CharSequenceParser<RelativeRef> {

  @Override
  public org.wiremock.url.RelativeRef parse(CharSequence stringForm) {
    try {
      var urlReference = UrlReferenceParser.INSTANCE.parse(stringForm);
      if (urlReference instanceof org.wiremock.url.RelativeRef) {
        return (org.wiremock.url.RelativeRef) urlReference;
      } else {
        throw new IllegalBaseUrl(stringForm.toString());
      }
    } catch (IllegalUrlPart illegalUrlPart) {
      throw new IllegalBaseUrl(stringForm.toString(), illegalUrlPart);
    }
  }

  record RelativeRef(
      @Override @Nullable Authority authority,
      @Override Path path,
      @Override @Nullable Query query,
      @Override @Nullable Fragment fragment)
      implements org.wiremock.url.RelativeRef {

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
    public RelativeRef normalise() {
      Authority normalisedAuthority =
          Optional.ofNullable(authority).map(Authority::normalise).orElse(null);
      Path normalisedPath = path.normalise();
      if (normalisedPath.isEmpty()) {
        normalisedPath = Path.ROOT;
      }
      Query normalisedQuery = query == null ? null : query.normalise();
      Fragment normalisedFragment = fragment == null ? null : fragment.normalise();
      if (Objects.equals(normalisedAuthority, authority)
          && Objects.equals(normalisedPath, path)
          && Objects.equals(normalisedQuery, query)
          && Objects.equals(normalisedFragment, fragment)) {
        return this;
      } else {
        return new RelativeRef(
            normalisedAuthority, normalisedPath, normalisedQuery, normalisedFragment);
      }
    }
  }
}
