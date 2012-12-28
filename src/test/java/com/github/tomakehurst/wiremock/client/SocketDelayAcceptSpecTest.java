package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.mapping.Json;
import org.junit.Test;

import static net.sf.json.test.JSONAssert.assertJsonEquals;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SocketDelayAcceptSpecTest {

    @Test
    public void canSerialiseToJson() {
        String json = Json.write(new RequestDelaySpec(500));
        assertJsonEquals("{ \"milliseconds\": 500 }", json);
    }

    @Test
    public void canDeserialiseFromJson() {
        RequestDelaySpec spec =
                Json.read("{ \"milliseconds\": 80 }", RequestDelaySpec.class);

        assertThat(spec.milliseconds(), is(80));
    }
}
