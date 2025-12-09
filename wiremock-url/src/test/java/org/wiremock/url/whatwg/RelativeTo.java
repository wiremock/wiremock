package org.wiremock.url.whatwg;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RelativeTo {
  @JsonProperty("non-opaque-path-base")
  NonOpaquePathBase,
  @JsonProperty("any-base")
  AnyBase
}
