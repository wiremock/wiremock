/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

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
