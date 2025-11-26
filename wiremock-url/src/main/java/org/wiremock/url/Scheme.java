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

import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
public interface Scheme {

  Scheme http = register("http", Port.of(80));
  Scheme https = register("https", Port.of(443));
  Scheme file = register("file");
  Scheme ftp = register("ftp", Port.of(21));
  Scheme ssh = register("ssh", Port.of(22));
  Scheme mailto = register("mailto");

  Scheme canonical();

  @Nullable Port defaultPort();

  default boolean isCanonical() {
    return canonical() == this;
  }

  static Scheme of(CharSequence scheme) throws IllegalScheme {
    return SchemeParser.INSTANCE.parse(scheme.toString());
  }

  static Scheme register(String schemeString) throws IllegalScheme {
    return register(schemeString, null);
  }

  static Scheme register(String schemeString, @Nullable Port defaultPort) throws IllegalScheme {
    return SchemeParser.INSTANCE.register(schemeString, defaultPort);
  }
}
