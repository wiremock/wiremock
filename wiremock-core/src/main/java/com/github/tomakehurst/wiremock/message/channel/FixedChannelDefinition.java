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
package com.github.tomakehurst.wiremock.message.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;

@NullMarked
public class FixedChannelDefinition {

  private final String providerName;
  private final String channelName;

  @JsonCreator
  public FixedChannelDefinition(
      @JsonProperty("providerName") String providerName,
      @JsonProperty("channelName") String channelName) {
    this.providerName = providerName;
    this.channelName = channelName;
  }

  public String getProviderName() {
    return providerName;
  }

  public String getChannelName() {
    return channelName;
  }

  @Override
  public boolean equals(Object o) {
    if (getClass() != o.getClass()) return false;
    FixedChannelDefinition that = (FixedChannelDefinition) o;
    return Objects.equals(providerName, that.providerName)
        && Objects.equals(channelName, that.channelName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(providerName, channelName);
  }

  @NullUnmarked
  public static class Builder {
    private String providerName;
    private String name;

    public Builder() {}

    public Builder(String providerName) {
      this.providerName = providerName;
    }

    public Builder onProvider(String providerName) {
      this.providerName = providerName;
      return this;
    }

    public Builder named(String name) {
      this.name = name;
      return this;
    }

    public FixedChannelDefinition build() {
      return new FixedChannelDefinition(providerName, name);
    }
  }
}
