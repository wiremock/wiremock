/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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
import org.wiremock.stringparser.StringParser;

public final class UriParser implements StringParser<Uri> {

  public static final UriParser INSTANCE = new UriParser();

  private static final Pattern regex =
      Pattern.compile(
          "^(?:(?<scheme>[^:/?#]+):)?(?://(?<authority>[^/?#]*))?(?<path>[^?#]*)(?:\\?(?<query>[^#]*))?(?:#(?<fragment>.*))?");

  private final SchemeRegistry schemeRegistry;

  public UriParser() {
    this(SchemeRegistry.INSTANCE);
  }

  public UriParser(SchemeRegistry schemeRegistry) {
    this.schemeRegistry = schemeRegistry;
  }

  @Override
  public Class<Uri> getType() {
    return Uri.class;
  }

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
      var scheme = schemeString == null ? null : schemeRegistry.parse(schemeString);

      var queryString = result.group("query");
      var query = queryString == null ? null : Query.parse(queryString);

      var fragmentString = result.group("fragment");
      var fragment = fragmentString == null ? null : Fragment.parse(fragmentString);

      var authority = extractAuthorityOrNull(result);
      var path = PathParser.INSTANCE.parse(result.group("path"));

      UriBuilder uriBuilder = new UriBuilder();
      uriBuilder
          .setScheme(scheme)
          .setAuthority(authority)
          .setPath(path)
          .setQuery(query)
          .setFragment(fragment);
      return uriBuilder.build(stringForm);
    } catch (IllegalUriPart illegalPart) {
      throw new IllegalUri(stringForm, illegalPart);
    }
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
}
