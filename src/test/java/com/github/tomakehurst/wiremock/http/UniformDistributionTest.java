package com.github.tomakehurst.wiremock.http;


import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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