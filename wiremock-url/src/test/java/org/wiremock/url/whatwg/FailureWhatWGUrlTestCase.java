package org.wiremock.url.whatwg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public sealed interface FailureWhatWGUrlTestCase extends WhatWGUrlTestCase permits
    RelativeToFailureWhatWGUrlTestCase, SimpleFailureWhatWGUrlTestCase {

  @Override
  @JsonProperty(value = "failure", access = JsonProperty.Access.READ_ONLY)
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  default boolean failure() {
    return true;
  }
}
