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

import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public class InvocationMappingBuilderTest {

	@Test
	public void canRecordGet() {
		final StubMapping actual = new InvocationMappingBuilder<>(TestResouce.class, (r) -> r.getStuff()).build();

		assertThat(actual.getRequest().getMethod(), equalTo(GET));
	}

	@Test
	public void canRecordPost() {
		final TestResourceDTO data = new TestResourceDTO("some value");
		final StubMapping actual = new InvocationMappingBuilder<>(TestResouce.class, (r) -> r.postStuff(data)).build();

		assertThat(actual.getRequest().getMethod(), equalTo(POST));
	}
}
