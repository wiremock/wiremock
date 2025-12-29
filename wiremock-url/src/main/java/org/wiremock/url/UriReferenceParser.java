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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

final class UriReferenceParser implements CharSequenceParser<UriReference> {

  static final UriReferenceParser INSTANCE = new UriReferenceParser();

  static boolean equals(UriReference one, Object o) {
    if (one == o) {
      return true;
    }

    if (!(o instanceof UriReference other)) {
      return false;
    }

    return Objects.equals(one.getScheme(), other.getScheme())
        && Objects.equals(one.getAuthority(), other.getAuthority())
        && Objects.equals(one.getPath(), other.getPath())
        && Objects.equals(one.getQuery(), other.getQuery())
        && Objects.equals(one.getFragment(), other.getFragment());
  }

  static int hashCode(UriReference urlReference) {
    return Objects.hash(
        urlReference.getScheme(),
        urlReference.getAuthority(),
        urlReference.getPath(),
        urlReference.getQuery(),
        urlReference.getFragment());
  }

  static String toString(UriReference urlReference) {
    StringBuilder result = new StringBuilder();
    if (urlReference.getScheme() != null) {
      result.append(urlReference.getScheme()).append(":");
    }
    if (urlReference.getAuthority() != null) {
      result.append("//").append(urlReference.getAuthority());
    }
    result.append(urlReference.getPath());
    if (urlReference.getQuery() != null) {
      result.append("?").append(urlReference.getQuery());
    }
    if (urlReference.getFragment() != null) {
      result.append("#").append(urlReference.getFragment());
    }
    return result.toString();
  }

  private final Pattern regex =
      Pattern.compile(
          "^(?:(?<scheme>[^:/?#]+):)?(?://(?<authority>[^/?#]*))?(?<path>[^?#]*)(?:\\?(?<query>[^#]*))?(?:#(?<fragment>.*))?");

  @Override
  public UriReference parse(CharSequence stringForm) {
    String string = stringForm.toString();
    try {
      var result = regex.matcher(stringForm);
      if (!result.matches()) {
        if (string.contains(":")) {
          throw new IllegalUrl(string);
        } else {
          throw new IllegalRelativeRef(string);
        }
      }

      var schemeString = result.group("scheme");
      var scheme = schemeString == null ? null : Scheme.parse(schemeString);

      var queryString = result.group("query");
      var query = queryString == null ? null : Query.parse(queryString);

      var fragmentString = result.group("fragment");
      var fragment = fragmentString == null ? null : Fragment.parse(fragmentString);

      var hierarchicalPart = extractHierarchicalPart(result);
      return UriReference.builder()
          .setScheme(scheme)
          .setAuthority(hierarchicalPart.authority)
          .setPath(hierarchicalPart.path)
          .setQuery(query)
          .setFragment(fragment)
          .build();
    } catch (IllegalUriPart illegalPart) {
      throw new IllegalUriReference(string, illegalPart);
    }
  }

  private HierarchicalPart extractHierarchicalPart(Matcher matcher) {
    var authority = extractAuthorityOrNull(matcher);
    var pathStr = matcher.group("path");
    var path = PathParser.INSTANCE.parse(pathStr);
    return new HierarchicalPart(authority, path);
  }

  @Nullable
  private Authority extractAuthorityOrNull(Matcher matcher) {
    String authorityStr = matcher.group("authority");
    if (authorityStr == null) {
      return null;
    } else {
      return AuthorityParser.INSTANCE.parse(authorityStr);
    }
  }

  record HierarchicalPart(@Nullable Authority authority, Path path) {}
}
