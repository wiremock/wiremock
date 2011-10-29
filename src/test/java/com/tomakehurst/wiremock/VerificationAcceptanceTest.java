package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.tomakehurst.wiremock.client.WireMock.verify;

import org.junit.Test;

public class VerificationAcceptanceTest extends FluentAPITestBase {

	@Test
	public void looselyVerifiesRequestBasedOnUrlOnly() {
		testClient.get("/this/got/requested");
		verify(getRequestedFor(urlEqualTo("/this/got/requested")));
	}
}
