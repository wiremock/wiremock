/*
 * Copyright (C) 2014-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ExtensionFactory;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.Test;

public class ResponseTransformerV2AcceptanceTest {

  WireMockServer wm;
  WireMockTestClient client;

  @Test
  public void transformsStubResponse() {
    startWithExtensions(StubResponseTransformer.class);

    wm.stubFor(
        get(urlEqualTo("/response-transform")).willReturn(aResponse().withBody("Original body")));

    assertThat(client.get("/response-transform").content(), is("Modified body"));
  }

  @Test
  public void acceptsTransformerParameters() {
    startWithExtensions(StubResponseTransformerWithParams.class);

    wm.stubFor(
        get(urlEqualTo("/response-transform-with-params"))
            .willReturn(
                aResponse()
                    .withTransformerParameter("name", "John")
                    .withTransformerParameter("number", 66)
                    .withTransformerParameter("flag", true)
                    .withBody("Original body")));

    assertThat(client.get("/response-transform-with-params").content(), is("John, 66, true"));
  }

  @Test
  public void globalTransformAppliedWithLocalParameters() {
    startWithExtensions(GlobalResponseTransformer.class);

    wm.stubFor(get(urlEqualTo("/global-response-transform")).willReturn(aResponse()));

    assertThat(client.get("/global-response-transform").firstHeader("X-Extra"), is("extra val"));
  }

  @Test
  public void filesRootIsCorrectlyPassedToTransformer() {
    startWithExtensions(services -> new FilesUsingResponseTransformer(services.getFiles()));

    wm.stubFor(get(urlEqualTo("/response-transform-with-files")).willReturn(ok()));

    assertThat(
        client.get("/response-transform-with-files").content(), endsWith("plain-example.txt"));
  }

  @SuppressWarnings("unchecked")
  private void startWithExtensions(Class<? extends Extension> extensionClasses) {
    wm = new WireMockServer(wireMockConfig().dynamicPort().extensions(extensionClasses));
    wm.start();
    client = new WireMockTestClient(wm.port());
  }

  private void startWithExtensions(ExtensionFactory<?>... extensionFactories) {
    wm = new WireMockServer(wireMockConfig().dynamicPort().extensions(extensionFactories));
    wm.start();
    client = new WireMockTestClient(wm.port());
  }

  public static class StubResponseTransformer implements ResponseTransformerV2 {

    @Override
    public Response transform(Response response, ServeEvent serveEvent) {
      return Response.Builder.like(response).but().body("Modified body").build();
    }

    @Override
    public boolean applyGlobally() {
      return true;
    }

    @Override
    public String getName() {
      return "stub-transformer";
    }
  }

  public static class StubResponseTransformerWithParams implements ResponseTransformerV2 {

    @Override
    public Response transform(Response response, ServeEvent serveEvent) {
      Parameters parameters = serveEvent.getTransformerParameters();
      return Response.Builder.like(response)
          .but()
          .body(
              parameters.getString("name")
                  + ", "
                  + parameters.getInt("number")
                  + ", "
                  + parameters.getBoolean("flag"))
          .build();
    }

    @Override
    public boolean applyGlobally() {
      return true;
    }

    @Override
    public String getName() {
      return "stub-transformer-with-params";
    }
  }

  public static class GlobalResponseTransformer implements ResponseTransformerV2 {

    @Override
    public Response transform(Response response, ServeEvent serveEvent) {
      return Response.Builder.like(response)
          .but()
          .headers(response.getHeaders().plus(httpHeader("X-Extra", "extra val")))
          .build();
    }

    @Override
    public String getName() {
      return "global-response-transformer";
    }

    @Override
    public boolean applyGlobally() {
      return true;
    }
  }

  public static class FilesUsingResponseTransformer implements ResponseTransformerV2 {

    private final FileSource fileSource;

    public FilesUsingResponseTransformer(FileSource fileSource) {
      this.fileSource = fileSource;
    }

    @Override
    public Response transform(Response response, ServeEvent serveEvent) {
      return Response.Builder.like(response)
          .but()
          .body(fileSource.getTextFileNamed("plain-example.txt").getPath())
          .build();
    }

    @Override
    public String getName() {
      return "files-using-response-transformer";
    }

    @Override
    public boolean applyGlobally() {
      return true;
    }
  }
}
