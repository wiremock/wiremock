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
package com.github.tomakehurst.wiremock.recording;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SnapshotStubMappingBodyExtractorTest {
  private FileSource filesSource;
  private SnapshotStubMappingBodyExtractor bodyExtractor;

  @BeforeEach
  public void init() {
    filesSource = Mockito.mock(FileSource.class, "filesFileSource");
    bodyExtractor = new SnapshotStubMappingBodyExtractor(filesSource);
  }

  @Test
  public void updatesStubMapping() {
    StubMapping stubMapping = WireMock.get("/foo").willReturn(ok("")).build();
    bodyExtractor.extractInPlace(stubMapping);
    assertThat(
        stubMapping.getResponse().getBodyFileName(), is("foo-" + stubMapping.getId() + ".txt"));
    assertThat(stubMapping.getResponse().specifiesBodyFile(), is(true));
    assertThat(stubMapping.getResponse().specifiesBodyContent(), is(false));
    // ignore arguments because this test is only for checking stub mapping changes
    verify(filesSource).writeBinaryFile(any(String.class), any(byte[].class));
  }

  @Test
  public void determinesFileNameProperlyFromUrlWithJson() {
    StubMapping stubMapping = WireMock.get("/foo/bar.json").willReturn(ok("{}")).build();
    bodyExtractor.extractInPlace(stubMapping);
    verifyWriteBinaryFile("foobarjson-" + stubMapping.getId() + ".json", "{}");
  }

  @Test
  public void determinesFileNameProperlyFromUrlWithText() {
    StubMapping stubMapping = WireMock.get("/foo/bar.txt").willReturn(ok("")).build();
    bodyExtractor.extractInPlace(stubMapping);
    verifyWriteBinaryFile("foobartxt-" + stubMapping.getId() + ".txt", "");
  }

  @Test
  public void determinesFileNameProperlyFromMimeTypeWithJson() {
    StubMapping stubMapping = WireMock.get("/foo/bar.txt").willReturn(okJson("{}")).build();
    bodyExtractor.extractInPlace(stubMapping);
    verifyWriteBinaryFile("foobartxt-" + stubMapping.getId() + ".json", "{}");
  }

  @Test
  public void determinesFileNameProperlyWithNamedStubMapping() {
    StubMapping stubMapping = WireMock.get("/foo").willReturn(okJson("{}")).build();
    stubMapping.setName("TEST NAME!");
    bodyExtractor.extractInPlace(stubMapping);
    verifyWriteBinaryFile("test-name-" + stubMapping.getId() + ".json", "{}");
  }

  private void verifyWriteBinaryFile(final String filename, final String body) {
    verify(filesSource).writeBinaryFile(eq(filename), eq(body.getBytes()));
  }
}
