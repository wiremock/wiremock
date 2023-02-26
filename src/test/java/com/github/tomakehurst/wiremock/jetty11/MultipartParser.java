/*
 * Copyright (C) 2018-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty11;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.servlet.WireMockHttpServletMultipartAdapter;
import com.google.common.base.Function;
import jakarta.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.stream.Collectors;
import org.eclipse.jetty.server.MultiPartInputStreamParser;

public class MultipartParser {

  @SuppressWarnings("unchecked")
  public static Collection<Request.Part> parse(byte[] body, String contentType) {
    MultiPartInputStreamParser parser =
        new MultiPartInputStreamParser(new ByteArrayInputStream(body), contentType, null, null);
    try {
      return parser.getParts().stream()
          .map((Function<Part, Request.Part>) WireMockHttpServletMultipartAdapter::from)
          .collect(Collectors.toList());
    } catch (Exception e) {
      return throwUnchecked(e, Collection.class);
    }
  }
}
