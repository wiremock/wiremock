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

import static org.wiremock.url.Constants.alwaysIllegal;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;

final class UrlReferenceParser implements CharSequenceParser<UrlReference> {

  static final UrlReferenceParser INSTANCE = new UrlReferenceParser();

  static boolean equals(UrlReference one, Object o) {
    if (one == o) {
      return true;
    }

    if (!(o instanceof UrlReference other)) {
      return false;
    }

    return Objects.equals(one.scheme(), other.scheme())
        && Objects.equals(one.authority(), other.authority())
        && Objects.equals(one.path(), other.path())
        && Objects.equals(one.query(), other.query())
        && Objects.equals(one.fragment(), other.fragment());
  }

  static int hashCode(UrlReference urlReference) {
    return Objects.hash(
        urlReference.scheme(),
        urlReference.authority(),
        urlReference.path(),
        urlReference.query(),
        urlReference.fragment());
  }

  static String toString(UrlReference urlReference) {
    StringBuilder result = new StringBuilder();
    if (urlReference.scheme() != null) {
      result.append(urlReference.scheme()).append(":");
    }
    if (urlReference.authority() != null) {
      result.append("//").append(urlReference.authority());
    }
    result.append(urlReference.path());
    if (urlReference.query() != null) {
      result.append("?").append(urlReference.query());
    }
    if (urlReference.fragment() != null) {
      result.append("#").append(urlReference.fragment());
    }
    return result.toString();
  }

  @Language("RegExp")
  private final String scheme = "(?<scheme>" + SchemeParser.INSTANCE.schemeRegex + ")";

  @Language("RegExp")
  private final String authority =
      "(?>(?<authority>" + AuthorityParser.INSTANCE.authorityRegex + ")?)";

  @Language("RegExp")
  private final String path = "(?<path>(|/" + PathParser.INSTANCE.pathRegex + "))";

  @Language("RegExp")
  private final String query = "(?<query>" + QueryParser.INSTANCE.queryRegex + ")";

  private final String fragmentRegex = "[^" + alwaysIllegal + "]*";

  @Language("RegExp")
  private final String fragment = "(?<fragment>" + fragmentRegex + ")";

  private final Pattern regex =
      Pattern.compile(
          "^(" + scheme + ":)?(//" + authority + ")" + path + "(\\?" + query + ")?(#" + fragment
              + ")?$");

  @Override
  public UrlReference parse(CharSequence stringForm) {
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
      if (scheme == null) {
        if (hierarchicalPart.authority == null
            && fragment == null
            && (hierarchicalPart.path.isAbsolute())) {
          return new PathAndQueryParser.PathAndQuery(hierarchicalPart.path, query);
        } else {
          return new RelativeRefParser.RelativeRef(
              hierarchicalPart.authority, hierarchicalPart.path, query, fragment);
        }

      } else {
        if (hierarchicalPart.authority != null) {
          if (hierarchicalPart.path.equals(Path.ROOT) && query == null && fragment == null) {
            return new BaseUrlParser.BaseUrl(scheme, hierarchicalPart.authority);
          } else {
            return Url.builder(scheme, hierarchicalPart.authority)
                .setPath(hierarchicalPart.path)
                .setQuery(query)
                .setFragment(fragment)
                .build();
          }
        } else {
          // not handling URIs yet
          throw new IllegalUrl(string);
        }
      }
    } catch (IllegalUrlPart illegalPart) {
      throw new IllegalUrl(string, illegalPart);
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
      return AuthorityParser.INSTANCE.parse(matcher, authorityStr);
    }
  }

  record HierarchicalPart(@Nullable Authority authority, Path path) {}
}
