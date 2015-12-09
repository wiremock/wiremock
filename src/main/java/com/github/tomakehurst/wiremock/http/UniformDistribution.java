package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Distribution that returns values uniformally distributed across a range.
 *
 * That is, given a uniform distribution of 50 to 60 ms, there will be an equal
 * spread of delays between 50 and 60. This would useful for representing an
 * average delay of 55ms with a +/- 5ms jitter.
 */
public final class UniformDistribution implements DelayDistribution {

    @JsonProperty("lower")
    private final int lower;
    @JsonProperty("upper")
    private final int upper;

    /**
     * @param lower lower bound inclusive
     * @param upper upper bound inclusive
     */
    public UniformDistribution(@JsonProperty("lower") int lower, @JsonProperty("upper") int upper) {
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public long sampleMillis() {
        return ThreadLocalRandom.current().nextLong(lower, upper + 1);
    }
}
