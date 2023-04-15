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

import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.Stores;
import java.util.List;

public class GetAllStubFilesTask implements AdminTask {

  private final Stores stores;

  public GetAllStubFilesTask(Stores stores) {
    this.stores = stores;
  }

  @Override
  public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
    BlobStore filesBlobStore = stores.getFilesBlobStore();
    List<String> filePaths = filesBlobStore.getAllKeys().sorted().collect(toList());
    return ResponseDefinition.okForJson(filePaths);
  }
}
