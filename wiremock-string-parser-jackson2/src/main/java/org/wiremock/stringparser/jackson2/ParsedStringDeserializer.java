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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import org.jspecify.annotations.Nullable;
import org.wiremock.stringparser.ParseException;
import org.wiremock.stringparser.ParsedString;
import org.wiremock.stringparser.StringParser;

public class ParsedStringDeserializer<T extends ParsedString> extends JsonDeserializer<T> {

  private final StringParser<T> parser;

  public ParsedStringDeserializer(StringParser<T> parser) {
    this.parser = parser;
  }

  public StringParser<T> getStringParser() {
    return parser;
  }

  @Override
  public @Nullable T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String text = p.getValueAsString();
    if (text == null) {
      return null;
    }

    try {
      return parser.parse(text);
    } catch (ParseException e) {
      throw JsonMappingException.from(p, e.getMessage(), e);
    }
  }
}
