/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension;

import java.util.List;

public interface ExtensionFactory {

  /**
   * Allows the factory to check the runtime environment and prevent itself being used if not
   * compatible e.g. because the wrong Jetty version is present.
   *
   * @return true if the factory can be loaded.
   */
  default boolean isLoadable() {
    return true;
  }

  List<Extension> create(WireMockServices services);
}
