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

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServedStub;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.DELETE;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.verification.NearMissCalculator.NEAR_MISS_COUNT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NearMissCalculatorTest {

    private Mockery context;

    NearMissCalculator nearMissCalculator;

    StubMappings stubMappings;
    RequestJournal requestJournal;

    @Before
    public void init() {
        context = new Mockery();

        stubMappings = context.mock(StubMappings.class);
        requestJournal = context.mock(RequestJournal.class);
        nearMissCalculator = new NearMissCalculator(stubMappings, requestJournal);
    }

    @Test
    public void returnsNearest3MissesForSingleRequest() {
        context.checking(new Expectations() {{
            one(stubMappings).getAll(); will(returnValue(
                asList(
                    get(urlEqualTo("/righ")).willReturn(aResponse()).build(),
                    get(urlEqualTo("/totally-wrong1")).willReturn(aResponse()).build(),
                    get(urlEqualTo("/totally-wrong222")).willReturn(aResponse()).build(),
                    get(urlEqualTo("/almost-right")).willReturn(aResponse()).build(),
                    get(urlEqualTo("/rig")).willReturn(aResponse()).build(),
                    get(urlEqualTo("/totally-wrong33333")).willReturn(aResponse()).build()
                )
            ));
        }});

        List<NearMiss> nearest = nearMissCalculator.findNearestTo(mockRequest().url("/right").asLoggedRequest());

        assertThat(nearest.size(), is(NEAR_MISS_COUNT));
        assertThat(nearest.get(0).getStubMapping().getRequest().getUrl(), is("/righ"));
        assertThat(nearest.get(1).getStubMapping().getRequest().getUrl(), is("/rig"));
        assertThat(nearest.get(2).getStubMapping().getRequest().getUrl(), is("/almost-right"));
    }

    @Test
    public void returns0NearMissesForSingleRequestWhenNoStubsPresent() {
        context.checking(new Expectations() {{
            one(stubMappings).getAll(); will(returnValue(emptyList()));
        }});

        List<NearMiss> nearest = nearMissCalculator.findNearestTo(mockRequest().url("/right").asLoggedRequest());

        assertThat(nearest.size(), is(0));
    }

    @Test
    public void returns3NearestMissesForTheGivenRequestPattern() {
        context.checking(new Expectations() {{
            one(requestJournal).getAllServedStubs();
            will(returnValue(
                asList(
                    new ServedStub(LoggedRequest.createFrom(mockRequest().method(DELETE).url("/rig")), new ResponseDefinition()),
                    new ServedStub(LoggedRequest.createFrom(mockRequest().method(DELETE).url("/righ")), new ResponseDefinition()),
                    new ServedStub(LoggedRequest.createFrom(mockRequest().method(DELETE).url("/almost-right")), new ResponseDefinition()),
                    new ServedStub(LoggedRequest.createFrom(mockRequest().method(POST).url("/almost-right")), new ResponseDefinition())
                )
            ));
        }});

        List<NearMiss> nearest = nearMissCalculator.findNearestTo(
            newRequestPattern(DELETE, urlEqualTo("/right")).build()
        );

        assertThat(nearest.size(), is(NEAR_MISS_COUNT));
        assertThat(nearest.get(0).getRequest().getUrl(), is("/righ"));
        assertThat(nearest.get(1).getRequest().getUrl(), is("/rig"));
        assertThat(nearest.get(2).getRequest().getUrl(), is("/almost-right"));
        assertThat(nearest.get(2).getRequest().getMethod(), is(DELETE));
    }

    @Test
    public void returns1NearestMissForTheGivenRequestPatternWhenOnlyOneRequestLogged() {
        context.checking(new Expectations() {{
            one(requestJournal).getAllServedStubs();
            will(returnValue(
                singletonList(
                    new ServedStub(
                        LoggedRequest.createFrom(mockRequest().method(DELETE).url("/righ")),
                        new ResponseDefinition()
                    )
                )
            ));
        }});

        List<NearMiss> nearest = nearMissCalculator.findNearestTo(
            newRequestPattern(DELETE, urlEqualTo("/right")).build()
        );

        assertThat(nearest.size(), is(1));
        assertThat(nearest.get(0).getRequest().getUrl(), is("/righ"));
    }

    @Test
    public void returns0NearMissesForSingleRequestPatternWhenNoRequestsLogged() {
        context.checking(new Expectations() {{
            one(requestJournal).getAllServedStubs();
            will(returnValue(emptyList()));
        }});

        List<NearMiss> nearest = nearMissCalculator.findNearestTo(
            newRequestPattern(DELETE, urlEqualTo("/right")).build()
        );

        assertThat(nearest.size(), is(0));
    }
}
