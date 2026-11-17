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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class JsonException extends InvalidInputException {

  private static final Pattern MISSING_REQUIRED_PROPERTY =
      Pattern.compile("Missing required creator property '([^']+)'");

  protected JsonException(Errors errors) {
    super(errors);
  }

  public static JsonException fromJackson(JsonProcessingException processingException) {
    if (processingException instanceof MismatchedInputException mie) {
      Matcher matcher = MISSING_REQUIRED_PROPERTY.matcher(mie.getOriginalMessage());
      if (matcher.find()) {
        String fieldName = matcher.group(1);
        return new JsonException(Errors.validation(fieldName, fieldName + " is required"));
      }
    }

    Throwable rootCause = getRootCause(processingException);

    String message = rootCause.getMessage();
    if (rootCause instanceof PatternSyntaxException patternSyntaxException) {
      message = patternSyntaxException.getMessage();
    } else if (rootCause instanceof JsonMappingException jsonMappingException) {
      message = jsonMappingException.getOriginalMessage();
    } else if (rootCause instanceof InvalidInputException invalidInputException) {
      message = invalidInputException.getErrors().first().getDetail();
    }

    String pointer = null;
    if (processingException instanceof JsonMappingException jsonMappingException) {
      List<String> nodes =
          jsonMappingException.getPath().stream()
              .map(JsonException::toNodeName)
              .collect(Collectors.toList());
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

  private static String toNodeName(JsonMappingException.Reference input) {
    return input.getFieldName() != null ? input.getFieldName() : String.valueOf(input.getIndex());
  }
}
