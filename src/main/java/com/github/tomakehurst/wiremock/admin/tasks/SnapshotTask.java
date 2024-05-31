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
package com.github.tomakehurst.wiremock.admin.tasks;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.jsonResponse;
import static java.net.HttpURLConnection.HTTP_OK;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

public class SnapshotTask implements AdminTask {

  @Override
  public ResponseDefinition execute(Admin admin, ServeEvent serveEvent, PathParams pathParams) {
    RecordSpec recordSpec =
        serveEvent.getRequest().getBody().length == 0
            ? RecordSpec.DEFAULTS
            : admin
                .getJson()
                .readValue(serveEvent.getRequest().getBodyAsString(), RecordSpec.class);

    SnapshotRecordResult result = admin.snapshotRecord(recordSpec);
    return jsonResponse(result, HTTP_OK);
  }
}
