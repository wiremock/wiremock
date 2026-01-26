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

import org.jspecify.annotations.Nullable;

record UriParseTestCase(String stringForm, UriExpectation expectation) {
  static UriParseTestCase testCase(String stringForm, UriExpectation expectation) {
    return new UriParseTestCase(stringForm, expectation);
  }
}

record UriExpectation(
    @Nullable Scheme scheme,
    @Nullable Authority authority,
    @Nullable Path path,
    @Nullable Query query,
    @Nullable Fragment fragment) {

  static UriExpectation expectation(
      @Nullable String schemeStr,
      @Nullable String authorityStr,
      String pathStr,
      @Nullable String queryStr,
      @Nullable String fragmentStr) {
    Scheme scheme = schemeStr == null ? null : Scheme.parse(schemeStr);
    Authority authority = authorityStr == null ? null : Authority.parse(authorityStr);
    Path path = Path.parse(pathStr);
    Query query = queryStr == null ? null : Query.parse(queryStr);
    Fragment fragment = fragmentStr == null ? null : Fragment.parse(fragmentStr);
    return new UriExpectation(scheme, authority, path, query, fragment);
  }
}
