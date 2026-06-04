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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ChannelProvider {

  private final String name;
  private final String driverType;
  private final Map<String, Object> settings;

  @JsonCreator
  public ChannelProvider(
      @JsonProperty("name") String name,
      @JsonProperty("driverType") String driverType,
      @Nullable @JsonProperty("settings") Map<String, Object> settings) {
    this.name = name;
    this.driverType = driverType;
    this.settings =
        settings != null ? Collections.unmodifiableMap(settings) : Collections.emptyMap();
  }

  public String getName() {
    return name;
  }

  public String getDriverType() {
    return driverType;
  }

  public Map<String, Object> getSettings() {
    return settings;
  }

  @Override
  public boolean equals(Object o) {
    if (getClass() != o.getClass()) return false;
    ChannelProvider that = (ChannelProvider) o;
    return Objects.equals(name, that.name)
        && Objects.equals(driverType, that.driverType)
        && Objects.equals(settings, that.settings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, driverType, settings);
  }

  public static Builder named(String name) {
    return new Builder(name);
  }

  @NullUnmarked
  public static class Builder {
    private String name;
    private String driverType;
    private final Map<String, Object> settings = new HashMap<>();

    public Builder() {}

    public Builder(String name) {
      this.name = name;
    }

    public Builder named(String name) {
      this.name = name;
      return this;
    }

    public Builder withDriver(String driverType) {
      this.driverType = driverType;
      return this;
    }

    public Builder withSetting(String key, Object value) {
      this.settings.put(key, value);
      return this;
    }

    public ChannelProvider build() {
      return new ChannelProvider(name, driverType, settings);
    }
  }
}
