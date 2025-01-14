/*
 * Copyright (C) 2017-2024 Thomas Akehurst
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;

public class JsonPathPatternJsonSerializer
    extends PathPatternJsonSerializer<MatchesJsonPathPattern> {

  @Override
  public JsonSerializer<MatchesJsonPathPattern> unwrappingSerializer(NameTransformer unwrapper) {
    return new UnwrappedJsonPathPatternJsonSerializer();
  }

  @Override
  protected void serializeAdditionalFields(
      MatchesJsonPathPattern value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {}
}
