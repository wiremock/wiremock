package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SnapshotRecordResultDeserialiserTest {

    @Test
    public void supportsFullResponse() {
        SnapshotRecordResult result = Json.read(
            "{                                     \n" +
                "    \"mappings\": [                    \n" +
                "        {                              \n" +
                "            \"request\": {             \n" +
                "                \"url\": \"/hello\",   \n" +
                "                \"method\": \"GET\"    \n" +
                "            },                         \n" +
                "            \"response\": {            \n" +
                "                \"status\": 201        \n" +
                "            }                          \n" +
                "        },                             \n" +
                "        {}                             \n" +
                "    ]                                  \n" +
                "}",
            SnapshotRecordResult.class
        );

        assertThat(result, instanceOf(SnapshotRecordResult.Full.class));
        assertThat(result.getStubMappings().size(), is(2));
    }

    @Test
    public void supportsIdsOnlyResponse() {
        SnapshotRecordResult result = Json.read(
            "{                                                     \n" +
                "    \"ids\": [                                         \n" +
                "        \"d3f32721-ab5e-479c-9f7a-fda76ed5d803\",      \n" +
                "        \"162d0567-4baf-408b-ad7f-41a779638082\",      \n" +
                "        \"02ee46c3-0b49-40ca-a424-8298c099b6db\"       \n" +
                "    ]                                                  \n" +
                "}",
            SnapshotRecordResult.class
        );

        assertThat(result, instanceOf(SnapshotRecordResult.Ids.class));

        SnapshotRecordResult.Ids idsResult = (SnapshotRecordResult.Ids) result;
        assertThat(idsResult.getIds().size(), is(3));
    }
}
