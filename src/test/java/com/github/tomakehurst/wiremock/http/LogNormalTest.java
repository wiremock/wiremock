package com.github.tomakehurst.wiremock.http;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
public class LogNormalTest {

    // To test properly we would need something like a normality test.
    // For our purposes, a simple verification is sufficient.
    @Test
    public void samplingLogNormalHasExpectedMean() {
        LogNormal distribution = new LogNormal(90.0, 0.39);
        int n = 10000;

        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += distribution.sampleMillis();
        }

        assertEquals(97.1115, sum / (double) n, 5.0);
    }
}
