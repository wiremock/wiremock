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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class PortParser implements CharSequenceParser<Port> {

  static PortParser INSTANCE = new PortParser();

  static final int MAX_PORT = 65_535;

  private final Map<Integer, Port> portsByInt = new ConcurrentHashMap<>();
  private final Map<String, Port> portsByString = new ConcurrentHashMap<>();

  Port of(int port) throws IllegalPort {
    return portsByInt.computeIfAbsent(
        port,
        (key) -> {
          if (port < 1 || port > MAX_PORT) {
            throw new IllegalPort(port);
          }
          return new Port(port, String.valueOf(port));
        });
  }

  @Override
  public org.wiremock.url.Port parse(CharSequence stringForm) {
    String string = stringForm.toString();
    return portsByString.computeIfAbsent(
        string,
        (key) -> {
          try {
            if (string.startsWith("+")) {
              throw new IllegalPort(string);
            }
            int port = Integer.parseInt(string);
            if (port < 1 || port > MAX_PORT) {
              throw new IllegalPort(port);
            }
            return new Port(port, string);
          } catch (NumberFormatException e) {
            throw new IllegalPort(string);
          }
        });
  }

  record Port(@Override int port, String portString) implements org.wiremock.url.Port {

    @Override
    public String toString() {
      return portString;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof org.wiremock.url.Port other)) {
        return false;
      }
      return port == other.port();
    }

    @Override
    public int hashCode() {
      return port;
    }
  }
}
