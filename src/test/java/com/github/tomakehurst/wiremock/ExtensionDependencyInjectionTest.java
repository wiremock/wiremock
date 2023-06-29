/*
 * Copyright (C) 2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.defaultTestFilesRoot;
import static net.javacrumbs.jsonunit.JsonMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;

import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.store.Stores;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ExtensionDependencyInjectionTest {

  WireMockServer wm;

  @AfterEach
  void stopServer() {
    if (wm != null) {
      wm.stop();
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void injectsCoreServicesOnConstructionByClass() {
    wm =
        new WireMockServer(
            options()
                .dynamicPort()
                .withRootDirectory(defaultTestFilesRoot())
                .extensions(RequestCounterApi.class));
    wm.start();
    WireMockTestClient client = new WireMockTestClient(wm.port());

    client.get("/something");
    client.get("/something");

    String content = client.get("/__admin/misc-info").content();

    assertThat(content, jsonPartEquals("example1", "Example 1"));
    assertThat(content, jsonPartMatches("fileSourcePath", endsWith("test-file-root")));
    assertThat(content, jsonPartEquals("requestCount", 2));
  }

  @Test
  void injectsCoreServicesOnConstructionByClassName() {
    wm =
        new WireMockServer(
            options()
                .dynamicPort()
                .withRootDirectory(defaultTestFilesRoot())
                .extensions(
                    "com.github.tomakehurst.wiremock.ExtensionDependencyInjectionTest$RequestCounterApi"));
    wm.start();
    WireMockTestClient client = new WireMockTestClient(wm.port());

    client.get("/something");
    client.get("/something");

    String content = client.get("/__admin/misc-info").content();

    assertThat(content, jsonPartEquals("example1", "Example 1"));
    assertThat(content, jsonPartMatches("fileSourcePath", endsWith("test-file-root")));
    assertThat(content, jsonPartEquals("requestCount", 2));
  }

  public static class RequestCounterApi implements AdminApiExtension {

    private final Admin admin;
    private final Stores stores;
    private final FileSource fileSource;

    @Inject
    public RequestCounterApi(Admin admin, Stores stores, FileSource fileSource) {
      this.admin = admin;
      this.stores = stores;
      this.fileSource = fileSource;
    }

    @Override
    public String getName() {
      return "request-counter";
    }

    @Override
    public void contributeAdminApiRoutes(Router router) {
      router.add(
          GET,
          "/misc-info",
          (ignored, serveEvent, pathParams) -> {
            String example1 =
                Strings.stringFromBytes(stores.getFilesBlobStore().get("plain-example1.txt").get());
            String fileSourcePath = fileSource.getPath();
            int requestCount = admin.getServeEvents().getRequests().size();
            return ResponseDefinition.okForJson(
                Map.of(
                    "example1", example1,
                    "fileSourcePath", fileSourcePath,
                    "requestCount", requestCount));
          });
    }
  }
}
