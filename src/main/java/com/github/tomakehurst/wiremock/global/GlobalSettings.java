/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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

/** The type Global settings. */
public class GlobalSettings {

  private final Integer fixedDelay;
  private final DelayDistribution delayDistribution;
  private final Parameters extended;

  private final boolean proxyPassThrough;

  /**
   * Builder global settings . builder.
   *
   * @return the global settings . builder
   */
  public static GlobalSettings.Builder builder() {
    return new Builder();
  }

  /**
   * Defaults global settings.
   *
   * @return the global settings
   */
  public static GlobalSettings defaults() {
    return new Builder().build();
  }

  /**
   * Instantiates a new Global settings.
   *
   * @param fixedDelay the fixed delay
   * @param delayDistribution the delay distribution
   * @param extended the extended
   * @param proxyPassThrough the proxy pass through
   */
  public GlobalSettings(
      @JsonProperty("fixedDelay") Integer fixedDelay,
      @JsonProperty("delayDistribution") DelayDistribution delayDistribution,
      @JsonProperty("extended") Parameters extended,
      @JsonProperty("proxyPassThrough") boolean proxyPassThrough) {
    this.fixedDelay = fixedDelay;
    this.delayDistribution = delayDistribution;
    this.extended = extended;
    this.proxyPassThrough = proxyPassThrough;
  }

  /**
   * Gets fixed delay.
   *
   * @return the fixed delay
   */
  public Integer getFixedDelay() {
    return fixedDelay;
  }

  /**
   * Gets delay distribution.
   *
   * @return the delay distribution
   */
  public DelayDistribution getDelayDistribution() {
    return delayDistribution;
  }

  /**
   * Gets extended.
   *
   * @return the extended
   */
  public Parameters getExtended() {
    return extended;
  }

  /**
   * Gets proxy pass through.
   *
   * @return the proxy pass through
   */
  public boolean getProxyPassThrough() {
    return proxyPassThrough;
  }

  /**
   * Copy global settings . builder.
   *
   * @return the global settings . builder
   */
  public GlobalSettings.Builder copy() {
    return new Builder()
        .fixedDelay(fixedDelay)
        .delayDistribution(delayDistribution)
        .extended(extended);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GlobalSettings that = (GlobalSettings) o;
    return Objects.equals(getFixedDelay(), that.getFixedDelay())
        && Objects.equals(getDelayDistribution(), that.getDelayDistribution())
        && Objects.equals(getExtended(), that.getExtended());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFixedDelay(), getDelayDistribution(), getExtended());
  }

  /** The type Builder. */
  public static class Builder {
    private Integer fixedDelay;
    private DelayDistribution delayDistribution;
    private Parameters extended;

    private boolean proxyPassThrough = true;

    /**
     * Fixed delay builder.
     *
     * @param fixedDelay the fixed delay
     * @return the builder
     */
    public Builder fixedDelay(Integer fixedDelay) {
      this.fixedDelay = fixedDelay;
      return this;
    }

    /**
     * Delay distribution builder.
     *
     * @param delayDistribution the delay distribution
     * @return the builder
     */
    public Builder delayDistribution(DelayDistribution delayDistribution) {
      this.delayDistribution = delayDistribution;
      return this;
    }

    /**
     * Extended builder.
     *
     * @param extended the extended
     * @return the builder
     */
    public Builder extended(Parameters extended) {
      this.extended = extended;
      return this;
    }

    /**
     * Proxy pass through builder.
     *
     * @param proxyPassThrough the proxy pass through
     * @return the builder
     */
    public Builder proxyPassThrough(boolean proxyPassThrough) {
      this.proxyPassThrough = proxyPassThrough;
      return this;
    }

    /**
     * Build global settings.
     *
     * @return the global settings
     */
    public GlobalSettings build() {
      return new GlobalSettings(fixedDelay, delayDistribution, extended, proxyPassThrough);
    }
  }
}
