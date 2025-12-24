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

final class OriginParser implements CharSequenceParser<Origin> {

  static final OriginParser INSTANCE = new OriginParser();

  @Override
  public Origin parse(CharSequence url) throws IllegalOrigin {
    try {
      var urlReference = UriReferenceParser.INSTANCE.parse(url);
      if (urlReference instanceof Origin) {
        return (Origin) urlReference;
      } else {
        throw new IllegalOrigin(url.toString());
      }
    } catch (IllegalUriPart illegalUriPart) {
      throw new IllegalOrigin(url.toString(), illegalUriPart);
    }
  }

  Origin of(Scheme scheme, HostAndPort hostAndPort) {
    var normalisedScheme = scheme.normalise();
    var normalisedHostAndPort = hostAndPort.normalise(normalisedScheme);
    return new OriginValue(normalisedScheme, normalisedHostAndPort);
  }
}
