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
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.stubbing.InMemoryStubMappings;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JsonFileMappingsLoaderTest {

	private static final String SAMPLE_REQUEST_MAPPING =
			"{ 													             \n" +
			"	\"request\": {									             \n" +
			"		\"method\": \"GET\",						             \n" +
			"		\"url\": \"/recorded/content\"				             \n" +
			"	},												             \n" +
			"	\"response\": {									             \n" +
			"		\"status\": 200,							             \n" +
			"		\"bodyFileName\": \"body-recorded-content-1$2!3.json\"   \n" +
			"	}												             \n" +
			"}													               ";

	private Mockery context;

	private FileSource mappingsFileSource;
	private JsonFileMappingsLoader jsonFileMappingsLoader;

	@Before
	public void setUp() throws Exception {
		context = new Mockery();
		mappingsFileSource = context.mock(FileSource.class, "mappingFileSource");
		jsonFileMappingsLoader = new JsonFileMappingsLoader(mappingsFileSource);
	}

	@Test
	public void testLoadMappings() throws Exception {

		final StubMappings stubMappings = new InMemoryStubMappings();

		final URI fileUri = this.getClass().getResource("/mappings/testmapping.json").toURI();
		final TextFile textFile = new TextFile(fileUri);

		final String mappingFileName = "testmapping.json";
		final String mappingFileSourcePath = StringUtils.removeEnd(fileUri.getPath(), "/" + mappingFileName);


		context.checking(new Expectations() {{
			one(mappingsFileSource).listFilesRecursively();
			will(returnValue(Lists.newArrayList(textFile)));
			one(mappingsFileSource).getPath();
			will(returnValue(mappingFileSourcePath));
		}});

		jsonFileMappingsLoader.loadMappingsInto(stubMappings);

		assertThat(stubMappings.getAll(), hasSize(1));
	}
}