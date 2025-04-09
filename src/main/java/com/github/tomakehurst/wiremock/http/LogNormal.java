/*
 * Copyright (C) 2015-2021 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Returns log normally distributed values. Takes two parameters, the median (50th percentile) of
 * the lognormal and the standard deviation of the underlying normal distribution, plus an optional
 * maximum value to truncate the result by resampling to prevent an extra long tail
 *
 * <p>The larger the standard deviation the longer the tails.
 *
 * @see <a
 *     href="https://www.wolframalpha.com/input/?i=lognormaldistribution%28log%2890%29%2C+0.1%29">lognormal
 *     example</a>
 */
public final class LogNormal implements DelayDistribution {

  @JsonProperty("median")
  private final double median;

  @JsonProperty("sigma")
  private final double sigma;

  @JsonProperty(value = "maxValue", required = false)
  private final Long maxValue;

  /**
   * @param median 50th percentile of the distribution in millis
   * @param sigma standard deviation of the distribution, a larger value produces a longer tail
   * @param maxValue the maximum value to truncate the distribution at, or null to disable truncation
   */
  @JsonCreator
  public LogNormal(
      @JsonProperty("median") double median,
      @JsonProperty("sigma") double sigma,
      @JsonProperty("maxValue") Long maxValue) {
    this.median = median;
    this.sigma = sigma;
    this.maxValue = maxValue;

    if (maxValue != null && maxValue < median) {
      throw new IllegalArgumentException(
          "The max value has to be greater than or equal to the median");
    }
  }

  /**
   * @param median 50th percentile of the distribution in millis
   * @param sigma standard deviation of the distribution, a larger value produces a longer tail
   */
  public LogNormal(double median, double sigma) {
    // Initialise maxValue to null to disable long tail truncation
    this(median, sigma, null);
  }

  @Override
  public long sampleMillis() {

    long generatedValue = generateDelayMillis();

    if (maxValue == null) {
      // Don't want to truncate any potential long tails
      return generatedValue;
    }

    // Rather than truncating the value at the max, if it's over the max value, then resample, but
    // only do that a few times
    int i = 0;
    while (generatedValue > maxValue && i < 10) {
      generatedValue = generateDelayMillis();
      i++;
    }

    // Belt and braces, in the unlikely event the generated value is still over the max, truncate
    // it at the max
    return Math.round(Math.min(maxValue, generatedValue));
  }

  private long generateDelayMillis() {
    return Math.round(Math.exp(ThreadLocalRandom.current().nextGaussian() * sigma) * median);
  }
}
