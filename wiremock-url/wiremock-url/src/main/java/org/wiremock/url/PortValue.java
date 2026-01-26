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

final class PortValue implements Port {

  private final int port;
  private final String portString;
  private final boolean isNormalForm;

  PortValue(int port, String portString, boolean isNormalForm) {
    this.port = port;
    this.portString = portString;
    this.isNormalForm = isNormalForm;
  }

  @Override
  public String toString() {
    return portString;
  }

  @Override
  public Port normalise() {
    if (portString.equals(String.valueOf(port))) {
      return this;
    } else {
      return PortParser.INSTANCE.of(port);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Port other)) {
      return false;
    }
    return toString().equals(other.toString());
  }

  @Override
  public int hashCode() {
    return portString.hashCode();
  }

  @Override
  public int getIntValue() {
    return port;
  }

  @Override
  public boolean isNormalForm() {
    return isNormalForm;
  }
}
