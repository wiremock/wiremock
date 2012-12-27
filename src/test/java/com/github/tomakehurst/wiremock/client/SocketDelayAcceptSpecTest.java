package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.mapping.Json;
import org.junit.Test;

import static net.sf.json.test.JSONAssert.assertJsonEquals;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SocketDelayAcceptSpecTest {

    @Test
    public void canSerialiseToJson() {
        String json = Json.write(SocketAcceptDelaySpec.ofMilliseconds(500).forNumRequests(2));
        assertJsonEquals("{ \"milliseconds\": 500, \"requestCount\": 2 }", json);
    }

    @Test
    public void canDeserialiseFromJson() {
        SocketAcceptDelaySpec spec =
                Json.read("{ \"milliseconds\": 80, \"requestCount\": 10 }", SocketAcceptDelaySpec.class);

        assertThat(spec.milliseconds(), is(80l));
        assertThat(spec.requestCount(), is(10));
    }
}
