/*
 * Copyright (C) 2017-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.type.CollectionType;
import tools.jackson.databind.type.TypeFactory;

public class ListOrSingleSerialiser extends ValueSerializer<ListOrSingle<Object>> {

  @Override
  public void serialize(
      ListOrSingle<Object> value, JsonGenerator gen, SerializationContext serializers)
      throws JacksonException {
    if (value.isEmpty()) {
      gen.writeStartArray();
      gen.writeEndArray();
      return;
    }

    Object firstValue = value.getFirst();
    if (value.isSingle()) {
      ValueSerializer<Object> serializer = serializers.findValueSerializer(firstValue.getClass());
      serializer.serialize(firstValue, gen, serializers);
    } else {
      CollectionType type =
          TypeFactory.createDefaultInstance()
              .constructCollectionType(List.class, firstValue.getClass());
      ValueSerializer<Object> serializer = serializers.findValueSerializer(type);
      serializer.serialize(value, gen, serializers);
    }
  }
}
