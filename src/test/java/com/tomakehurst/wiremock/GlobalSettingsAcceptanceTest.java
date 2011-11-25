package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.tomakehurst.wiremock.client.WireMock.get;
import static com.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.tomakehurst.wiremock.client.WireMock;

public class GlobalSettingsAcceptanceTest extends AcceptanceTestBase {

	@Test
	public void settingGlobalFixedResponseDelay() {
		WireMock.setGlobalFixedDelay(500);
		givenThat(get(urlEqualTo("/globally/delayed/resource")).willReturn(aResponse().withStatus(200)));
        
	    long start = System.currentTimeMillis();
        testClient.get("/globally/delayed/resource");
        int duration = (int) (System.currentTimeMillis() - start);
        
        assertThat(duration, greaterThanOrEqualTo(500));
	}
	
}
