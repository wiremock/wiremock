/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.defaultTestFilesRoot;
import static net.javacrumbs.jsonunit.JsonMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.store.Stores;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ExtensionFactoryTest {

  WireMockServer wm;
  WireMockTestClient client;

  @AfterEach
  void stopServer() {
    if (wm != null) {
      wm.close();
    }
  }

  @Test
  void injectsCoreServicesOnConstructionByFactory() {
    initialiseWireMockServer(
        options()
            .dynamicPort()
            .withRootDirectory(defaultTestFilesRoot())
            .stubCorsEnabled(true)
            .templatingEnabled(false)
            .extensionScanningEnabled(true)
            .extensions(
                services ->
                    List.of(
                        new MiscInfoApi(
                            services.getAdmin(),
                            services.getOptions(),
                            services.getStores(),
                            services.getFiles(),
                            services.getExtensions()))));

    client.get("/something");
    client.get("/something");

    String content = client.get("/__admin/misc-info").content();

    assertThat(content, jsonPartEquals("example1", "Example 1"));
    assertThat(
        content,
        jsonPartMatches("fileSourcePath", endsWith("test-file-root" + File.separator + "__files")));
    assertThat(content, jsonPartEquals("requestCount", 2));
    assertThat(content, jsonPartEquals("stubCorsEnabled", true));
    assertThat(
        content,
        jsonPartEquals("extensionCount", 5)); // Includes the three service loaded extensions
  }

  @Test
  void usesExtensionFactoryLoadedViaServiceLoaderWhenEnabled() {
    initialiseWireMockServer(
        options()
            .dynamicPort()
            .extensionScanningEnabled(true)
            .withRootDirectory(defaultTestFilesRoot())
            .templatingEnabled(false));

    wm.stubFor(get("/transform-this").willReturn(noContent().withTransformers("loader-test")));

    client.get("/just-count-this");

    assertThat(client.get("/transform-this").content(), is("Request count 1"));
  }

  @Test
  void doesNotUseExtensionFactoryLoadedViaServiceLoaderByDefault() {
    initialiseWireMockServer(
        options().dynamicPort().withRootDirectory(defaultTestFilesRoot()).templatingEnabled(false));

    wm.stubFor(get("/transform-this").willReturn(noContent().withTransformers("loader-test")));

    client.get("/just-count-this");

    assertThat(client.get("/transform-this").statusCode(), is(204));
  }

  @Test
  void usesExtensionInstanceLoadedViaServiceLoader() {
    initialiseWireMockServer(
        options()
            .dynamicPort()
            .extensionScanningEnabled(true)
            .withRootDirectory(defaultTestFilesRoot())
            .templatingEnabled(false));

    wm.stubFor(
        get("/transform-this").willReturn(noContent().withTransformers("instance-loader-test")));

    assertThat(client.get("/transform-this").content(), is("Expected stuff"));
  }

  private void initialiseWireMockServer(WireMockConfiguration options) {
    wm = new WireMockServer(options);
    wm.start();
    client = new WireMockTestClient(wm.port());
  }

  public static class MiscInfoApi implements AdminApiExtension {

    private final Admin admin;
    private final Options options;
    private final Stores stores;
    private final FileSource fileSource;

    private final Extensions extensions;

    public MiscInfoApi(
        Admin admin, Options options, Stores stores, FileSource fileSource, Extensions extensions) {
      this.admin = admin;
      this.options = options;
      this.stores = stores;
      this.fileSource = fileSource;
      this.extensions = extensions;
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
                    "requestCount", requestCount,
                    "stubCorsEnabled", options.getStubCorsEnabled(),
                    "extensionCount", extensions.getCount()));
          });
    }
  }
}
