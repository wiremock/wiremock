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
package com.github.tomakehurst.wiremock.message;

import static com.github.tomakehurst.wiremock.message.ChannelType.Directionality.BIDIRECTIONAL;
import static com.github.tomakehurst.wiremock.message.ChannelType.Lifecycle.REQUEST_INITIATED;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public class ChannelType {

  public enum Lifecycle {
    REQUEST_INITIATED
  }

  public enum Directionality {
    BIDIRECTIONAL
  }

  private final String name;
  private final Lifecycle lifecycle;
  private final Directionality directionality;

  public ChannelType(String name, Lifecycle lifecycle, Directionality directionality) {
    this.name = name;
    this.lifecycle = lifecycle;
    this.directionality = directionality;
  }

  public boolean isRequestInitiated() {
    return getLifecycle() == REQUEST_INITIATED;
  }

  public String getName() {
    return name;
  }

  public Lifecycle getLifecycle() {
    return lifecycle;
  }

  public Directionality getDirectionality() {
    return directionality;
  }

  @JsonCreator
  public static ChannelType fromJson(String value) {
    return WEBSOCKET;
  }

  @JsonValue
  public String toJson() {
    return getName().toLowerCase(Locale.ROOT);
  }

  public static ChannelType WEBSOCKET =
      new ChannelType("websocket", REQUEST_INITIATED, BIDIRECTIONAL);
}
