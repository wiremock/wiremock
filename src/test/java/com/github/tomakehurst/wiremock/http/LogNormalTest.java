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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LogNormalTest {

  private final double median = 90.0;
  private final double sigma = 0.39;

  @Test
  public void samplingLogNormalHasExpectedMean() {
    LogNormal distribution = new LogNormal(median, sigma);
    samplingLogNormalHasExpectedMean(distribution, 97.1115);
  }

  @Test
  public void samplingTruncatedLogNormalWithHighCapHasExpectedMean() {
    samplingTruncatedLogNormalHasExpectedMean(150, 88.15);
  }

  @Test
  public void samplingTruncatedLogNormalWithLowerCapHasExpectedMean() {
    samplingTruncatedLogNormalHasExpectedMean(130, 83.6);
  }

  @Test
  public void samplingTruncatedLogNormalWithCapSameAsMaxHasExpectedMean() {
    // This test should, on occasion, exercise the resampling of the distribution value when the
    // initial generated
    // value(s) are higher than the max.
    samplingTruncatedLogNormalHasExpectedMean((long) median, 67.82);
  }

  @Test
  public void samplingTruncatedLogNormalFailsIfMaxLessThanMedian() {
    try {
      new LogNormal(median, sigma, (long) median);
    } catch (IllegalArgumentException ex) {
      // Fail - max = median is okay
      Assertions.fail("A maxValue matching median should not throw an exception");
    }

    try {
      new LogNormal(median, sigma, ((long) median) - 1);
      Assertions.fail("A maxValue less than median should throw an exception");
    } catch (IllegalArgumentException ex) {
      // Exception expected
    }
  }

  private void samplingTruncatedLogNormalHasExpectedMean(long maxCapValue, double expectedMean) {
    LogNormal distribution = new LogNormal(median, sigma, maxCapValue);
    samplingLogNormalHasExpectedMean(distribution, expectedMean);
  }

  // To test properly we would need something like a normality test.
  // For our purposes, a simple verification is sufficient.
  private void samplingLogNormalHasExpectedMean(LogNormal distribution, double expectedMean) {

    int n = 10000;

    long sum = 0;
    for (int i = 0; i < n; i++) {
      long val = distribution.sampleMillis();
      sum += val;
    }

    assertEquals(expectedMean, sum / (double) n, 5.0);
  }
}
