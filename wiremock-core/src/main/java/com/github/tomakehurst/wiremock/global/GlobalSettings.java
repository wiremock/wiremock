/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.global;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.DelayDistribution;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class GlobalSettings {

  @Nullable private final Integer fixedDelay;
  @Nullable private final DelayDistribution delayDistribution;
  @Nullable private final Parameters extended;

  private final boolean proxyPassThrough;

  public static GlobalSettings.Builder builder() {
    return new Builder();
  }

  public static GlobalSettings defaults() {
    return new Builder().build();
  }

  public GlobalSettings(
      @JsonProperty("fixedDelay") @Nullable Integer fixedDelay,
      @JsonProperty("delayDistribution") @Nullable DelayDistribution delayDistribution,
      @JsonProperty("extended") @Nullable Parameters extended,
      @JsonProperty("proxyPassThrough") boolean proxyPassThrough) {
    this.fixedDelay = fixedDelay;
    this.delayDistribution = delayDistribution;
    this.extended = extended;
    this.proxyPassThrough = proxyPassThrough;
  }

  public @Nullable Integer getFixedDelay() {
    return fixedDelay;
  }

  public @Nullable DelayDistribution getDelayDistribution() {
    return delayDistribution;
  }

  public @Nullable Parameters getExtended() {
    return extended;
  }

  public boolean getProxyPassThrough() {
    return proxyPassThrough;
  }

  public GlobalSettings.Builder copy() {
    return new Builder()
        .fixedDelay(fixedDelay)
        .delayDistribution(delayDistribution)
        .extended(extended);
  }

  public GlobalSettings transform(Consumer<Builder> transformer) {
    final Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  private Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GlobalSettings that = (GlobalSettings) o;
    return Objects.equals(getFixedDelay(), that.getFixedDelay())
        && Objects.equals(getDelayDistribution(), that.getDelayDistribution())
        && Objects.equals(getExtended(), that.getExtended());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFixedDelay(), getDelayDistribution(), getExtended());
  }

  public static class Builder {
    @Nullable private Integer fixedDelay;
    @Nullable private DelayDistribution delayDistribution;
    @Nullable private Parameters extended;

    private boolean proxyPassThrough = true;

    public Builder() {}

    public Builder(GlobalSettings existing) {
      this.fixedDelay = existing.getFixedDelay();
      this.delayDistribution = existing.getDelayDistribution();
      this.extended = existing.getExtended();
      this.proxyPassThrough = existing.getProxyPassThrough();
    }

    public Builder fixedDelay(@Nullable Integer fixedDelay) {
      this.fixedDelay = fixedDelay;
      return this;
    }

    public Builder delayDistribution(@Nullable DelayDistribution delayDistribution) {
      this.delayDistribution = delayDistribution;
      return this;
    }

    public Builder extended(@Nullable Parameters extended) {
      this.extended = extended;
      return this;
    }

    public Builder proxyPassThrough(boolean proxyPassThrough) {
      this.proxyPassThrough = proxyPassThrough;
      return this;
    }

    public @Nullable Integer getFixedDelay() {
      return fixedDelay;
    }

    public @Nullable DelayDistribution getDelayDistribution() {
      return delayDistribution;
    }

    public @Nullable Parameters getExtended() {
      return extended;
    }

    public boolean isProxyPassThrough() {
      return proxyPassThrough;
    }

    public Builder setFixedDelay(@Nullable Integer fixedDelay) {
      this.fixedDelay = fixedDelay;
      return this;
    }

    public Builder setDelayDistribution(@Nullable DelayDistribution delayDistribution) {
      this.delayDistribution = delayDistribution;
      return this;
    }

    public Builder setExtended(@Nullable Parameters extended) {
      this.extended = extended;
      return this;
    }

    public Builder setProxyPassThrough(boolean proxyPassThrough) {
      this.proxyPassThrough = proxyPassThrough;
      return this;
    }

    public GlobalSettings build() {
      return new GlobalSettings(fixedDelay, delayDistribution, extended, proxyPassThrough);
    }
  }
}
