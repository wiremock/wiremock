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

import org.jspecify.annotations.Nullable;

/**
 * Represents an absolute url as defined in <a
 * href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.3">RFC 3986 Section 4.3</a>.
 *
 * <p>An absolute URL is a URL with no fragment, suitable for making requests for a representation
 * over the network.
 *
 * <p>Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.3">RFC 3986 Section 4.3</a>
 */
public interface AbsoluteUrl extends Url {

  /**
   * {@implSpec} Implementations must ALWAYS return null
   *
   * @deprecated This always returns null so you have no reason to ever call it
   * @return null
   */
  @Override
  @Deprecated // no point ever calling on this subtype
  @Nullable
  default Fragment getFragment() {
    return null;
  }

  /**
   * Returns a normalised form of this absolute URL.
   *
   * @return a normalised absolute URL
   */
  @Override
  AbsoluteUrl normalise();

  /**
   * Parses a string into an absolute URL.
   *
   * @param absoluteUrl the string to parse
   * @return the parsed absolute URL
   * @throws IllegalAbsoluteUrl if the string is not a valid absolute URL
   */
  static AbsoluteUrl parse(String absoluteUrl) throws IllegalAbsoluteUrl {
    return AbsoluteUrlParser.INSTANCE.parse(absoluteUrl);
  }
}
