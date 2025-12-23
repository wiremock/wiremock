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

class HostAndPortParser implements CharSequenceParser<HostAndPort> {

  static final HostAndPortParser INSTANCE = new HostAndPortParser();

  @Override
  public HostAndPort parse(CharSequence stringForm) throws ParseException {
    String hostAndPortStr = stringForm.toString();
    var authority = Authority.parse(hostAndPortStr);
    if (authority instanceof HostAndPort) {
      return (HostAndPort) authority;
    } else {
      throw new IllegalHostAndPort(hostAndPortStr);
    }
  }
}
