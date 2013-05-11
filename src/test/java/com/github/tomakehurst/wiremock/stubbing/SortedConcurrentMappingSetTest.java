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

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.hasExactly;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SortedConcurrentMappingSetTest {
	
	private SortedConcurrentMappingSet mappingSet;
	
	@Before
	public void init() {
		mappingSet = new SortedConcurrentMappingSet();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void returnsMappingsInPriorityThenInsertionOrder() {
		mappingSet.add(aMapping(3, "/priority3/1"));
		mappingSet.add(aMapping(3, "/priority3/2"));
		mappingSet.add(aMapping(6, "/priority6/1"));
		mappingSet.add(aMapping(1, "/priority1/1"));
		mappingSet.add(aMapping(1, "/priority1/2"));
		mappingSet.add(aMapping(1, "/priority1/3"));
		
		assertThat(mappingSet, hasExactly(
				requestUrlIs("/priority1/3"),
				requestUrlIs("/priority1/2"),
				requestUrlIs("/priority1/1"),
				requestUrlIs("/priority3/2"),
				requestUrlIs("/priority3/1"),
				requestUrlIs("/priority6/1")));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void supportsNullPriority() {
		mappingSet.add(aMapping(null, "/1"));
		mappingSet.add(aMapping(null, "/2"));
		mappingSet.add(aMapping(null, "/3"));
		mappingSet.add(aMapping(null, "/4"));
		
		assertThat(mappingSet, hasExactly(
				requestUrlIs("/4"),
				requestUrlIs("/3"),
				requestUrlIs("/2"),
				requestUrlIs("/1")));
	}
	
	@Test
	public void clearsCorrectly() {
		mappingSet.add(aMapping(3, "/priority3/1"));
		mappingSet.add(aMapping(3, "/priority3/2"));
		mappingSet.add(aMapping(6, "/priority6/1"));
		mappingSet.add(aMapping(1, "/priority1/1"));
		
		mappingSet.clear();
		
		assertThat("Mapping set should be empty", mappingSet.iterator().hasNext(), is(false));
	}
	
	private StubMapping aMapping(Integer priority, String url) {
		RequestPattern requestPattern = new RequestPattern(ANY, url);
		StubMapping mapping = new StubMapping(requestPattern, new ResponseDefinition());
		mapping.setPriority(priority);
		return mapping;
	}
	
	private Matcher<StubMapping> requestUrlIs(final String expectedUrl) {
		return new TypeSafeMatcher<StubMapping>() {

			@Override
			public void describeTo(Description desc) {
			}

			@Override
			public boolean matchesSafely(StubMapping actualMapping) {
				return actualMapping.getRequest().getUrl().equals(expectedUrl);
			}
			
		};
	}
}
