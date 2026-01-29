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

import java.util.Objects;
import org.jspecify.annotations.Nullable;

final class QueryParamValueValue implements QueryParamValue {

  private final String stringForm;
  private final MemoisedNormalisable<QueryParamValue> memoisedNormalisable;

  public QueryParamValueValue(String stringForm) {
    this(stringForm, null);
  }

  QueryParamValueValue(String stringForm, @Nullable Boolean isNormalForm) {
    this.stringForm = stringForm;
    this.memoisedNormalisable =
        new MemoisedNormalisable<>(this, isNormalForm, this::isNormalFormWork, this::normaliseWork);
  }

  @Override
  public String toString() {
    return stringForm;
  }

  @Override
  public String decode() {
    return PercentEncoding.decode(stringForm.replace('+', ' '));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof QueryParamValue that) {
      return Objects.equals(this.toString(), that.toString());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return stringForm.hashCode();
  }

  @Override
  public QueryParamValue normalise() {
    return memoisedNormalisable.normalise();
  }

  private @Nullable QueryParamValue normaliseWork() {
    String result =
        PercentEncoding.normalise(
            stringForm.replace('+', ' '), QueryParamValueParser.queryParamValueCharSet);
    return result != null ? new QueryParamValueValue(result, true) : null;
  }

  @Override
  public boolean isNormalForm() {
    return memoisedNormalisable.isNormalForm();
  }

  private boolean isNormalFormWork() {
    return PercentEncoding.isNormalForm(stringForm, QueryParamValueParser.queryParamValueCharSet);
  }
}
