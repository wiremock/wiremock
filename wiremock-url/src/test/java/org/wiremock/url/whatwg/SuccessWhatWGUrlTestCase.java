package org.wiremock.url.whatwg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

// 596 success
@JsonIgnoreProperties("comment")
public record SuccessWhatWGUrlTestCase(

    // always present, never null, can be empty signifying empty input
    String input,

    // always present, can be null, never empty
    // 328 null base & success
    // 268 present base & success
    // 213 null base & failure
    //  60 present base & failure
    @Nullable String base,

    @Override
    @JsonProperty(value = "failure", access = JsonProperty.Access.READ_ONLY)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean failure,

    // can be absent (null), never empty, 536 occurrences
    @Nullable String href,

    // can be absent (null), never empty. What I call base url
    @Nullable String origin,

    // always present, never empty, never just ":" on success
    String protocol,

    // always present, can be empty on success
    String username,

    // always present, can be empty on success
    String password,

    // always present, can be empty (226) on success
    // what I call hostAndPort
    String host,

    // always present, can be empty (226) on success
    // what I call host
    String hostname,

    // sometimes empty on success
    // sometimes absent on success TEST ME
    @Nullable String port,

    // always present, can be empty (21) on success
    String pathname,

    // always present, often empty (531) on success
    String search,

    // sometimes present (9), can be empty (7) on success
    @Nullable String searchParams,

    // always present, often empty (537) on success
    String hash
) implements WhatWGUrlTestCase {
  
}
