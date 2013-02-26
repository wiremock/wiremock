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
package com.github.tomakehurst.wiremock.verification;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static com.github.tomakehurst.wiremock.verification.LoggedRequest.createFrom;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.hamcrest.Matchers;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(JMock.class)
public class LoggedRequestTest {
	
	private Mockery context;
	
	@Before
	public void init() {
		context = new Mockery();
	}

	@Test
	public void headerMatchingIsCaseInsensitive() {
		LoggedRequest loggedRequest = createFrom(aRequest(context)
				.withUrl("/for/logging")
				.withMethod(POST)
				.withBody("Actual Content")
				.withHeader("Content-Type", "text/plain")
				.withHeader("ACCEPT", "application/json")
				.build());
		
		assertTrue(loggedRequest.containsHeader("content-type"));
		assertNotNull(loggedRequest.getHeader("content-type"));
		assertTrue(loggedRequest.containsHeader("CONTENT-TYPE"));
		assertNotNull(loggedRequest.getHeader("CONTENT-TYPE"));
		assertTrue(loggedRequest.containsHeader("Accept"));
		assertNotNull(loggedRequest.getHeader("Accept"));
	}

    static  final String DATE = "2012-06-07 16:39:41";
    static final String JSON_EXAMPLE = "{\n" +
            "      \"url\" : \"/my/url\",\n" +
            "      \"absoluteUrl\" : \"http://mydomain.com/my/url\",\n" +
            "      \"method\" : \"GET\",\n" +
            "      \"headers\" : {\n" +
            "        \"Accept-Language\" : \"en-us,en;q=0.5\"\n" +
            "      },\n" +
            "      \"body\" : \"some text\",\n" +
            "      \"browserProxyRequest\" : true,\n" +
            "      \"loggedDate\" : 1339083581000,\n" +
            "      \"loggedDateString\" : \"" + DATE + "\"\n" +
            "    }";

    @Test
    public void jsonRepresentation() throws Exception {
        HttpHeaders headers = new HttpHeaders(httpHeader("Accept-Language", "en-us,en;q=0.5"));

        LoggedRequest loggedRequest = new LoggedRequest(
                "/my/url",
                "http://mydomain.com/my/url",
                RequestMethod.GET,
                headers,
                "some text",
                true,
                parse(DATE));

        assertThat(Json.write(loggedRequest), equalToIgnoringWhiteSpace(JSON_EXAMPLE));
    }

    private Date parse(String dateString) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return df.parse(dateString);
    }


}
