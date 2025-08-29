/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.common.ResourceUtil.getResource;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.io.IOException;
import java.io.InputStream;

/**
 * An abstract base class for admin tasks that serve documentation files from the classpath.
 *
 * <p>Subclasses must implement {@link #getMimeType()} and {@link #getFilePath()} to specify the
 * content type and location of the documentation file to be served.
 */
public abstract class AbstractGetDocTask implements AdminTask {

  @Override
  public ResponseDefinition execute(Admin admin, ServeEvent serveEvent, PathParams pathParams) {
    try (InputStream inputStream =
        getResource(AbstractGetDocTask.class, getFilePath()).openStream()) {
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

  /**
   * Gets the MIME type of the documentation file.
   *
   * @return The MIME type as a string (e.g., "text/html").
   */
  protected abstract String getMimeType();

  /**
   * Gets the classpath path to the documentation file.
   *
   * @return The path to the resource file.
   */
  protected abstract String getFilePath();
}
