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
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.ValuePattern;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.ValueMatchingStrategy.toValuePattern;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class RequestPatternBuilder {

	private RequestMethod method;
	private UrlMatchingStrategy urlMatchingStrategy;
	private Map<String, ValueMatchingStrategy> headers = newLinkedHashMap();
    private Map<String, ValueMatchingStrategy> queryParameters = newLinkedHashMap();
    private Set<String> withoutHeaders = newHashSet();
	private List<ValueMatchingStrategy> bodyPatterns = newArrayList();
	
	public RequestPatternBuilder(RequestMethod method,
			UrlMatchingStrategy urlMatchingStrategy) {
		this.method = method;
		this.urlMatchingStrategy = urlMatchingStrategy;
	}
	
	public RequestPatternBuilder withHeader(String key, ValueMatchingStrategy headerMatchingStrategy) {
		headers.put(key, headerMatchingStrategy);
		return this;
	}

    public RequestPatternBuilder withQueryParam(String key, ValueMatchingStrategy queryParamMatchingStrategy) {
        queryParameters.put(key, queryParamMatchingStrategy);
        return this;
    }

    public RequestPatternBuilder withoutHeader(String key) {
        withoutHeaders.add(key);
        return this;
    }

	public RequestPatternBuilder withRequestBody(ValueMatchingStrategy bodyMatchingStrategy) {
		bodyPatterns.add(bodyMatchingStrategy);
		return this;
	}

    public static RequestPatternBuilder allRequests() {
        UrlMatchingStrategy matchAllUrls = new UrlMatchingStrategy();
        matchAllUrls.setUrlPattern(".*");
        return new RequestPatternBuilder(RequestMethod.ANY, matchAllUrls);
    }

	public RequestPattern build() {
		RequestPattern requestPattern = new RequestPattern();
		requestPattern.setMethod(method);
		urlMatchingStrategy.contributeTo(requestPattern);
		for (Map.Entry<String, ValueMatchingStrategy> header: headers.entrySet()) {
			requestPattern.addHeader(header.getKey(), header.getValue().asValuePattern());
		}

        for (String key: withoutHeaders) {
            requestPattern.addHeader(key, ValuePattern.absent());
        }

        for (Map.Entry<String, ValueMatchingStrategy> queryParam: queryParameters.entrySet()) {
            requestPattern.addQueryParam(queryParam.getKey(), queryParam.getValue().asValuePattern());
        }

		if (!bodyPatterns.isEmpty()) {
			requestPattern.setBodyPatterns(newArrayList(transform(bodyPatterns, toValuePattern)));
		}

		return requestPattern;
	}


}
