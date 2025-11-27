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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;

final class UrlReferenceParser implements CharSequenceParser<UrlReference> {

  static final UrlReferenceParser INSTANCE = new UrlReferenceParser();

  @Language("RegExp")
  private final String scheme = "(?<scheme>" + SchemeParser.INSTANCE.schemeRegex + ")";

  @Language("RegExp")
  private final String authority = "(?<authority>" + AuthorityParser.INSTANCE.authorityRegex + ")";

  @Language("RegExp")
  private final String path = "(?<path>" + PathParser.INSTANCE.pathRegex + ")";

  @Language("RegExp")
  private final String query = "(?<query>" + QueryParser.INSTANCE.queryRegex + ")";

  private final String fragmentRegex = ".*";

  @Language("RegExp")
  private final String fragment = "(?<fragment>" + fragmentRegex + ")";

  private final Pattern regex =
      Pattern.compile(
          "^(" + scheme + ":)?(//" + authority + ")?" + path + "(\\?" + query + ")?(#" + fragment
              + ")?$");

  @Override
  public UrlReference parse(CharSequence stringForm) {
    String string = stringForm.toString();
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
    if (scheme != null) {
      if (hierarchicalPart.authority != null) {
        return Url.builder(scheme, hierarchicalPart.authority)
            .setPath(hierarchicalPart.path)
            .setQuery(query)
            .setFragment(fragment)
            .build();
      } else {
        throw new IllegalUrl(string);
      }
    } else {
      if (fragment == null && (hierarchicalPart.path.isAbsolute())) {
        return new PathAndQueryParser.PathAndQuery(hierarchicalPart.path, query);
      } else {
        return new RelativeRefParser.RelativeRef(
            hierarchicalPart.authority, hierarchicalPart.path, query, fragment);
      }
    }
  }

  private HierarchicalPart extractHierarchicalPart(Matcher matcher) {
    var authority = extractAuthorityOrNull(matcher);
    var pathStr = matcher.group("path");
    var path = new PathParser.Path(pathStr);
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
