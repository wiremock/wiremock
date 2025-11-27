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

import static org.wiremock.url.PortParser.MAX_PORT;

public final class IllegalPort extends IllegalUrlPart {

  public IllegalPort(String illegalPortString) {
    super(
        illegalPortString,
        "Illegal port ["
            + illegalPortString
            + "]; Port value must be an integer between 1 and "
            + MAX_PORT);
  }

  public IllegalPort(int illegalPort) {
    this(String.valueOf(illegalPort));
  }
}
