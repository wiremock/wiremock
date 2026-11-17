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
package com.github.tomakehurst.wiremock.extension;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

import com.github.tomakehurst.wiremock.extension.MessageServeEventListener.MessagePhase;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.util.List;
import java.util.Map;

public class MessageServeEventListenerUtils {

  public static void triggerListeners(
      Map<String, MessageServeEventListener> listeners,
      MessagePhase phase,
      MessageServeEvent messageServeEvent) {
    triggerListeners(
        listeners, phase, messageServeEvent, messageServeEvent.getServeEventListeners());
  }

  public static void triggerListeners(
      Map<String, MessageServeEventListener> listeners,
      MessagePhase phase,
      MessageServeEvent messageServeEvent,
      List<MessageServeEventListenerDefinition> listenerDefinitions) {

    listeners.values().stream()
        .filter(MessageServeEventListener::applyGlobally)
        .forEach(listener -> listener.onEvent(phase, messageServeEvent, Parameters.empty()));

    for (MessageServeEventListenerDefinition listenerDef : listenerDefinitions) {
      MessageServeEventListener listener = listeners.get(listenerDef.getName());
      if (listener == null) {
        notifier()
            .error(
                "No per-stub message listener was found named \"" + listenerDef.getName() + "\"");
      } else if (!listener.applyGlobally() && listenerDef.shouldFireFor(phase)) {
        listener.onEvent(phase, messageServeEvent, listenerDef.getParameters());
      }
    }
  }
}
