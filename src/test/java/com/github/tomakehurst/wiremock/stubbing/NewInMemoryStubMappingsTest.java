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
package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.*;
import com.github.tomakehurst.wiremock.verification.InMemoryRequestJournal;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NewInMemoryStubMappingsTest {

	NewInMemoryStubMappings inMemoryStubMappings;
    InMemoryRequestJournal requestJournal;

	@Before
	public void setUp() {
        requestJournal = new InMemoryRequestJournal(Optional.<Integer>absent());

		inMemoryStubMappings = new NewInMemoryStubMappings(
            Collections.<String, RequestMatcherExtension>emptyMap(),
            requestJournal,
            Collections.<String, ResponseDefinitionTransformer>emptyMap(),
            new SingleRootFileSource(".")
        );
	}

	@Test
    public void requestJournalEntriesContainsNearMissesWhenNoMatchFound() {
        StubMapping stub1 = stubMapping(new NewRequestPattern(
            UrlPattern.equalTo("/one"),
            GET,
            ImmutableMap.of("Header-One", MultiValuePattern.of(StringValuePattern.equalTo("h1-one")))
        ), "1");
        StubMapping stub2 = stubMapping(new NewRequestPattern(
            UrlPattern.equalTo("/two"),
            GET,
            ImmutableMap.of("Header-One", MultiValuePattern.of(StringValuePattern.equalTo("h1-one")))
        ), "2");
        StubMapping stub3 = stubMapping(new NewRequestPattern(
            UrlPattern.equalTo("/two"),
            GET,
            ImmutableMap.of("Header-One", MultiValuePattern.of(StringValuePattern.equalTo("h1-two")))
        ), "3");
        StubMapping stub4 = stubMapping(new NewRequestPattern(
            UrlPattern.equalTo("/four"),
            GET,
            ImmutableMap.of("Header-One", MultiValuePattern.of(StringValuePattern.equalTo("nonononon")))
        ), "4");
        inMemoryStubMappings.addMapping(stub2);
        inMemoryStubMappings.addMapping(stub1);
        inMemoryStubMappings.addMapping(stub3);
        inMemoryStubMappings.addMapping(stub4);

        ServedStub servedStub = inMemoryStubMappings.serveFor(mockRequest()
            .url("/onn")
            .method(GET)
            .header("Header-One", "h1-one"));

        assertTrue(servedStub.isNoExactMatch());

        ServedStub journaledServedStub = requestJournal.getAllServedStubs().get(0);

        assertThat(journaledServedStub.getNearMisses().size(), is(3));
        assertThat(journaledServedStub.getNearMisses().get(0).getStubMapping(), is(stub1));
        assertThat(journaledServedStub.getNearMisses().get(1).getStubMapping(), is(stub2));
        assertThat(journaledServedStub.getNearMisses().get(2).getStubMapping(), is(stub3));
    }

    @Test
    public void requestJournalEntryContainsNoNearMissesWhenExactMatchFound() {
        StubMapping stub1 = stubMapping(new NewRequestPattern(
            UrlPattern.equalTo("/one"),
            GET,
            ImmutableMap.of("Header-One", MultiValuePattern.of(StringValuePattern.equalTo("h1-one")))
        ), "1");
        StubMapping stub2 = stubMapping(new NewRequestPattern(
            UrlPattern.equalTo("/two"),
            GET,
            ImmutableMap.of("Header-One", MultiValuePattern.of(StringValuePattern.equalTo("h1-one")))
        ), "2");
        StubMapping stub3 = stubMapping(new NewRequestPattern(
            UrlPattern.equalTo("/two"),
            GET,
            ImmutableMap.of("Header-One", MultiValuePattern.of(StringValuePattern.equalTo("h1-two")))
        ), "3");
        StubMapping stub4 = stubMapping(new NewRequestPattern(
            UrlPattern.equalTo("/four"),
            GET,
            ImmutableMap.of("Header-One", MultiValuePattern.of(StringValuePattern.equalTo("nonononon")))
        ), "4");
        inMemoryStubMappings.addMapping(stub2);
        inMemoryStubMappings.addMapping(stub1);
        inMemoryStubMappings.addMapping(stub3);
        inMemoryStubMappings.addMapping(stub4);

        inMemoryStubMappings.serveFor(mockRequest()
            .url("/one")
            .method(GET)
            .header("Header-One", "h1-one"));

        assertThat(requestJournal.getAllServedStubs().get(0).getNearMisses(), nullValue());
    }

    @Test
    public void requestJournalContainsEmptyNearMissesCollectionWhenNoStubsWerePresent() {
        inMemoryStubMappings.serveFor(mockRequest()
            .url("/one")
            .method(GET)
            .header("Header-One", "h1-one"));

        ServedStub servedStub = requestJournal.getAllServedStubs().get(0);
        assertThat(servedStub.getRequest().getUrl(), is("/one"));
        assertThat(servedStub.getNearMisses(), is(Collections.<NearMiss>emptyList()));
    }

    private StubMapping stubMapping(NewRequestPattern requestPattern, String body) {
        StubMapping stubMapping = new StubMapping();
        stubMapping.setNewRequest(requestPattern);
        stubMapping.setResponse(new ResponseDefinition(200, body));
        return stubMapping;
    }

    @Test
	public void testEditMapping() throws Exception {

		StubMapping existingMapping = aMapping(1, "/priority1/1");
		inMemoryStubMappings.addMapping(existingMapping);

		StubMapping newMapping = aMapping(1, "/priority1/2");
		newMapping.setUuid(existingMapping.getUuid());

		inMemoryStubMappings.editMapping(newMapping);

		List<StubMapping> allMappings = inMemoryStubMappings.getAll();

		assertThat(allMappings, hasSize(1));
		assertThat(allMappings.get(0), is(newMapping));
		assertThat(newMapping.getInsertionIndex(), is(existingMapping.getInsertionIndex()));
	}

	@Test
	public void testEditMappingNotPresent() throws Exception {

		StubMapping existingMapping = aMapping(1, "/priority1/1");
		inMemoryStubMappings.addMapping(existingMapping);

		StubMapping newMapping = aMapping(1, "/priority1/2");

		try {
			inMemoryStubMappings.editMapping(newMapping);
			fail("Expected Exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage(), containsString(newMapping.getUuid().toString()));
		}
	}

	private StubMapping aMapping(Integer priority, String url) {
		RequestPattern requestPattern = new RequestPattern(ANY, url);
		StubMapping mapping = new StubMapping(requestPattern, new ResponseDefinition());
		mapping.setPriority(priority);
		return mapping;
	}
}