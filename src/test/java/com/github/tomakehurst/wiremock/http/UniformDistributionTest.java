/*
 * Copyright (C) 2011 Thomas Akehurst
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


import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UniformDistributionTest {

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