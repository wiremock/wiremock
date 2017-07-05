package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.SnapshotOutputFormatter;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.*;

import static com.github.tomakehurst.wiremock.admin.model.SnapshotOutputFormatter.FULL;
import static com.github.tomakehurst.wiremock.admin.model.SnapshotOutputFormatter.IDS;
import static org.junit.Assert.assertEquals;

public class SnapshotOutputFormatterTest {
    @Test
    public void fromStringDefault() {
        assertEquals(FULL, SnapshotOutputFormatter.fromString(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromStringWithInvalidFormat() {
        SnapshotOutputFormatter.fromString("invalid output format");
    }

    @Test
    public void fromStringWithFull() {
        assertEquals(FULL, SnapshotOutputFormatter.fromString("full"));
    }

    @Test
    public void fromStringWithIds() {
        assertEquals(IDS, SnapshotOutputFormatter.fromString("ids"));
    }

    @Test
    public void formatIds() {
        List<StubMapping> stubMappings = Lists.newArrayList(new StubMapping(), new StubMapping());

        Map<String, List<UUID>> expected = new HashMap<>();
        expected.put("ids", Lists.newArrayList(
            stubMappings.get(0).getId(),
            stubMappings.get(1).getId()
        ));

        assertEquals(expected, IDS.format(stubMappings));
    }

    @Test
    public void formatFull() {
        List<StubMapping> stubMappings = Lists.newArrayList(new StubMapping(), new StubMapping());

        Map<String, List<StubMapping>> expected = new HashMap<>();
        expected.put("mappings", stubMappings);

        assertEquals(expected, FULL.format(stubMappings));
    }
}
