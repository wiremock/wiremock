/*
 * Copyright (C) 2019-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubImport.Options;
import com.github.tomakehurst.wiremock.stubbing.StubImport.Options.DuplicatePolicy;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class StubLifecycleListenerModifyingAcceptanceTest {

  @TempDir public static File tempDir;

  @RegisterExtension
  public WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(
              options()
                  .dynamicPort()
                  .withRootDirectory(tempDir.getAbsolutePath())
                  .extensions(new ModifyingStubLifecycleListener()))
          .build();

  @Test
  void stubCanBeModifiedBeforeCreationByListener() {
    wm.stubFor(get("/test").withName("Created").willReturn(ok()));
    assertThat(wm.listAllStubMappings().getMappings().get(0).getName(), is("Modified on create"));
  }

  @Test
  void stubCanBeModifiedBeforeEditingByListener() {
    StubMapping initial = wm.stubFor(get("/test").withName("Created").willReturn(ok()));

    wm.editStub(get("/test").withId(initial.getId()).withName("Edited").willReturn(ok()));

    assertThat(wm.listAllStubMappings().getMappings().get(0).getName(), is("Modified on edit"));
  }

  @ParameterizedTest
  @CsvSource({"false,3,2", "true,2,1"})
  void stubsCanBeModifiedByListenerInBatch(
      boolean deleteExistingStubs, int expectedTotalCount, int expectedUpdatedStubIndex) {
    var existingStub1 = wm.stubFor(get("/1").willReturn(ok()));
    wm.stubFor(get("/2").willReturn(ok()));

    var stub1 = get("/1-updated").withId(existingStub1.getId()).willReturn(ok()).build();
    var stub2 = get("/2-new").willReturn(ok()).build();
    wm.importStubs(
        new StubImport(
            List.of(stub1, stub2), new Options(DuplicatePolicy.OVERWRITE, deleteExistingStubs)));

    List<StubMapping> mappings = wm.listAllStubMappings().getMappings();
    assertThat(mappings, hasSize(expectedTotalCount));
    assertThat(mappings.get(0).getId(), is(stub2.getId()));
    assertThat(mappings.get(0).getName(), is("Modified on create"));
    assertThat(mappings.get(expectedUpdatedStubIndex).getId(), is(stub1.getId()));
    assertThat(mappings.get(expectedUpdatedStubIndex).getName(), is("Modified on edit"));
    assertThat(
        mappings.get(expectedUpdatedStubIndex).getInsertionIndex(),
        is(existingStub1.getInsertionIndex()));
  }

  public static class ModifyingStubLifecycleListener implements StubLifecycleListener {

    @Override
    public StubMapping beforeStubCreated(StubMapping stub) {
      return stub.transform(b -> b.setName("Modified on create"));
    }

    @Override
    public StubMapping beforeStubEdited(StubMapping oldStub, StubMapping newStub) {
      return newStub.transform(b -> b.setName("Modified on edit"));
    }

    @Override
    public String getName() {
      return "modifying-listener";
    }
  }
}
