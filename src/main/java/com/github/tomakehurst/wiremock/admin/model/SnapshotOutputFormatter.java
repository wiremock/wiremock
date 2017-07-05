package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.*;

/**
 * Generates response for calls to snapshot API endpoint using the recorded stub mappings
 */
public enum SnapshotOutputFormatter {
    FULL {
        @Override
        public Map<String, List> format(List<StubMapping> stubMappings) {
            final Map<String, List> output = new HashMap<>();
            output.put("mappings", stubMappings);
            return output;
        }
    },
    IDS {
        @Override
        public Map<String, List> format(List<StubMapping> stubMappings) {
            final List<UUID> stubMappingIds = new ArrayList<>(stubMappings.size());
            for (StubMapping stubMapping : stubMappings) {
                stubMappingIds.add(stubMapping.getId());
            }

            final Map<String, List> output = new HashMap<>();
            output.put("ids", stubMappingIds);
            return output;
        }
    };

    public abstract Map<String, List> format(List<StubMapping> stubMappings);

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
