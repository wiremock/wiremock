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

import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.stubbing.InMemoryStubMappings;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.google.common.collect.Lists;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JsonFileMappingsSourceTest {

	private Mockery context;

	private FileSource mappingsFileSource;
	private JsonFileMappingsSource jsonFileMappingsSource;

	@Before
	public void setUp() throws Exception {
		context = new Mockery();
		mappingsFileSource = context.mock(FileSource.class, "mappingFileSource");
		jsonFileMappingsSource = new JsonFileMappingsSource(mappingsFileSource);
	}

	@Test
	public void loadsMappingsFromFile() throws Exception {

		final StubMappings stubMappings = new InMemoryStubMappings();

		final String mappingFileName = "testmapping.json";
		final URI fileUri = this.getClass().getResource("/mappings/" + mappingFileName).toURI();
		final TextFile textFile = new TextFile(fileUri);

		final String mappingFileSource = fileUri.getPath().replace("/" + mappingFileName, "");


		context.checking(new Expectations() {{
			atLeast(1).of(mappingsFileSource).exists();
			will(returnValue(true));
			oneOf(mappingsFileSource).listFilesRecursively();
			will(returnValue(Lists.newArrayList(textFile)));
			oneOf(mappingsFileSource).getUri();
			will(returnValue(URI.create(mappingFileSource)));
		}});

		jsonFileMappingsSource.loadMappingsInto(stubMappings);

		assertThat(stubMappings.getAll(), hasSize(1));
	}

	@Test
	public void savesMappingToFile() throws Exception {

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

		jsonFileMappingsSource.saveMappings(stubMappings);
		assertThat(stubMapping.isTransient(), is(false));
	}

	@Test
	public void savesEditedMappingToTheFileItOriginatedFrom() throws Exception {

		final StubMappings stubMappings = new InMemoryStubMappings();

		final String mappingFileName = "testmapping.json";
		final URI fileUri = this.getClass().getResource("/mappings/" + mappingFileName).toURI();
		final TextFile textFile = new TextFile(fileUri);

		final String mappingFileSource = fileUri.getPath().replace(mappingFileName, "");

		context.checking(new Expectations() {{
			atLeast(1).of(mappingsFileSource).exists();
			will(returnValue(true));
			oneOf(mappingsFileSource).listFilesRecursively();
			will(returnValue(Lists.newArrayList(textFile)));
			oneOf(mappingsFileSource).getUri();
			will(returnValue(URI.create(mappingFileSource)));
		}});

		jsonFileMappingsSource.loadMappingsInto(stubMappings);

		final StubMapping stubMapping = stubMappings.getAll().get(0);
		stubMapping.setTransient(true);

		context.checking(new Expectations() {{
			one(mappingsFileSource).writeTextFile(
					with(mappingFileName),
					with(Json.write(stubMapping))
			);
		}});

		jsonFileMappingsSource.saveMappings(stubMappings);
		assertThat(stubMapping.isTransient(), is(false));
	}

	@Test
	public void loadsMappingsViaClasspathFileSource() {
		ClasspathFileSource fileSource = new ClasspathFileSource("jar-filesource");
		JsonFileMappingsSource source = new JsonFileMappingsSource(fileSource);
		StubMappings stubMappings = new InMemoryStubMappings();

		source.loadMappingsInto(stubMappings);

		assertThat(stubMappings.getAll().get(0).getRequest().getUrl(), is("/test"));
	}
}