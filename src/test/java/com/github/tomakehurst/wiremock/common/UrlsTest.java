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
package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.http.FormParameter;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import org.junit.Test;

import java.net.URI;
import java.util.Map;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UrlsTest {

    private Map<String, QueryParameter> qParams;
	private Map<String, FormParameter> fParams;

    @Test
    public void splitQueryCopesWithEqualsInParamValues() {
        qParams = Urls.splitQuery(URI.create("/thing?param1=one&param2=one==two=three"));
        assertThat(qParams.get("param1").firstValue(), is("one"));
        assertThat(qParams.get("param2").firstValue(), is("one==two=three"));
    }

    @Test
    public void splitQueryReturnsEmptyStringsAsValuesWhenOnlyKeysArePresent() {
        qParams = Urls.splitQuery(URI.create("/thing?param1&param2&param3"));
        assertThat(qParams.get("param1").firstValue(), is(""));
        assertThat(qParams.get("param2").firstValue(), is(""));
        assertThat(qParams.get("param3").firstValue(), is(""));
    }

    @Test
    public void splitQuerySupportsMultiValuedParameters() {
        qParams = Urls.splitQuery(URI.create("/thing?param1=1&param2=two&param1=2&param1=3"));
        assertThat(qParams.size(), is(2));
        assertThat(qParams.get("param1").isSingleValued(), is(false));
        assertThat(qParams.get("param1").values(), hasItems("1", "2", "3"));
    }

	@Test
	public void splitFormSupportsMultiValuesParameters() {
		fParams = Urls.splitForm("param1=1&param1=2");
		assertThat(fParams.size(), is(1));
		assertThat(fParams.get("param1").isSingleValued(), is(false));
		assertThat(fParams.get("param1").values(), hasItems("1", "2"));
	}

	@Test
	public void splitFormReturnsEmptyStringsAsValuesWhenOnlyKeysArePresent() {
		fParams = Urls.splitForm("param1&param2&param3");
		assertThat(fParams.size(), is(3));
		assertThat(fParams.get("param1").firstValue(), is(""));
		assertThat(fParams.get("param2").firstValue(), is(""));
		assertThat(fParams.get("param3").firstValue(), is(""));
	}
}
