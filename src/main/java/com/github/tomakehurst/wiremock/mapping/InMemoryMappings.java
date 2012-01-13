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
package com.github.tomakehurst.wiremock.mapping;

import static com.github.tomakehurst.wiremock.mapping.ResponseDefinition.copyOf;
import static com.google.common.collect.Iterables.find;

import com.google.common.base.Predicate;


public class InMemoryMappings implements Mappings {
	
	private final SortedConcurrentMappingSet mappings = new SortedConcurrentMappingSet();
	
	@Override
	public ResponseDefinition getFor(Request request) {
		RequestResponseMapping matchingMapping = find(
				mappings,
				mappingMatching(request),
				RequestResponseMapping.notConfigured());
		return copyOf(matchingMapping.getResponse());
	}
	
	private Predicate<RequestResponseMapping> mappingMatching(final Request request) {
		return new Predicate<RequestResponseMapping>() {
			public boolean apply(RequestResponseMapping input) {
				return input.getRequest().isMatchedBy(request);
			}
		};
	}

	@Override
	public void addMapping(RequestResponseMapping mapping) {
		mappings.add(mapping);
	}

	@Override
	public void reset() {
		mappings.clear();
	}

}
