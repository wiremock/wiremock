/*
 * Copyright (C) 2015-2025 Thomas Akehurst
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class UniformDistributionTest {

  @Test
  public void valuesAreWithinInclusiveBounds() {
    UniformDistribution d = new UniformDistribution(50, 60);
    for (int i = 0; i < 10_000; i++) {
      long v = d.sampleMillis();
      assertThat(v, greaterThanOrEqualTo(50L));
      assertThat(v, lessThanOrEqualTo(60L));
    }
  }

  @Test
  public void producesBothLowerAndUpperBoundsOverManySamples() {
    UniformDistribution d = new UniformDistribution(10, 15);
    boolean sawLower = false;
    boolean sawUpper = false;

    for (int i = 0; i < 20_000 && !(sawLower && sawUpper); i++) {
      long v = d.sampleMillis();
      if (v == 10) sawLower = true;
      if (v == 15) sawUpper = true;
    }

    assertThat("lower bound not observed", sawLower, is(true));
    assertThat("upper bound not observed", sawUpper, is(true));
  }

  @Test
  public void meanIsCloseToMidpoint() {
    int lower = 100;
    int upper = 200;
    double expectedMean = (lower + upper) / 2.0; // 150

    UniformDistribution d = new UniformDistribution(lower, upper);

    int n = 20_000;
    long sum = 0;
    for (int i = 0; i < n; i++) {
      sum += d.sampleMillis();
    }
    double mean = sum / (double) n;

    // With n=20000 on a width-100 uniform, standard error ~ (width)/sqrt(12*n) ~ 100/sqrt(240000)
    // ~ 0.2, so tolerance 1.0 is generous and very stable
    assertEquals(expectedMean, mean, 1.0);
  }

  @Test
  public void lowerEqualsUpperAlwaysReturnsThatValue() {
    UniformDistribution d = new UniformDistribution(123, 123);
    for (int i = 0; i < 1000; i++) {
      assertThat(d.sampleMillis(), is(123L));
    }
  }

  @Test
  public void supportsNegativeRanges() {
    UniformDistribution d = new UniformDistribution(-5, 5);
    boolean sawNegative = false;
    boolean sawPositive = false;

    for (int i = 0; i < 20_000; i++) {
      long v = d.sampleMillis();
      assertThat(v, greaterThanOrEqualTo(-5L));
      assertThat(v, lessThanOrEqualTo(5L));
      if (v < 0) sawNegative = true;
      if (v > 0) sawPositive = true;
    }

    assertThat(sawNegative, is(true));
    assertThat(sawPositive, is(true));
    
 @Test 
 public void shouldReturnAllValuesInTheRange() {
    DelayDistribution distribution = new UniformDistribution(3, 4);

    boolean[] found = new boolean[5];
    Arrays.fill(found, false);

    for (int i = 0; i < 100; i++) {
      found[(int) distribution.sampleMillis()] = true;
    }

    assertThat("found 0", found[0], is(false));
    assertThat("found 1", found[1], is(false));
    assertThat("found 2", found[2], is(false));
    assertThat("found 3", found[3], is(true));
    assertThat("found 4", found[4], is(true));
  }
}
