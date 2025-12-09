package org.wiremock.url.whatwg;

public sealed interface FailureWhatWGUrlTestCase extends WhatWGUrlTestCase permits
    RelativeToFailureWhatWGUrlTestCase, SimpleFailureWhatWGUrlTestCase {

  default boolean success() {
    return false;
  }
}
