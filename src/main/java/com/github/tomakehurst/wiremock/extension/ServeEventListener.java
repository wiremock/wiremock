/*
 * Copyright (C) 2023 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

public interface ServeEventListener extends Extension {

  enum RequestPhase {
    BEFORE_MATCH,
    AFTER_MATCH,
    AFTER_COMPLETE
  }

  default void onEvent(RequestPhase requestPhase, ServeEvent serveEvent, Parameters parameters) {
    switch (requestPhase) {
      case BEFORE_MATCH:
        beforeMatch(serveEvent, parameters);
        break;
      case AFTER_MATCH:
        afterMatch(serveEvent, parameters);
        break;
      case AFTER_COMPLETE:
        afterComplete(serveEvent, parameters);
        break;
    }
  }

  default void beforeMatch(ServeEvent serveEvent, Parameters parameters) {}

  default void afterMatch(ServeEvent serveEvent, Parameters parameters) {}

  default void afterComplete(ServeEvent serveEvent, Parameters parameters) {}

  default boolean applyGlobally() {
    return true;
  }
}
