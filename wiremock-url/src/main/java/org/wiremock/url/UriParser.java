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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

final class UriParser implements StringParser<Uri> {

  static final UriParser INSTANCE = new UriParser();

  static boolean equals(Uri one, Object o) {
    if (one == o) {
      return true;
    }

    if (!(o instanceof Uri other)) {
      return false;
    }

    Class<? extends Uri> oneClass = one.getClass();
    Class<? extends Uri> otherClass = other.getClass();
    return shareSameSuperTypes(
            oneClass,
            otherClass,
            Origin.class,
            ServersideAbsoluteUrl.class,
            AbsoluteUrl.class,
            OpaqueUri.class,
            RelativeUrl.class,
            PathAndQuery.class)
        && Objects.equals(one.getScheme(), other.getScheme())
        && Objects.equals(one.getAuthority(), other.getAuthority())
        && Objects.equals(one.getPath(), other.getPath())
        && Objects.equals(one.getQuery(), other.getQuery())
        && Objects.equals(one.getFragment(), other.getFragment());
  }

  @SuppressWarnings("SameParameterValue")
  private static boolean shareSameSuperTypes(
      Class<?> oneClass, Class<?> otherClass, Class<?>... types) {
    for (Class<?> type : types) {
      if (oneClass.isAssignableFrom(type) != otherClass.isAssignableFrom(type)) {
        return false;
      }
    }
    return true;
  }

  static int hashCode(Uri uri) {
    return Objects.hash(
        uri.getScheme(), uri.getAuthority(), uri.getPath(), uri.getQuery(), uri.getFragment());
  }

  static String toString(Uri uri) {
    StringBuilder result = new StringBuilder();
    if (uri.getScheme() != null) {
      result.append(uri.getScheme()).append(":");
    }
    if (uri.getAuthority() != null) {
      result.append("//").append(uri.getAuthority());
    }
    result.append(uri.getPath());
    if (uri.getQuery() != null) {
      result.append("?").append(uri.getQuery());
    }
    if (uri.getFragment() != null) {
      result.append("#").append(uri.getFragment());
    }
    return result.toString();
  }

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
