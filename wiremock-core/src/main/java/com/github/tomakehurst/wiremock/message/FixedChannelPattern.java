/*
 * Copyright (C) 2026 Thomas Akehurst
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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@JsonInclude(NON_EMPTY)
@NullMarked
public class FixedChannelPattern implements ChannelPattern {

  private final ChannelType channelType;
  private final String channelProvider;
  @Nullable private final StringValuePattern channelName;

  @JsonCreator
  public FixedChannelPattern(
      @JsonProperty("type") ChannelType type,
      @JsonProperty("channelProvider") String channelProvider,
      @Nullable @JsonProperty("channelName") StringValuePattern channelName) {
    this.channelType = type;
    this.channelProvider = channelProvider;
    this.channelName = channelName;
  }

  public static FixedChannelPattern forChannel(
      String channelProvider, StringValuePattern channelName) {
    return new FixedChannelPattern(ChannelType.FIXED, channelProvider, channelName);
  }

  @Override
  @JsonInclude
  public ChannelType getType() {
    return channelType;
  }

  public String getChannelProvider() {
    return channelProvider;
  }

  @Nullable
  public StringValuePattern getChannelName() {
    return channelName;
  }

  @Override
  public boolean matches(MessageChannel channel) {
    if (!(channel instanceof FixedChannel fixedChannel)) {
      return false;
    }
    if (!Objects.equals(channelProvider, fixedChannel.getProviderName())) {
      return false;
    }
    if (channelName != null) {
      return channelName.match(fixedChannel.getChannelName()).isExactMatch();
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FixedChannelPattern that = (FixedChannelPattern) o;
    return Objects.equals(channelProvider, that.channelProvider)
        && Objects.equals(channelName, that.channelName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelProvider, channelName);
  }
}
