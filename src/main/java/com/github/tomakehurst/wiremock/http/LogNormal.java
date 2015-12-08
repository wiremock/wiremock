package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Returns log normally distributed values. Takes two parameters, the median (50th percentile) of the lognormal
 * and the standard deviation of the underlying normal distribution.
 *
 * The larger the standard deviation the longer the tails.
 *
 * @see <a href="https://www.wolframalpha.com/input/?i=lognormaldistribution%28log%2890%29%2C+0.1%29">lognormal example</a>
 */
public final class LogNormal implements DelayDistribution {

    @JsonProperty("median")
    private final double median;
    @JsonProperty("sigma")
    private final double sigma;

    /**
     * @param median 50th percentile of the distribution in millis
     * @param sigma standard deviation of the distribution, a larger value produces a longer tail
     */
    @JsonCreator
    public LogNormal(@JsonProperty("median") double median, @JsonProperty("sigma") double sigma) {
        this.median = median;
        this.sigma = sigma;
    }

    @Override
    public long sampleMillis() {
        return Math.round(Math.exp(ThreadLocalRandom.current().nextGaussian() * sigma) * median);
    }
}
