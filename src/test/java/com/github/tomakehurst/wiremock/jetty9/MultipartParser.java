/*
 * Copyright (C) 2018-2021 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty9;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.servlet.WireMockHttpServletMultipartAdapter;
import com.google.common.base.Function;
import jakarta.servlet.http.Part;
import org.eclipse.jetty.server.MultiPartFormInputStream;

import java.io.ByteArrayInputStream;
import java.util.Collection;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.google.common.collect.FluentIterable.from;

public class MultipartParser {

  @SuppressWarnings("unchecked")
  public static Collection<Request.Part> parse(byte[] body, String contentType) {
      MultiPartFormInputStream parser =
        new MultiPartFormInputStream(new ByteArrayInputStream(body), contentType, null, null);
    try {
      return from(parser.getParts())
          .transform(
              new Function<Part, Request.Part>() {
                @Override
                public Request.Part apply(Part input) {
                  return WireMockHttpServletMultipartAdapter.from(input);
                }
              })
          .toList();
    } catch (Exception e) {
      return throwUnchecked(e, Collection.class);
    }
  }
}
