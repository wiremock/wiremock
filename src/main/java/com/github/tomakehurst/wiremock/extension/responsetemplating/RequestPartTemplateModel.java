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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.tomakehurst.wiremock.common.ListOrSingle;
import com.github.tomakehurst.wiremock.http.Body;
import java.util.Map;
import java.util.TreeMap;

public class RequestPartTemplateModel {

  private final String name;
  private final Map<String, ListOrSingle<String>> headers;
  private final Body body;

  public RequestPartTemplateModel(
      String name, Map<String, ListOrSingle<String>> headers, Body body) {
    this.name = name;
    this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    this.headers.putAll(headers);
    this.body = body;
  }

  public String getName() {
    return name;
  }

  public Map<String, ListOrSingle<String>> getHeaders() {
    return headers;
  }

  public String getBody() {
    return body.asString();
  }

  public String getBodyAsBase64() {
    return body.asBase64();
  }

  public boolean isBinary() {
    return body.isBinary();
  }
}
