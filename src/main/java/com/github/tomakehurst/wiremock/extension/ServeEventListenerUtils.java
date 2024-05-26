/*
 * Copyright (C) 2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.List;
import java.util.Map;

public class ServeEventListenerUtils {
  public static void triggerListeners(
      Map<String, ServeEventListener> serveEventListeners,
      ServeEventListener.RequestPhase requestPhase,
      ServeEvent serveEvent) {

    serveEventListeners.values().stream()
        .filter(ServeEventListener::applyGlobally)
        .forEach(listener -> listener.onEvent(requestPhase, serveEvent, Parameters.empty()));

    List<ServeEventListenerDefinition> serveEventListenerDefinitions =
        serveEvent.getServeEventListeners();
    for (ServeEventListenerDefinition listenerDef : serveEventListenerDefinitions) {
      ServeEventListener listener = serveEventListeners.get(listenerDef.getName());
      if (listener != null
          && !listener.applyGlobally()
          && listenerDef.shouldFireFor(requestPhase)) {
        Parameters parameters = listenerDef.getParameters();
        listener.onEvent(requestPhase, serveEvent, parameters);
      } else {
        notifier().error("No per-stub listener was found named \"" + listenerDef.getName() + "\"");
      }
    }
  }
}
