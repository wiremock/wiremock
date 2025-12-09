package org.wiremock.url.whatwg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties({"comment"})
public record SimpleFailureWhatWGUrlTestCase(
    // always present, never null, can be empty signifying empty input
    String input,
// always present, can be null, never empty
    // 213 null base & failure
    //  60 present base & failure
    @Nullable String base
    ) implements FailureWhatWGUrlTestCase {
}
