package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RecordingStatusResultTest {

    @Test
    public void deserialise() {
        RecordingStatusResult result = Json.read("{ \"status\": \"Recording\" }", RecordingStatusResult.class);

        assertThat(result.getStatus(), is(RecordingStatus.Recording));
    }

    @Test
    public void serialise() {
        RecordingStatusResult result = new RecordingStatusResult(RecordingStatus.Recording);

        String json = Json.write(result);

        assertThat(json, equalToJson("{ \"status\": \"Recording\" }"));
    }
}
