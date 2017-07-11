package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;

@JsonDeserialize(using = SnapshotRecordResultDeserialiser.class)
public class SnapshotRecordResult {

    protected final List<StubMapping> stubMappings;

    protected SnapshotRecordResult(List<StubMapping> mappings) {
        this.stubMappings = mappings;
    }

    @JsonIgnore
    public List<StubMapping> getStubMappings() {
        return stubMappings;
    }

    public static SnapshotRecordResult full(List<StubMapping> stubMappings) {
        return new Full(stubMappings);
    }

    public static SnapshotRecordResult ids(List<StubMapping> stubMappings) {
        return new Ids(stubMappings);
    }

    public static class Full extends SnapshotRecordResult {

        public Full(List<StubMapping> mappings) {
            super(mappings);
        }

        public List<StubMapping> getMappings() {
            return stubMappings;
        }
    }

    public static class Ids extends SnapshotRecordResult {

        private final List<UUID> ids;

        public Ids(List<StubMapping> mappings) {
            super(mappings);
            this.ids = Lists.transform(mappings, new Function<StubMapping, UUID>() {
                @Override
                public UUID apply(StubMapping input) {
                    return input.getId();
                }
            });
        }

        public List<UUID> getIds() {
            return ids;
        }
    }


}
