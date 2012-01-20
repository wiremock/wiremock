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

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.mapping.RequestPattern;
import com.github.tomakehurst.wiremock.mapping.RequestResponseMapping;
import com.github.tomakehurst.wiremock.mapping.ResponseDefinition;

public class MappingBuilder {
	
	private RequestPatternBuilder requestPatternBuilder;
	private ResponseDefinitionBuilder responseDefBuilder;
	private Integer priority;
	
	public MappingBuilder(RequestMethod method, UrlMatchingStrategy urlMatchingStrategy) {
		requestPatternBuilder = new RequestPatternBuilder(method, urlMatchingStrategy);
	}

	public MappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder) {
		this.responseDefBuilder = responseDefBuilder;
		return this;
	}
	
	public MappingBuilder atPriority(Integer priority) {
		this.priority = priority;
		return this;
	}
	
	public MappingBuilder withHeader(String key, HeaderMatchingStrategy headerMatchingStrategy) {
		requestPatternBuilder.withHeader(key, headerMatchingStrategy);
		return this;
	}
	
	public MappingBuilder withBodyMatching(String bodyPattern) {
	    requestPatternBuilder.withBodyMatching(bodyPattern);
        return this;
    }
	
	public MappingBuilder withBodyContaining(String bodyPattern) {
        requestPatternBuilder.withBodyContaining(bodyPattern);
        return this;
    }
	
	public RequestResponseMapping build() {
		RequestPattern requestPattern = requestPatternBuilder.build();
		ResponseDefinition response = responseDefBuilder.build();
		RequestResponseMapping mapping = new RequestResponseMapping(requestPattern, response);
		mapping.setPriority(priority);
		return mapping;
	}
}
