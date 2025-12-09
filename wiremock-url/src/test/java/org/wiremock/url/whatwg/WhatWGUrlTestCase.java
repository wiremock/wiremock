package org.wiremock.url.whatwg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public sealed interface WhatWGUrlTestCase permits FailureWhatWGUrlTestCase,
    SuccessWhatWGUrlTestCase {

  default boolean success() {
    return !failure();
  }

  String input();

  boolean failure();
}
