package org.wiremock.url.whatwg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"comment", "base"})
public record RelativeToFailureWhatWGUrlTestCase(
    // always present, never null, can be empty signifying empty input
    String input,
    RelativeTo relativeTo
) implements FailureWhatWGUrlTestCase {
}
