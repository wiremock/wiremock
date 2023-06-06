/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.util.List;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class JsonException extends InvalidInputException {

  protected JsonException(Errors errors) {
    super(errors);
  }

  public static JsonException fromJackson(JsonProcessingException processingException) {
    Throwable rootCause = getRootCause(processingException);

    String message = rootCause.getMessage();
    if (rootCause instanceof PatternSyntaxException) {
      PatternSyntaxException patternSyntaxException = (PatternSyntaxException) rootCause;
      message = patternSyntaxException.getMessage();
    } else if (rootCause instanceof JsonMappingException) {
      message = ((JsonMappingException) rootCause).getOriginalMessage();
    } else if (rootCause instanceof InvalidInputException) {
      message = ((InvalidInputException) rootCause).getErrors().first().getDetail();
    }

    String pointer = null;
    if (processingException instanceof JsonMappingException) {
      List<String> nodes =
          ((JsonMappingException) processingException)
              .getPath().stream().map(TO_NODE_NAMES).collect(Collectors.toList());
      pointer = "/" + String.join("/", nodes);
    }

    return new JsonException(Errors.single(10, pointer, "Error parsing JSON", message));
  }

  private static Throwable getRootCause(Throwable e) {
    if (e.getCause() != null) {
      return getRootCause(e.getCause());
    }

    return e;
  }

  private static final Function<JsonMappingException.Reference, String> TO_NODE_NAMES =
      input ->
          input.getFieldName() != null ? input.getFieldName() : String.valueOf(input.getIndex());
}
