/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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

/** The interface Serve event listener. */
public interface ServeEventListener extends Extension {

  /** The enum Request phase. */
  enum RequestPhase {
    /** Before match request phase. */
    BEFORE_MATCH,
    /** After match request phase. */
    AFTER_MATCH,
    /** Before response sent request phase. */
    BEFORE_RESPONSE_SENT,
    /** After complete request phase. */
    AFTER_COMPLETE
  }

  /**
   * On event.
   *
   * @param requestPhase the request phase
   * @param serveEvent the serve event
   * @param parameters the parameters
   */
  default void onEvent(RequestPhase requestPhase, ServeEvent serveEvent, Parameters parameters) {
    switch (requestPhase) {
      case BEFORE_MATCH:
        beforeMatch(serveEvent, parameters);
        break;
      case AFTER_MATCH:
        afterMatch(serveEvent, parameters);
        break;
      case BEFORE_RESPONSE_SENT:
        beforeResponseSent(serveEvent, parameters);
        break;
      case AFTER_COMPLETE:
        afterComplete(serveEvent, parameters);
        break;
    }
  }

  /**
   * Before match.
   *
   * @param serveEvent the serve event
   * @param parameters the parameters
   */
  default void beforeMatch(ServeEvent serveEvent, Parameters parameters) {}

  /**
   * After match.
   *
   * @param serveEvent the serve event
   * @param parameters the parameters
   */
  default void afterMatch(ServeEvent serveEvent, Parameters parameters) {}

  /**
   * Before response sent.
   *
   * @param serveEvent the serve event
   * @param parameters the parameters
   */
  default void beforeResponseSent(ServeEvent serveEvent, Parameters parameters) {}

  /**
   * After complete.
   *
   * @param serveEvent the serve event
   * @param parameters the parameters
   */
  default void afterComplete(ServeEvent serveEvent, Parameters parameters) {}

  /**
   * Apply globally boolean.
   *
   * @return the boolean
   */
  default boolean applyGlobally() {
    return true;
  }
}
