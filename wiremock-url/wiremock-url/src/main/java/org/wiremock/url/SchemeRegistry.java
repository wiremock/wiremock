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
package org.wiremock.url;

import org.jspecify.annotations.Nullable;
import org.wiremock.stringparser.StringParser;

public interface SchemeRegistry extends StringParser<Scheme> {

  SchemeRegistry INSTANCE = new DefaultSchemeRegistry();

  Scheme http = INSTANCE.register("http", Port.of(80));
  Scheme https = INSTANCE.register("https", Port.of(443));
  Scheme ws = INSTANCE.register("ws", Port.of(80));
  Scheme wss = INSTANCE.register("wss", Port.of(443));
  Scheme file = INSTANCE.register("file");
  Scheme ftp = INSTANCE.register("ftp", Port.of(21));
  Scheme sftp = INSTANCE.register("sftp", Port.of(22));
  Scheme ssh = INSTANCE.register("ssh", Port.of(22));
  Scheme mailto = INSTANCE.register("mailto");

  @Override
  default Class<Scheme> getType() {
    return Scheme.class;
  }

  default Scheme register(String schemeString) throws IllegalScheme {
    return register(schemeString, null);
  }

  Scheme register(String schemeString, @Nullable Port defaultPort) throws IllegalScheme;
}
