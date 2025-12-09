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

final class PathAndQueryParser implements CharSequenceParser<PathAndQuery> {

  @Override
  public org.wiremock.url.PathAndQuery parse(CharSequence stringForm) {
    try {
      var urlReference = UrlReferenceParser.INSTANCE.parse(stringForm);
      if (urlReference instanceof org.wiremock.url.PathAndQuery) {
        return (org.wiremock.url.PathAndQuery) urlReference;
      } else {
        throw new IllegalPathAndQuery(stringForm.toString());
      }
    } catch (IllegalUrlPart illegalUrlPart) {
      throw new IllegalPathAndQuery(stringForm.toString(), illegalUrlPart);
    }
  }

  record PathAndQuery(Path path, @Nullable Query query) implements org.wiremock.url.PathAndQuery {

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
    public PathAndQuery normalise() {
      return this;
    }
  }
}
