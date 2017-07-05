package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;

// Matches the size of the body of a ResponseDefinition, for use by the Snapshot API when determining if the body
// should be extracted to a file.
@JsonDeserialize(using = ResponseDefinitionBodyMatcherDeserializer.class)
public class ResponseDefinitionBodyMatcher implements ValueMatcher<ResponseDefinition> {
    private final long textSizeThreshold;
    private final long binarySizeThreshold;

    public ResponseDefinitionBodyMatcher(long textSizeThreshold, long binarySizeThreshold) {
        this.textSizeThreshold = textSizeThreshold;
        this.binarySizeThreshold = binarySizeThreshold;
    }

    @Override
    public MatchResult match(ResponseDefinition responseDefinition) {
        if (!responseDefinition.specifiesBodyContent()) {
            return MatchResult.noMatch();
        } else if (
            responseDefinition.getHeaders() != null
            && responseDefinition.getHeaders().getContentTypeHeader().mimeTypePart() != null
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
}
