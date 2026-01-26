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
package org.wiremock.url.jackson2;

import java.util.List;
import org.wiremock.stringparser.jackson2.ParsedStringModule;
import org.wiremock.url.AbsoluteUriParser;
import org.wiremock.url.AbsoluteUrlParser;
import org.wiremock.url.AuthorityParser;
import org.wiremock.url.BaseUrlParser;
import org.wiremock.url.FragmentParser;
import org.wiremock.url.HostAndPortParser;
import org.wiremock.url.HostParser;
import org.wiremock.url.OpaqueUriParser;
import org.wiremock.url.OriginParser;
import org.wiremock.url.PasswordParser;
import org.wiremock.url.PathAndQueryParser;
import org.wiremock.url.PathParser;
import org.wiremock.url.PortParser;
import org.wiremock.url.QueryParamKeyParser;
import org.wiremock.url.QueryParamValueParser;
import org.wiremock.url.QueryParser;
import org.wiremock.url.RelativeUrlParser;
import org.wiremock.url.SchemeRegistry;
import org.wiremock.url.SchemeRelativeUrlParser;
import org.wiremock.url.SegmentParser;
import org.wiremock.url.ServersideAbsoluteUrlParser;
import org.wiremock.url.UriParser;
import org.wiremock.url.UrlParser;
import org.wiremock.url.UrlWithAuthorityParser;
import org.wiremock.url.UserInfoParser;
import org.wiremock.url.UsernameParser;

public class WireMockUrlModule extends ParsedStringModule {

  public WireMockUrlModule() {
    this(UriParser.INSTANCE);
  }

  public WireMockUrlModule(SchemeRegistry schemeRegistry) {
    this(new UriParser(schemeRegistry));
  }

  public WireMockUrlModule(UriParser uriParser) {
    super(
        List.of(
            new AbsoluteUriParser(uriParser),
            new AbsoluteUrlParser(uriParser),
            new OpaqueUriParser(uriParser),
            new OriginParser(uriParser),
            new RelativeUrlParser(uriParser),
            new SchemeRelativeUrlParser(uriParser),
            new ServersideAbsoluteUrlParser(uriParser),
            new UrlParser(uriParser),
            new UrlWithAuthorityParser(uriParser),
            new BaseUrlParser(uriParser),
            PathAndQueryParser.INSTANCE,
            AuthorityParser.INSTANCE,
            FragmentParser.INSTANCE,
            HostParser.INSTANCE,
            HostAndPortParser.INSTANCE,
            PasswordParser.INSTANCE,
            PathParser.INSTANCE,
            PortParser.INSTANCE,
            QueryParser.INSTANCE,
            QueryParamKeyParser.INSTANCE,
            QueryParamValueParser.INSTANCE,
            SchemeRegistry.INSTANCE,
            SegmentParser.INSTANCE,
            UriParser.INSTANCE,
            UserInfoParser.INSTANCE,
            UsernameParser.INSTANCE));
  }
}
