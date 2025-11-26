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

/**
 * Represents a valid network port.
 *
 * <p>Implementations must enforce that 1 <= port <= 65,535
 *
 * <p>Implementations should be equal to any other port implementation with the same port number
 *
 * <p>An implementation's toString should return <code>String.valueOf(port())</code>
 *
 * <p>An implementation's hashCode() should return <code>port()</code>
 */
public interface Port {

  int port();

  static Port parse(String port) throws IllegalPort {
    return PortParser.INSTANCE.parse(port);
  }

  static Port of(int port) throws IllegalPort {
    return PortParser.INSTANCE.of(port);
  }
}
