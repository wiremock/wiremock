/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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
import com.github.tomakehurst.wiremock.common.ClientError;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.JsonException;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import java.util.Set;

public class MatchesJsonSchemaPattern extends StringValuePattern {

  private final JsonSchema schema;
  private final WireMock.JsonSchemaVersion schemaVersion;
  private final int schemaPropertyCount;
  private final Errors invalidSchemaErrors;

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
    JsonSchema schema;
    JsonNode schemaAsJson = Json.read(schemaJson, JsonNode.class);
    int schemaPropertyCount;
    Errors invalidSchemaErrors;
    try {
      schema = schemaFactory.getSchema(schemaAsJson, config);
      schemaPropertyCount = Json.schemaPropertyCount(schemaAsJson);
      invalidSchemaErrors = null;
    } catch (Exception e) {
      schema = null;
      schemaPropertyCount = 0;
      invalidSchemaErrors = getInvalidSchemaErrors(e);
    }
    this.schema = schema;
    this.schemaVersion = schemaVersion;

    this.schemaPropertyCount = schemaPropertyCount;
    this.invalidSchemaErrors = invalidSchemaErrors;
  }

  public MatchesJsonSchemaPattern(
      JsonNode schemaJsonNode, WireMock.JsonSchemaVersion schemaVersion) {
    this(Json.write(schemaJsonNode), schemaVersion);
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
    if (schema == null) {
      return MatchResult.noMatch(new SubEvent(SubEvent.ERROR, invalidSchemaErrors));
    }
    if (json == null) {
      return MatchResult.noMatch();
    }

    JsonNode jsonNode;
    try {
      jsonNode = Json.read(json, JsonNode.class);
    } catch (JsonException je) {
      jsonNode = new TextNode(json);
    }

    final Set<ValidationMessage> validationMessages;
    try {
      validationMessages = validate(jsonNode, json);
    } catch (Exception e) {
      return MatchResult.noMatch(new SubEvent(SubEvent.ERROR, getInvalidSchemaErrors(e)));
    }

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

  private static Errors getInvalidSchemaErrors(Exception e) {
    Errors invalidSchemaErrors;
    if (e instanceof ClientError) {
      Errors.Error error = ((ClientError) e).getErrors().first();
      invalidSchemaErrors =
          Errors.single(
              error.getCode(),
              error.getSource().getPointer(),
              "Invalid JSON Schema",
              error.getDetail());
    } else {
      invalidSchemaErrors =
          Errors.singleWithDetail(10, "Invalid JSON Schema", getRootCause(e).getMessage());
    }
    return invalidSchemaErrors;
  }

  private static Throwable getRootCause(Throwable e) {
    if (e.getCause() != null) {
      return getRootCause(e.getCause());
    }
    return e;
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
