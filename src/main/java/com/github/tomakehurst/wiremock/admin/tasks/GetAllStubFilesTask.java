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

import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.Stores;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.List;

/**
 * An admin task to list all files available in the `__files` directory.
 *
 * <p>This task handles the API request to get a sorted list of all file paths available in the file
 * blob store.
 *
 * @see Stores
 */
public class GetAllStubFilesTask implements AdminTask {

  private final Stores stores;

  /**
   * Constructs a new GetAllStubFilesTask.
   *
   * @param stores The stores instance for accessing the file system.
   */
  public GetAllStubFilesTask(Stores stores) {
    this.stores = stores;
  }

  @Override
  public ResponseDefinition execute(Admin admin, ServeEvent serveEvent, PathParams pathParams) {
    BlobStore filesBlobStore = stores.getFilesBlobStore();
    List<String> filePaths = filesBlobStore.getAllKeys().sorted().collect(toList());
    return ResponseDefinition.okForJson(filePaths);
  }
}
