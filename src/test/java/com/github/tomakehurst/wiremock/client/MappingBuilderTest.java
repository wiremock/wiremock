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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.matching.ValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.header;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MappingBuilderTest {

	@Test
	public void shouldBuildMappingWithExactUrlNoHeaders() {
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrl("/match/this");
		StubMapping mapping =
			new MappingBuilder(POST, urlStrategy)
			.willReturn(new ResponseDefinitionBuilder().withStatus(201))
			.build();
		
		assertThat(mapping.getRequest().getUrl(), is("/match/this"));
		assertThat(mapping.getRequest().getMethod(), is(POST));
		assertThat(mapping.getResponse().getStatus(), is(201));
	}
	
	@Test
	public void shouldBuildMappingWithMatchedUrlNoHeaders() {
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrlPattern("/match/[A-Z]{5}");
		StubMapping mapping =
			new MappingBuilder(POST, urlStrategy)
			.willReturn(new ResponseDefinitionBuilder())
			.build();
		
		assertThat(mapping.getRequest().getUrlPattern(), is("/match/[A-Z]{5}"));
	}
	
	@Test
	public void shouldBuildMappingWithExactUrlAndRequestHeaders() {
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrl("/match/this");
		
		StubMapping mapping =
			new MappingBuilder(POST, urlStrategy)
			.withHeader("Content-Type", headerStrategyEqualTo("text/plain"))
			.withHeader("Encoding", headerStrategyMatches("UTF-\\d"))
			.withHeader("X-My-Thing", headerStrategyDoesNotMatch("[A-Z]+"))
			.willReturn(new ResponseDefinitionBuilder())
			.build();
		
		assertThat(mapping.getRequest().getHeaders(), hasEntry("Content-Type", headerEqualTo("text/plain")));
		assertThat(mapping.getRequest().getHeaders(), hasEntry("Encoding", headerMatches("UTF-\\d")));
		assertThat(mapping.getRequest().getHeaders(), hasEntry("X-My-Thing", headerDoesNotMatch("[A-Z]+")));
	}
	
	@Test
	public void shouldBuildMappingWithResponseBody() {
		StubMapping mapping =
			new MappingBuilder(POST, new UrlMatchingStrategy())
			.willReturn(new ResponseDefinitionBuilder().withBody("Some content"))
			.build();
		
		assertThat(mapping.getResponse().getBody(), is("Some content"));
	}

    @Test
    public void shouldBuildMappingWithResponseByteBody() {
        StubMapping mapping =
                new MappingBuilder(POST, new UrlMatchingStrategy())
                        .willReturn(new ResponseDefinitionBuilder().withBody("Some content".getBytes()))
                        .build();

        assertThat(mapping.getResponse().getByteBody(), is("Some content".getBytes()));
    }

    @SuppressWarnings("unchecked")
	@Test
	public void shouldBuildMappingWithResponseHeaders() {
		StubMapping mapping =
			new MappingBuilder(POST, new UrlMatchingStrategy())
			.willReturn(new ResponseDefinitionBuilder()
				.withHeader("Content-Type", "text/xml")
				.withHeader("Encoding", "UTF-8"))
			.build();
		
		assertThat(mapping.getResponse().getHeaders().all(), hasItems(
                header("Content-Type", "text/xml"),
                header("Encoding", "UTF-8")));
	}

	private ValuePattern headerEqualTo(String value) {
		ValuePattern headerPattern = new ValuePattern();
		headerPattern.setEqualTo(value);
		return headerPattern;
	}
	
	private ValuePattern headerMatches(String value) {
		ValuePattern headerPattern = new ValuePattern();
		headerPattern.setMatches(value);
		return headerPattern;
	}
	
	private ValuePattern headerDoesNotMatch(String value) {
		ValuePattern headerPattern = new ValuePattern();
		headerPattern.setDoesNotMatch(value);
		return headerPattern;
	}
	
	private ValueMatchingStrategy headerStrategyEqualTo(String value) {
		ValueMatchingStrategy headerStrategy = new ValueMatchingStrategy();
		headerStrategy.setEqualTo(value);
		return headerStrategy;
	}
	
	private ValueMatchingStrategy headerStrategyMatches(String value) {
		ValueMatchingStrategy headerStrategy = new ValueMatchingStrategy();
		headerStrategy.setMatches(value);
		return headerStrategy;
	}
	
	private ValueMatchingStrategy headerStrategyDoesNotMatch(String value) {
		ValueMatchingStrategy headerStrategy = new ValueMatchingStrategy();
		headerStrategy.setDoesNotMatch(value);
		return headerStrategy;
	}
}
