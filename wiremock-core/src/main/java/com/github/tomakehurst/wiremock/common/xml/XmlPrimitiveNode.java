/*
 * Copyright (C) 2024-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common.xml;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Map;

public class XmlPrimitiveNode<T> extends XmlNode {

  private final T value;

  public XmlPrimitiveNode(T value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value instanceof Number ? NumberFormat.getInstance().format(value) : value.toString();
  }

  @Override
  public Map<String, String> getAttributes() {
    return Collections.emptyMap();
  }
}
