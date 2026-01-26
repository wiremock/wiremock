/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.message.MessagePattern;

public class WaitForMessageEventRequest {

  private final MessagePattern pattern;
  private final long timeoutMillis;
  private final Integer count;

  @JsonCreator
  public WaitForMessageEventRequest(
      @JsonProperty("pattern") MessagePattern pattern,
      @JsonProperty("timeoutMillis") long timeoutMillis,
      @JsonProperty("count") Integer count) {
    this.pattern = pattern != null ? pattern : MessagePattern.ANYTHING;
    this.timeoutMillis = timeoutMillis;
    this.count = count;
  }

  public static WaitForMessageEventRequest forSingleEvent(
      MessagePattern pattern, long timeoutMillis) {
    return new WaitForMessageEventRequest(pattern, timeoutMillis, null);
  }

  public static WaitForMessageEventRequest forMultipleEvents(
      MessagePattern pattern, long timeoutMillis, int count) {
    return new WaitForMessageEventRequest(pattern, timeoutMillis, count);
  }

  public MessagePattern getPattern() {
    return pattern;
  }

  public long getTimeoutMillis() {
    return timeoutMillis;
  }

  public Integer getCount() {
    return count;
  }

  @com.fasterxml.jackson.annotation.JsonIgnore
  public boolean isMultipleEvents() {
    return count != null && count > 1;
  }
}
