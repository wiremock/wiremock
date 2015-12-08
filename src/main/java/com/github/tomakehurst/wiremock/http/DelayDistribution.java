package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Distribution that models delays.
 *
 * Implementations should be thread safe.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LogNormal.class, name = "lognormal")
})
public interface DelayDistribution {
    /**
     * Samples a delay in milliseconds from the distribution.
     *
     * @return next delay in millis
     */
    long sampleMillis();
}
