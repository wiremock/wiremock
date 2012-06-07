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
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.mapping.Request;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RequestQueryAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void returnsRecordedRequestsMatchingOnMethodAndExactUrl() {
        testClient.get("/return/this");
        testClient.get("/but/not/this");
        testClient.get("/return/this");
        testClient.get("/return/this");
        testClient.get("/but/not/this");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlEqualTo("/return/this")));

        assertThat(requests.size(), is(3));

        Request firstRequest = requests.get(0);
        assertThat(firstRequest.getUrl(), is("/return/this"));
        assertThat(firstRequest.getMethod(), is(RequestMethod.GET));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void returnsRecordedRequestsMatchingOnMethodAndUrlRegex() {
        testClient.put("/should/return/this/request");
        testClient.get("/donot/return/this/request"); //Wrong method
        testClient.put("/also/return/this");
        testClient.put("/not/this");

        List<LoggedRequest> requests = findAll(putRequestedFor(urlMatching(".*return/this.*")));

        assertThat(requests.size(), is(2));
        assertThat(requests, hasItems(withUrl("/should/return/this/request"), withUrl("/also/return/this")));
    }

    private Matcher<LoggedRequest> withUrl(final String url) {
        return new TypeSafeMatcher<LoggedRequest>() {
            @Override
            public boolean matchesSafely(LoggedRequest loggedRequest) {
                return loggedRequest.getUrl().equals(url);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("A logged request with url: " + url);
            }
        };
    }

    @Test
    public void returnsNoResultsAfterReset() {
        testClient.get("/blah");
        testClient.get("/blah");
        testClient.get("/blah");

        WireMock.reset();
        List<LoggedRequest> requests = findAll(getRequestedFor(urlEqualTo("/blah")));

        assertThat(requests.size(), is(0));
    }
}
