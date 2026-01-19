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
package com.github.tomakehurst.wiremock.matching;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.BeanSerializerFactory;

public abstract class PathPatternJsonSerializer<T extends PathPattern> extends ValueSerializer<T> {

  @Override
  public void serialize(T value, JsonGenerator gen, SerializationContext serializers) {
    gen.writeStartObject();
    this.serializePathPattern(value, gen, serializers);
    gen.writeEndObject();
  }

  protected void serializePathPattern(
      T value, JsonGenerator gen, SerializationContext serializers) {
    if (value.isSimple()) {
      gen.writeStringProperty(value.getName(), value.getExpected());
    } else {
      AdvancedPathPattern advancedPathPattern =
          new AdvancedPathPattern(value.getExpected(), value.getValuePattern());
      gen.writeName(value.getName());

      JavaType javaType = serializers.getConfig().constructType(advancedPathPattern.getClass());
      ValueSerializer<Object> serializer =
          BeanSerializerFactory.instance.createSerializer(serializers, javaType);
      serializer.serialize(advancedPathPattern, gen, serializers);
    }
    serializeAdditionalFields(value, gen, serializers);
  }

  protected abstract void serializeAdditionalFields(
      T value, JsonGenerator gen, SerializationContext serializers);
}
