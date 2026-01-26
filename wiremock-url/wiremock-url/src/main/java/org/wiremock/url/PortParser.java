/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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
import org.wiremock.stringparser.StringParser;

public final class PortParser implements StringParser<Port> {

  public static final PortParser INSTANCE = new PortParser();

  static final int MAX_PORT = Integer.MAX_VALUE;

  private final Map<Integer, PortValue> portsByInt = new ConcurrentHashMap<>();

  @Override
  public Class<Port> getType() {
    return Port.class;
  }

  PortValue of(int port) throws IllegalPort {
    return portsByInt.computeIfAbsent(
        port,
        (p) -> {
          validate(p);
          return new PortValue(p, String.valueOf(p), true);
        });
  }

  @Override
  public Port parse(String stringForm) {
    try {
      if (stringForm.startsWith("+")) {
        throw new IllegalPort(stringForm);
      }
      int port = Integer.parseInt(stringForm);
      String canonical = String.valueOf(port);
      boolean isNormalForm = stringForm.equals(canonical);
      if (isNormalForm) {
        return of(port);
      } else {
        validate(port);
        return new PortValue(port, stringForm, false);
      }

    } catch (NumberFormatException e) {
      throw new IllegalPort(stringForm);
    }
  }

  private static void validate(int port) {
    if (port < 0) {
      throw new IllegalPort(port);
    }
  }
}
