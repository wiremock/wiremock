package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

/**
 * Possible output formats for snapshot task
 */
public enum SnapshotOutputFormat {
    FULL {
        @Override
        public Object format(StubMapping stubMapping) {
            return stubMapping;
        }
    },
    IDS {
        @Override
        public Object format(StubMapping stubMapping) {
            return stubMapping.getId();
        }
    };

    public abstract Object format(StubMapping stubMapping);

    @JsonCreator
    public static SnapshotOutputFormat fromString(String value) {
        if (value == null || value.equalsIgnoreCase("ids")) {
            return IDS;
        }
        return FULL;
    }
}
