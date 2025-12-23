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
 * <p>An implementation must be immutable (and hence threadsafe)
 *
 * <p>Implementations must enforce that 1 <= port <= 65,535
 *
 * <p>Implementations should be equal to any other Port implementation with the same text
 * representation. Implementations are <b>NOT</b> equal if they have different string
 * representations, even if they represent the same port number. For example, {@code
 * Port.parse("00080").equals(Port.of(80)) == false} because their string representations differ
 * ("00080" vs "80"), even though both represent port 80. This preserves the original format as it
 * appeared in the URL.
 *
 * <p>An implementation's toString should return the String used when it was created. For ports
 * created via {@code parse()}, this preserves the original format including any leading zeros. For
 * ports created via {@code of()}, this returns the canonical form without leading zeros.
 *
 * <p>An implementation's hashCode() is based on the string representation, ensuring consistency
 * with equals.
 */
public interface Port {

  int port();

  /**
   * Returns a Port with the canonical (normalized) string representation of this port number,
   * without any leading zeros.
   *
   * <p>For example, {@code Port.parse("00080").normalise()} returns a Port whose {@code toString()}
   * is "80".
   *
   * <p>If this Port is already in canonical form, returns a Port equal to this one.
   *
   * @return a Port with the canonical string representation
   */
  Port normalise();

  static Port parse(String port) throws IllegalPort {
    return PortParser.INSTANCE.parse(port);
  }

  static Port of(int port) throws IllegalPort {
    return PortParser.INSTANCE.of(port);
  }
}
