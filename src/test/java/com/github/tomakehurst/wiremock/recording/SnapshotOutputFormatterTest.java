package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.recording.SnapshotOutputFormatter;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.recording.SnapshotOutputFormatter.FULL;
import static com.github.tomakehurst.wiremock.recording.SnapshotOutputFormatter.IDS;
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
