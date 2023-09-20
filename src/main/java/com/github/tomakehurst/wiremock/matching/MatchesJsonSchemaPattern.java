/*
 * Copyright (C) 2023 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.JsonException;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import java.util.Set;

public class MatchesJsonSchemaPattern extends StringValuePattern {

  private final JsonSchema schema;
  private final WireMock.JsonSchemaVersion schemaVersion;
  private final int schemaPropertyCount;

  public MatchesJsonSchemaPattern(String schemaJson) {
    this(schemaJson, WireMock.JsonSchemaVersion.V202012);
  }

  public MatchesJsonSchemaPattern(
      @JsonProperty("matchesJsonSchema") String schemaJson,
      @JsonProperty("schemaVersion") WireMock.JsonSchemaVersion schemaVersion) {
    super(schemaJson);

    SchemaValidatorsConfig config = new SchemaValidatorsConfig();
    config.setTypeLoose(false);
    config.setHandleNullableField(true);

    final JsonSchemaFactory schemaFactory =
        JsonSchemaFactory.getInstance(schemaVersion.toVersionFlag());
    schema = schemaFactory.getSchema(schemaJson, config);
    this.schemaVersion = schemaVersion;

    schemaPropertyCount = Json.schemaPropertyCount(Json.read(schemaJson, JsonNode.class));
  }

  public String getMatchesJsonSchema() {
    return expectedValue;
  }

  public WireMock.JsonSchemaVersion getSchemaVersion() {
    return schemaVersion;
  }

  @Override
  public String getExpected() {
    return Json.prettyPrint(getValue());
  }

  @Override
  public MatchResult match(String json) {
    if (json == null) {
      return MatchResult.noMatch();
    }

    JsonNode jsonNode;
    try {
      jsonNode = Json.read(json, JsonNode.class);
    } catch (JsonException je) {
      jsonNode = new TextNode(json);
    }

    final Set<ValidationMessage> validationMessages = validate(jsonNode, json);
    if (validationMessages.isEmpty()) {
      return MatchResult.exactMatch();
    }

    return new MatchResult() {
      @Override
      public boolean isExactMatch() {
        return false;
      }

      @Override
      public double getDistance() {
        if (schemaPropertyCount == 0) {
          return 1;
        }

        return (double) validationMessages.size() / schemaPropertyCount;
      }
    };
  }

  private Set<ValidationMessage> validate(JsonNode jsonNode, String originalJson) {
    final Set<ValidationMessage> validationMessages = schema.validate(jsonNode);
    if (validationMessages.isEmpty() || jsonNode.isTextual() || jsonNode.isContainerNode()) {
      return validationMessages;
    } else {
      return schema.validate(new TextNode(originalJson));
    }
  }
}
