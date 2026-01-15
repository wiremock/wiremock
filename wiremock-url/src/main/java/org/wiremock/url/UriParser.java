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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

final class UriParser implements StringParser<Uri> {

  static final UriParser INSTANCE = new UriParser();

  private final Pattern regex =
      Pattern.compile(
          "^(?:(?<scheme>[^:/?#]+):)?(?://(?<authority>[^/?#]*))?(?<path>[^?#]*)(?:\\?(?<query>[^#]*))?(?:#(?<fragment>.*))?");

  @Override
  public Uri parse(String stringForm) {
    try {
      var result = regex.matcher(stringForm);
      if (!result.matches()) {
        if (stringForm.contains(":")) {
          throw new IllegalAbsoluteUrl(stringForm);
        } else {
          throw new IllegalRelativeUrl(stringForm);
        }
      }

      var schemeString = result.group("scheme");
      var scheme = schemeString == null ? null : Scheme.parse(schemeString);

      var queryString = result.group("query");
      var query = queryString == null ? null : Query.parse(queryString);

      var fragmentString = result.group("fragment");
      var fragment = fragmentString == null ? null : Fragment.parse(fragmentString);

      var hierarchicalPart = extractHierarchicalPart(scheme == null, result, stringForm);
      return Uri.builder()
          .setScheme(scheme)
          .setAuthority(hierarchicalPart.authority)
          .setPath(hierarchicalPart.path)
          .setQuery(query)
          .setFragment(fragment)
          .build();
    } catch (IllegalUriPart illegalPart) {
      throw new IllegalUri(stringForm, illegalPart);
    }
  }

  private HierarchicalPart extractHierarchicalPart(
      boolean isRelative, Matcher matcher, String stringForm) {
    var authority = extractAuthorityOrNull(matcher);
    var pathStr = matcher.group("path");
    var path = PathParser.INSTANCE.parse(pathStr);

    if (isRelative) {
      if (authority == null) {
        if (!path.isAbsolute()
            && !path.isEmpty()
            && path.getSegments().get(0).toString().contains(":")) {
          throw new IllegalRelativeUrl(
              stringForm,
              new IllegalPath(
                  path.toString(),
                  "path `" + path + "` may not contain a colon (`:`) in the first segment"));
        }
      } else {
        if (!path.isAbsolute() && !path.isEmpty()) {
          throw new IllegalRelativeUrl(
              stringForm,
              new IllegalPath(path.toString(), "path `" + path + "` must be absolute or empty"));
        }
      }
    }
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
