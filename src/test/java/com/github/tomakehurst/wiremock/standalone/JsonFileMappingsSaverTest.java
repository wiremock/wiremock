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
package com.github.tomakehurst.wiremock.standalone;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.stubbing.InMemoryStubMappings;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class JsonFileMappingsSaverTest {

	private Mockery context;

	private FileSource mappingsFileSource;
	private JsonFileMappingsSaver jsonFileMappingsSaver;

	@Before
	public void setUp() throws Exception {
		context = new Mockery();
		mappingsFileSource = context.mock(FileSource.class, "mappingsFileSource");
		jsonFileMappingsSaver = new JsonFileMappingsSaver(mappingsFileSource);
	}

	@Test
	public void testSaveMappings() throws Exception {

		final StubMapping stubMapping = new StubMapping();
		stubMapping.setTransient(true);

		final StubMappings stubMappings = new InMemoryStubMappings();
		stubMappings.addMapping(stubMapping);

		context.checking(new Expectations() {{
			one(mappingsFileSource).writeTextFile(
					with(allOf(startsWith("saved-mapping-"), endsWith(".json"))),
					with(Json.write(stubMapping))
			);
		}});

		jsonFileMappingsSaver.saveMappings(stubMappings);
		assertThat(stubMapping.isTransient(), is(false));
	}
}