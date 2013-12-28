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
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.commons.io.FileUtils;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SavingMappingsAcceptanceTest extends AcceptanceTestBase {
    private static final File FILE_SOURCE_ROOT = new File("build/save-mappings-files");
    private static final File FILES_DIRECTORY = new File(FILE_SOURCE_ROOT, "__files");
    private static final File MAPPINGS_DIRECTORY = new File(FILE_SOURCE_ROOT, "mappings");

    private static void resetFileSourceRoot() {
        try {
            if (FILE_SOURCE_ROOT.exists()) {
                FileUtils.deleteDirectory(FILE_SOURCE_ROOT);
            }
            if (!FILES_DIRECTORY.mkdirs()) {
                throw new Exception("Could no create " + FILES_DIRECTORY.getAbsolutePath());
            }
            if (!MAPPINGS_DIRECTORY.mkdirs()) {
                throw new Exception("Could no create " + MAPPINGS_DIRECTORY.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public static void setupServer() {
        resetFileSourceRoot();
        setupServer(wireMockConfig().fileSource(new SingleRootFileSource(FILE_SOURCE_ROOT)));
    }

    @Before
    public void setUp() throws Exception {
        resetFileSourceRoot();
        reset();
    }

    @Test
    public void savesMappingsToMappingsDirectory() {
        // Check the mapping we're about to add isn't already there
        WireMockResponse response = testClient.get("/some/url");
        assertThat(response.statusCode(), is(404));

        // Add a mapping and save it
        stubFor(get(urlEqualTo("/some/url"))
                .willReturn(aResponse().withBody("Response to /some/url")));
        saveAllMappings();

        // Reset, clearing in-memory mappings
        resetToDefault();

        // Check the mapping now exists
        response = testClient.get("/some/url");
        assertThat(response.statusCode(), is(200));
        assertThat(response.content(), is("Response to /some/url"));
    }

    @Test
    public void doesNotDuplicateMappingsAlreadyPersistedToFileSystem() {
        // Check the mapping we're about to add isn't already there
        WireMockResponse response = testClient.get("/some/url");
        assertThat(response.statusCode(), is(404));

        // Add a mapping and save it
        stubFor(get(urlEqualTo("/some/url"))
                .willReturn(aResponse().withBody("Response to /some/url")));
        saveAllMappings();

        // Save a second time
        saveAllMappings();

        // Check only one file has been written
        assertThat(MAPPINGS_DIRECTORY.listFiles().length, is(1));
    }

    @Test
    public void doesNotDuplicateMappingsAlreadyPersistedAfterReset() {
        // Check the mapping we're about to add isn't already there
        WireMockResponse response = testClient.get("/some/url");
        assertThat(response.statusCode(), is(404));

        // Add a mapping and save it
        stubFor(get(urlEqualTo("/some/url"))
                .willReturn(aResponse().withBody("Response to /some/url")));
        saveAllMappings();

        // Reset to default to reload the just saved mappings, then save a second time
        resetToDefault();
        saveAllMappings();

        // Check only one file has been written
        assertThat(MAPPINGS_DIRECTORY.listFiles().length, is(1));
    }
}
