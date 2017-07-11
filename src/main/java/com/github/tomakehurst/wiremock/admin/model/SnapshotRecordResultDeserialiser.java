package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.io.IOException;
import java.util.List;

public class SnapshotRecordResultDeserialiser extends StdDeserializer<SnapshotRecordResult> {

    protected SnapshotRecordResultDeserialiser() {
        super(SnapshotRecordResult.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SnapshotRecordResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String name = parser.nextFieldName();
        parser.nextToken();
        List<StubMapping> mappings = deserializationContext.readValue(parser, List.class);
        if (name.equals("mappings")) {
            return SnapshotRecordResult.full(mappings);
        } else if (name.equals("ids")) {
            return SnapshotRecordResult.ids(mappings);
        }

        throw new JsonParseException(parser, "Snapshot result must contain either mappings or ids element");
    }
}
