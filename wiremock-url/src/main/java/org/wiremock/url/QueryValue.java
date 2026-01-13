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

final class QueryValue implements Query {

  private final String query;
  private final boolean isNormalForm;

  QueryValue(String query) {
    this(query, false);
  }

  QueryValue(String query, boolean isNormalForm) {
    this.query = query;
    this.isNormalForm = isNormalForm;
  }

  @Override
  public String toString() {
    return query;
  }

  @Override
  public Query normalise() {
    if (isNormalForm) {
      return this;
    }

    String result = Constants.normalise(query, QueryParser.queryCharSet);

    if (result == null) {
      return this;
    } else {
      return new QueryValue(result, true);
    }
  }

  @Override
  public boolean isNormalForm() {
    return isNormalForm || Constants.isNormalForm(query, QueryParser.queryCharSet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Query that) {
      return Objects.equals(this.toString(), that.toString());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(query);
  }
}
