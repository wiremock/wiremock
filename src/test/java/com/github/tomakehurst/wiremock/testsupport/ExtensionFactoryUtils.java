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
package com.github.tomakehurst.wiremock.testsupport;

import static java.util.Collections.emptyList;

import com.github.jknack.handlebars.Helper;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ExtensionFactory;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformerTest;
import java.util.List;
import java.util.Map;

public class ExtensionFactoryUtils {

  public static ResponseTemplateTransformer buildTemplateTransformer(boolean global) {
    return (ResponseTemplateTransformer)
        buildExtension(
            new MockWireMockServices(),
            services ->
                List.of(
                    new ResponseTemplateTransformer(
                        services.getTemplateEngine(), global, services.getFiles(), emptyList())));
  }

  public static ResponseTemplateTransformer buildTemplateTransformer(
      boolean global, String helperName, Helper<?> helper) {
    return (ResponseTemplateTransformer)
        buildExtension(
            new MockWireMockServices().setHelpers(Map.of(helperName, helper)),
            services ->
                List.of(
                    new ResponseTemplateTransformer(
                        services.getTemplateEngine(), global, services.getFiles(), emptyList())));
  }

  public static ResponseTemplateTransformer buildTemplateTransformer(Long maxCacheEntries) {
    return (ResponseTemplateTransformer)
        buildExtension(
            new MockWireMockServices().setMaxCacheEntries(maxCacheEntries),
            services ->
                List.of(
                    new ResponseTemplateTransformer(
                        services.getTemplateEngine(), false, services.getFiles(), emptyList())));
  }

  public static Extension buildExtension(
      MockWireMockServices wireMockServices, ExtensionFactory factory) {
    FileSource fileSource =
        new ClasspathFileSource(
            ResponseTemplateTransformerTest.class
                .getClassLoader()
                .getResource("templates")
                .getPath());
    wireMockServices.setFileSource(fileSource);
    return factory.create(wireMockServices).stream().findFirst().get();
  }
}
