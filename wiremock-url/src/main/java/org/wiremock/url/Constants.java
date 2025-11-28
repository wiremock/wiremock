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

import org.intellij.lang.annotations.Language;

class Constants {

  @Language("RegExp")
  static final String unreserved = "[a-zA-Z0-9\\-._~]";

  @Language("RegExp")
  static final String pctEncoded = "%[0-9A-F]{2}";

  @Language("RegExp")
  static final String subDelims = "[!$&'()*+,;=]";

  @Language("RegExp")
  static final String alwaysIllegal =
      "\\u0000-\\u0008\\u000A-\\u001F\\u007F\\u0080-\\u009F\\uD800-\\uDFFF";

  private Constants() {
    throw new UnsupportedOperationException("Not instantiable");
  }
}
