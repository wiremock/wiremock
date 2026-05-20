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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.nio.charset.Charset;

public class SimpleEntityDefinitionSerializer extends StdSerializer<SimpleEntityDefinition> {

  public SimpleEntityDefinitionSerializer() {
    super(SimpleEntityDefinition.class);
  }

  @Override
  public void serialize(
      SimpleEntityDefinition value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    if (value.isSimpleStringStyle()) {
      gen.writeString(value.getDataAsString());
      return;
    }

    gen.writeStartObject();

    Format format = value.getFormatForSerialization();
    if (format != null) {
      gen.writeStringField("format", format.value());
    }

    CompressionType compression = value.getCompression();
    if (!EntityDefinition.DEFAULT_COMPRESSION.equals(compression)) {
      gen.writeStringField("compression", compression.value());
    }

    Charset charset = value.getCharset();
    if (charset != null && !EntityDefinition.DEFAULT_CHARSET.equals(charset)) {
      gen.writeStringField("charset", charset.name());
    }

    Object data = value.getData();
    if (data != null) {
      gen.writeStringField("data", (String) data);
    }

    String base64Data = value.getBase64Data();
    if (base64Data != null) {
      gen.writeStringField("base64Data", base64Data);
    }

    gen.writeEndObject();
  }
}
