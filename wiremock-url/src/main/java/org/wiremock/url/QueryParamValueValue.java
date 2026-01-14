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

class QueryParamValueValue implements QueryParamValue {

  private final String stringForm;
  private final boolean isNormalForm;

  public QueryParamValueValue(String stringForm) {
    this(stringForm, false);
  }

  QueryParamValueValue(String stringForm, boolean isNormalForm) {
    this.stringForm = stringForm;
    this.isNormalForm = isNormalForm;
  }

  @Override
  public String toString() {
    return stringForm;
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
    if (isNormalForm) {
      return this;
    }

    String result = Constants.normalise(stringForm, QueryParamValueParser.queryParamValueCharSet);

    if (result == null) {
      return this;
    } else {
      return new QueryParamValueValue(result, true);
    }
  }

  @Override
  public boolean isNormalForm() {
    return isNormalForm
        || Constants.isNormalForm(stringForm, QueryParamValueParser.queryParamValueCharSet);
  }
}
