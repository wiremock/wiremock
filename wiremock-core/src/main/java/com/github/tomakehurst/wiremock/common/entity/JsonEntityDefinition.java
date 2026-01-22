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
package com.github.tomakehurst.wiremock.common.entity;

import static com.github.tomakehurst.wiremock.common.entity.CompressionType.NONE;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;

public class JsonEntityDefinition extends TextEntityDefinition {

  private final JsonNode data;

  public JsonEntityDefinition(Object data) {
    super(TextFormat.JSON, UTF_8, NONE, null, null, null, null);
    this.data = data instanceof JsonNode ? (JsonNode) data : Json.node(data);
  }

  @Override
  public Object getData() {
    return data;
  }

  @JsonIgnore
  public JsonNode getDataAsJson() {
    return data;
  }

  @Override
  public String getDataAsString() {
    return Json.write(data);
  }

  @Override
  public byte[] getDataAsBytes() {
    return Json.toByteArray(data);
  }
}
