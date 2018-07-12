/*
 * Copyright (C) 2011 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class GetStubFilesTask implements AdminTask {
    @Override
    public ResponseDefinition execute(final Admin admin, final Request request, final PathParams pathParams) {
        try {
            final FileSource fileSource = admin.getOptions().filesRoot().child(FILES_ROOT);
            final TextFile textFile = fileSource.getTextFileNamed(pathParams.get("filename"));
            return ResponseDefinition.okForJson(textFile.readContentsAsString());
        } catch (final Exception e) {
            return ResponseDefinition.badRequest(Errors.single(60, "Could not find specified file."));
        }
    }
}
