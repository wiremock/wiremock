package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.Collections;
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

    public static SnapshotRecordResult idsFromMappings(List<StubMapping> stubMappings) {
        return new Ids(Lists.transform(stubMappings, new Function<StubMapping, UUID>() {
            @Override
            public UUID apply(StubMapping input) {
                return input.getId();
            }
        }));
    }

    public static SnapshotRecordResult ids(List<UUID> ids) {
        return new Ids(ids);
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

        public Ids(List<UUID> ids) {
            super(Collections.<StubMapping>emptyList());
            this.ids = ids;
        }

        public List<UUID> getIds() {
            return ids;
        }
    }


}
