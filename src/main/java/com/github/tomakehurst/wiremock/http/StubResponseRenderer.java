/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.http.Response.response;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.extension.ProxyRenderer;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.SettingsStore;
import com.github.tomakehurst.wiremock.store.files.BlobStoreFileSource;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;

public class StubResponseRenderer implements ResponseRenderer {

  private final BlobStore filesBlobStore;
  private final FileSource filesFileSource;
  private final SettingsStore settingsStore;
  private final List<ProxyRenderer> proxyResponseRenderers;
  private final ProxyResponseRenderer defaultProxyResponseRenderer;
  private final List<ResponseTransformer> responseTransformers;
  private final List<ResponseTransformerV2> v2ResponseTransformers;

  public StubResponseRenderer(
      BlobStore filesBlobStore,
      SettingsStore settingsStore,
      List<ProxyRenderer> proxyResponseRenderers,
      ProxyResponseRenderer defaultProxyResponseRenderer,
      List<ResponseTransformer> responseTransformers,
      List<ResponseTransformerV2> v2ResponseTransformers) {
    this.filesBlobStore = filesBlobStore;
    this.settingsStore = settingsStore;
    this.proxyResponseRenderers = proxyResponseRenderers;
    this.defaultProxyResponseRenderer = defaultProxyResponseRenderer;
    this.responseTransformers = responseTransformers;
    this.v2ResponseTransformers = v2ResponseTransformers;

    filesFileSource = new BlobStoreFileSource(filesBlobStore);
  }

  @Override
  public Response render(ServeEvent serveEvent) {
    ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();
    if (!responseDefinition.wasConfigured()) {
      return Response.notConfigured();
    }

    Response response = buildResponse(serveEvent);

    response =
        applyTransformations(
            responseDefinition.getOriginalRequest(),
            responseDefinition,
            response,
            responseTransformers);

    response = applyV2Transformations(response, serveEvent, v2ResponseTransformers);

    return response;
  }

  private Response buildResponse(ServeEvent serveEvent) {
    if (serveEvent.getResponseDefinition().isProxyResponse()) {
      for (ProxyRenderer renderer : proxyResponseRenderers) {
        if (renderer.applyFor(serveEvent)) {
          return renderer.render(serveEvent);
        }
      }
      return defaultProxyResponseRenderer.render(serveEvent);
    } else {
      Response.Builder responseBuilder = renderDirectly(serveEvent);
      return responseBuilder.build();
    }
  }

  private Response applyTransformations(
      Request request,
      ResponseDefinition responseDefinition,
      Response response,
      List<ResponseTransformer> transformers) {
    if (transformers.isEmpty()) {
      return response;
    }

    ResponseTransformer transformer = transformers.get(0);
    Response newResponse =
        transformer.applyGlobally() || responseDefinition.hasTransformer(transformer)
            ? transformer.transform(
                request, response, filesFileSource, responseDefinition.getTransformerParameters())
            : response;

    return applyTransformations(
        request, responseDefinition, newResponse, transformers.subList(1, transformers.size()));
  }

  private Response applyV2Transformations(
      Response response, ServeEvent serveEvent, List<ResponseTransformerV2> transformers) {

    if (transformers.isEmpty()) {
      return response;
    }

    final ResponseTransformerV2 transformer = transformers.get(0);
    final ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();

    Response newResponse =
        transformer.applyGlobally() || responseDefinition.hasTransformer(transformer)
            ? transformer.transform(response, serveEvent)
            : response;

    return applyV2Transformations(
        newResponse, serveEvent, transformers.subList(1, transformers.size()));
  }

  private Response.Builder renderDirectly(ServeEvent serveEvent) {
    ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();

    HttpHeaders headers = responseDefinition.getHeaders();
    StubMapping stubMapping = serveEvent.getStubMapping();
    if (serveEvent.getWasMatched() && stubMapping != null) {
      headers =
          getFirstNonNull(headers, new HttpHeaders())
              .plus(new HttpHeader("Matched-Stub-Id", stubMapping.getId().toString()));

      if (stubMapping.getName() != null) {
        headers = headers.plus(new HttpHeader("Matched-Stub-Name", stubMapping.getName()));
      }
    }

    GlobalSettings settings = settingsStore.get();
    Response.Builder responseBuilder =
        response()
            .status(responseDefinition.getStatus())
            .statusMessage(responseDefinition.getStatusMessage())
            .headers(headers)
            .fault(responseDefinition.getFault())
            .configureDelay(
                settings.getFixedDelay(),
                settings.getDelayDistribution(),
                responseDefinition.getFixedDelayMilliseconds(),
                responseDefinition.getDelayDistribution())
            .chunkedDribbleDelay(responseDefinition.getChunkedDribbleDelay());

    if (responseDefinition.specifiesBodyFile()) {
      final InputStreamSource bodyStreamSource =
          filesBlobStore.getStreamSource(responseDefinition.getBodyFileName());
      responseBuilder.body(bodyStreamSource);
    } else if (responseDefinition.specifiesBodyContent()) {
      responseBuilder.body(responseDefinition.getByteBody());
    }

    return responseBuilder;
  }
}
