package org.wiremock.url.whatwg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public sealed interface WhatWGUrlTestCase permits FailureWhatWGUrlTestCase,
    SuccessWhatWGUrlTestCase {

  boolean success();

  String input();

  @JsonProperty(value = "failure", access = JsonProperty.Access.READ_ONLY)
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  default boolean failure() {
    return !success();
  }
}
