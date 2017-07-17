package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.*;

/**
 * Generates response for calls to snapshot API endpoint using the recorded stub mappings
 */
public enum SnapshotOutputFormatter {
    FULL {
        @Override
        public SnapshotRecordResult format(List<StubMapping> stubMappings) {
            return SnapshotRecordResult.full(stubMappings);
        }
    },
    IDS {
        @Override
        public SnapshotRecordResult format(List<StubMapping> stubMappings) {
            return SnapshotRecordResult.idsFromMappings(stubMappings);
        }
    };

    public abstract SnapshotRecordResult format(List<StubMapping> stubMappings);

    @JsonCreator
    public static SnapshotOutputFormatter fromString(String value) {
        if (value == null || value.equalsIgnoreCase("full")) {
            return FULL;
        } else if (value.equalsIgnoreCase("ids")) {
            return IDS;
        } else {
            throw new IllegalArgumentException("Invalid output format");
        }
    }
}
