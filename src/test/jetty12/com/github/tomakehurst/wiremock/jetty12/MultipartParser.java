/*
 * Copyright (C) 2018-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty12;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.servlet.WireMockHttpServletMultipartAdapter;
import jakarta.servlet.http.Part;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.jetty.client.BytesRequestContent;
import org.eclipse.jetty.ee10.servlet.ServletMultiPartFormData.Parts;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.util.Attributes;

public class MultipartParser
    implements com.github.tomakehurst.wiremock.MultipartParserLoader.MultipartParser {
  @SuppressWarnings("unchecked")
  @Override
  public Collection<Request.Part> parse(byte[] body, String contentType) {
    String boundary = MultiPart.extractBoundary(contentType);

    final File filesDirectory = new File(System.getProperty("java.io.tmpdir"));
    final CompletableFuture<Collection<Part>> parts =
        MultiPartFormData.from(
                Attributes.NULL,
                null,
                boundary,
                parser -> {
                  try {
                    // No existing core parts, so we need to configure the parser.

                    Content.Source source = new BytesRequestContent(body);
                    parser.setFilesDirectory(filesDirectory.toPath());
                    return parser.parse(source);
                  } catch (Throwable failure) {
                    return CompletableFuture.failedFuture(failure);
                  }
                })
            .thenApply(
                formDataParts -> new Parts(filesDirectory.toPath(), formDataParts).getParts());

    try {
      return parts.get().stream()
          .map(WireMockHttpServletMultipartAdapter::from)
          .collect(Collectors.toList());
    } catch (Exception e) {
      return throwUnchecked(e, Collection.class);
    }
  }
}
