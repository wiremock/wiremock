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
package com.github.tomakehurst.wiremock.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.entity.BinaryEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.StringEntityDefinition;
import java.util.Objects;

public class MessageDefinition {

  private final EntityDefinition body;

  @JsonCreator
  public MessageDefinition(@JsonProperty("body") EntityDefinition body) {
    this.body = body;
  }

  public static MessageDefinition fromString(String message) {
    return new MessageDefinition(new StringEntityDefinition(message));
  }

  public static MessageDefinition fromBytes(byte[] data) {
    return new MessageDefinition(BinaryEntityDefinition.aBinaryMessage().withBody(data).build());
  }

  public EntityDefinition getBody() {
    return body;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessageDefinition that = (MessageDefinition) o;
    return Objects.equals(body, that.body);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(body);
  }
}
