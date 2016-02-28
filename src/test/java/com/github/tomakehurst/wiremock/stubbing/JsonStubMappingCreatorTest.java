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

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JsonStubMappingCreatorTest {

	private static final String JSON_WITH_UUID =
					"{                                                                  \n" +
					"  \"uuid\" : \"82cefb9d-e2b3-4286-96ec-b33e855ece31\",             \n" +
					"  \"request\" : {                                                  \n" +
					"    \"url\" : \"/xml/content\",                                    \n" +
					"    \"method\" : \"POST\",                                         \n" +
					"    \"bodyPatterns\" : [ {                                         \n" +
					"      \"equalToXml\" : \"<stuff />\"                               \n" +
					"    } ]                                                            \n" +
					"  },                                                               \n" +
					"  \"response\" : {                                                 \n" +
					"    \"status\" : 200,                                              \n" +
					"    \"bodyFileName\" : \"body-xml-content-1$2!3.json\"             \n" +
					"  }                                                                \n" +
					"}";

	private static final String JSON_WITHOUT_UUID =
					"{                                                                  \n" +
					"  \"request\" : {                                                  \n" +
					"    \"url\" : \"/xml/content\",                                    \n" +
					"    \"method\" : \"POST\",                                         \n" +
					"    \"bodyPatterns\" : [ {                                         \n" +
					"      \"equalToXml\" : \"<stuff />\"                               \n" +
					"    } ]                                                            \n" +
					"  },                                                               \n" +
					"  \"response\" : {                                                 \n" +
					"    \"status\" : 200,                                              \n" +
					"    \"bodyFileName\" : \"body-xml-content-1$2!3.json\"             \n" +
					"  }                                                                \n" +
					"}";

	private final JsonStubMappingCreator jsonStubMappingCreator = new JsonStubMappingCreator();

	@Test
	public void testAddMappingFromJsonWithUuid() throws Exception {

		StubMapping stubMapping = jsonStubMappingCreator.createMappingFrom(JSON_WITH_UUID);
		assertThat(stubMapping, notNullValue());
		assertThat(stubMapping.getRequest(), notNullValue());
		assertThat(stubMapping.getResponse(), notNullValue());
		assertThat(stubMapping.getUuid(), notNullValue());
		assertThat(stubMapping.getUuid().toString(), is("82cefb9d-e2b3-4286-96ec-b33e855ece31"));
	}

	@Test
	public void testAddMappingFromJsonWithoutUuidGeneratesNewUuid() throws Exception {

		StubMapping stubMapping = jsonStubMappingCreator.createMappingFrom(JSON_WITHOUT_UUID);
		assertThat(stubMapping, notNullValue());
		assertThat(stubMapping.getRequest(), notNullValue());
		assertThat(stubMapping.getResponse(), notNullValue());
		assertThat(stubMapping.getUuid(), notNullValue());
		assertThat(stubMapping.getUuid().toString(), is(not("82cefb9d-e2b3-4286-96ec-b33e855ece31")));
	}
}