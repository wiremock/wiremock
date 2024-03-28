/*
 * Copyright (C) 2017-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Strings.bytesFromString;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.hc.client5.http.entity.mime.AbstractContentBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Args;

public class MultipartBody extends AbstractContentBody {
  private final String name;
  private final byte[] body;

  MultipartBody(String name, byte[] body) {
    super(ContentType.APPLICATION_OCTET_STREAM);
    Args.notEmpty(name, "Name was empty");
    Args.notNull(body, "Body was null");
    this.name = name;
    this.body = body;
  }

  MultipartBody(String name, String body, ContentType contentType) {
    super(contentType);
    Args.notEmpty(name, "Name was empty");
    Args.notEmpty(body, "Body was null");
    this.name = name;
    this.body = bytesFromString(body, contentType.getCharset());
  }

  public static MultipartBody part(String name, byte[] body) {
    return new MultipartBody(name, body);
  }

  public static MultipartBody part(String name, String body, ContentType contentType) {
    return new MultipartBody(name, body, contentType);
  }

  @Override
  public String getFilename() {
    return name;
  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    out.write(body);
  }

  @Override
  public long getContentLength() {
    return body.length;
  }
}
