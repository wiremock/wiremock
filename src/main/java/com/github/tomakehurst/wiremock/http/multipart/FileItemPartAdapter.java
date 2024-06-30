/*
 * Copyright (C) 2019-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.multipart;

import com.github.tomakehurst.wiremock.http.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

public class FileItemPartAdapter implements Request.Part {

  private final FileItem fileItem;

  public FileItemPartAdapter(FileItem fileItem) {
    this.fileItem = fileItem;
  }

  @Override
  public String getName() {
    return fileItem.getFieldName();
  }

  @Override
  public HttpHeader getHeader(String name) {
    Iterator<String> headerValues = fileItem.getHeaders().getHeaders(name);
    List<String> values = new ArrayList<>();
    headerValues.forEachRemaining(values::add);
    return new HttpHeader(name, values);
  }

  @Override
  public HttpHeaders getHeaders() {
    FileItemHeaders headers = fileItem.getHeaders();
    Iterator<String> i = headers.getHeaderNames();
    List<HttpHeader> headersList = new ArrayList<>();
    while (i.hasNext()) {
      String name = i.next();
      headersList.add(getHeader(name));
    }

    return new HttpHeaders(Collections.unmodifiableList(headersList));
  }

  @Override
  public Body getBody() {
    return Body.ofBinaryOrText(fileItem.get(), new ContentTypeHeader(fileItem.getContentType()));
  }

  public static final Function<FileItem, Request.Part> TO_PARTS = FileItemPartAdapter::new;
}
