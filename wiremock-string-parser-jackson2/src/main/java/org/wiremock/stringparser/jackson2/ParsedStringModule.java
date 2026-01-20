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
package org.wiremock.stringparser.jackson2;

import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;
import org.wiremock.stringparser.ParsedString;
import org.wiremock.stringparser.StringParser;

public class ParsedStringModule extends SimpleModule {

  private final List<StringParser<?>> stringParsers;

  public ParsedStringModule(List<StringParser<?>> stringParsers) {
    this.stringParsers = stringParsers;
  }

  public List<StringParser<?>> getStringParsers() {
    return stringParsers;
  }

  @Override
  public void setupModule(SetupContext context) {

    // Global serializer for all ParsedString types
    context.addSerializers(
        new SimpleSerializers(List.of(new ToStringSerializer(ParsedString.class))));

    // Per-type deserializers
    SimpleDeserializers deserializers = new SimpleDeserializers();
    for (var deserializer : stringParsers) {
      addDeserializer(deserializers, new ParsedStringDeserializer<>(deserializer));
    }

    context.addDeserializers(deserializers);
  }

  private <T extends ParsedString> void addDeserializer(
      SimpleDeserializers deserializers, ParsedStringDeserializer<T> deserializer) {
    deserializers.addDeserializer(deserializer.getStringParser().getType(), deserializer);
  }
}
