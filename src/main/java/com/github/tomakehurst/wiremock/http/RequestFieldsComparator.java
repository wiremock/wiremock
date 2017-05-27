package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;

/**
 * Contains a list of request fields that can be used to sort/compare StubMappings
 */
public class RequestFieldsComparator implements Comparator<StubMapping> {
    private final List<String> fields;

    @JsonCreator
    public RequestFieldsComparator(@JsonProperty("fields") String ...fields) {
        this.fields = Lists.newArrayList(fields);
    }

    @Override
    public int compare(StubMapping one, StubMapping two) {
        int result = 0;
        for (String field : fields) {
            result = compareByField(field, one.getRequest(), two.getRequest());
            if (result != 0) {
                break;
            }
        }
        return result;
    }

    /**
     * Compares two RequestPatterns in terms of the field.
     */
    private int compareByField(String field, RequestPattern one, RequestPattern two) {
        switch (field) {
            case "url":
                if (one.getUrl() == null && two.getUrl() != null) {
                    return -1;
                } else if (one.getUrl() != null && two.getUrl() == null) {
                    return 1;
                } else if (one.getUrl() == null && two.getUrl() == null) {
                    return 0;
                }
                return one.getUrl().compareToIgnoreCase(two.getUrl());
            case "method":
                return one.getMethod().equals(two.getMethod()) ? 0 : 1;
            default: // assume field is a header
                if (one.getHeaders() == null) {
                    return -1;
                } else if (two.getHeaders() == null) {
                    return 1;
                }

                MultiValuePattern headerOne = one.getHeaders().get(field);
                MultiValuePattern headerTwo = two.getHeaders().get(field);
                if (headerOne == null) {
                    return -1;
                } else if (headerTwo == null) {
                    return 1;
                }
                return headerOne.getExpected().compareToIgnoreCase(headerTwo.getExpected());
        }
    }
}
