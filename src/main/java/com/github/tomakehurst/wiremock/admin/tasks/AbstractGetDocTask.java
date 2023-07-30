/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin.tasks;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.io.Resources;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractGetDocTask implements AdminTask {

  @Override
  public ResponseDefinition execute(Admin admin, ServeEvent serveEvent, PathParams pathParams) {
    try (InputStream inputStream = Resources.getResource(getFilePath()).openStream()) {
      byte[] content = inputStream.readAllBytes();
      return responseDefinition()
          .withStatus(200)
          .withBody(content)
          .withHeader(CONTENT_TYPE, getMimeType())
          .build();
    } catch (IOException e) {
      return responseDefinition().withStatus(500).build();
    }
  }

  protected abstract String getMimeType();

  protected abstract String getFilePath();
}
