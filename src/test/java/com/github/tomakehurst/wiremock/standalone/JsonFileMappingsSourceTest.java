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
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.stubbing.InMemoryStubMappings;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.google.common.base.Charsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JsonFileMappingsSourceTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

	@Test
	public void loadsMappingsViaClasspathFileSource() {
		ClasspathFileSource fileSource = new ClasspathFileSource("jar-filesource");
		JsonFileMappingsSource source = new JsonFileMappingsSource(fileSource);
		StubMappings stubMappings = new InMemoryStubMappings();

		source.loadMappingsInto(stubMappings);

		List<StubMapping> allMappings = stubMappings.getAll();
		assertThat(allMappings, hasSize(2));

		List<String> mappingRequestUrls = asList(
			allMappings.get(0).getRequest().getUrl(),
			allMappings.get(1).getRequest().getUrl()
		);
		assertThat(mappingRequestUrls, is(asList("/second_test", "/test")));
	}

	@Test
	public void stubMappingFilesAreWrittenWithInsertionIndex() throws Exception {
	    JsonFileMappingsSource source = new JsonFileMappingsSource(new SingleRootFileSource(tempDir.getRoot()));

        StubMapping stub = get("/saveable").willReturn(ok()).build();
        source.save(stub);

        File savedFile = tempDir.getRoot().listFiles()[0];
        String savedStub = Files.toString(savedFile, UTF_8);

        assertThat(savedStub, containsString("\"insertionIndex\" : 0"));
    }


}