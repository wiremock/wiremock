package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;

import java.util.Objects;

// Matches the size of the body of a ResponseDefinition, for use by the Snapshot API when determining if the body
// should be extracted to a file.
@JsonDeserialize(using = ResponseDefinitionBodyMatcherDeserializer.class)
public class ResponseDefinitionBodyMatcher implements ValueMatcher<ResponseDefinition> {

    public static final long DEFAULT_MAX_TEXT_SIZE = 100 * 1024 * 1024;
    public static final long DEFAULT_MAX_BINARY_SIZE = 100 * 1024 * 1024;

    private final long textSizeThreshold;
    private final long binarySizeThreshold;

    public ResponseDefinitionBodyMatcher(long textSizeThreshold, long binarySizeThreshold) {
        this.textSizeThreshold = textSizeThreshold;
        this.binarySizeThreshold = binarySizeThreshold;
    }

    public String getTextSizeThreshold() {
        return String.valueOf(textSizeThreshold);
    }

    public String getBinarySizeThreshold() {
        return String.valueOf(binarySizeThreshold);
    }

    @Override
    public MatchResult match(ResponseDefinition responseDefinition) {
        if (!responseDefinition.specifiesBodyContent()) {
            return MatchResult.noMatch();
        } else if (
            responseDefinition.getHeaders() != null
            && ContentTypes.determineIsTextFromMimeType(responseDefinition.getHeaders().getContentTypeHeader().mimeTypePart())
        ) {
            if (responseDefinition.getBody().length() > textSizeThreshold) {
                return MatchResult.exactMatch();
            } else {
                return MatchResult.noMatch();
            }
        } else {
            if (responseDefinition.getByteBody().length > binarySizeThreshold) {
                return MatchResult.exactMatch();
            } else {
                return MatchResult.noMatch();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResponseDefinitionBodyMatcher that = (ResponseDefinitionBodyMatcher) o;

        return Objects.equals(textSizeThreshold, that.textSizeThreshold)
            && Objects.equals(binarySizeThreshold, that.binarySizeThreshold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(textSizeThreshold, binarySizeThreshold);
    }
}
