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
}
