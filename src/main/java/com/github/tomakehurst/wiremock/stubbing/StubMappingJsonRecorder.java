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
package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.common.UniqueFilenameGenerator;
import com.github.tomakehurst.wiremock.common.VeryShortIdGenerator;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.ValuePattern;
import com.github.tomakehurst.wiremock.verification.VerificationResult;

import java.util.List;

import static com.github.tomakehurst.wiremock.common.Json.write;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static java.util.Arrays.asList;

public class StubMappingJsonRecorder implements RequestListener {

    private final FileSource mappingsFileSource;
    private final FileSource filesFileSource;
    private final Admin admin;
    private final List<CaseInsensitiveKey> headersToMatch;
    private IdGenerator idGenerator;

    public StubMappingJsonRecorder(FileSource mappingsFileSource, FileSource filesFileSource, Admin admin, List<CaseInsensitiveKey> headersToMatch) {
        this.mappingsFileSource = mappingsFileSource;
        this.filesFileSource = filesFileSource;
        this.admin = admin;
        this.headersToMatch = headersToMatch;
        idGenerator = new VeryShortIdGenerator();
    }

    @Override
    public void requestReceived(Request request, Response response) {
        RequestPattern requestPattern = buildRequestPatternFrom(request);

        if (requestNotAlreadyReceived(requestPattern) && response.isFromProxy()) {
            notifier().info(String.format("Recording mappings for %s", request.getUrl()));
            writeToMappingAndBodyFile(request, response, requestPattern);
        } else {
            notifier().info(String.format("Not recording mapping for %s as this has already been received", request.getUrl()));
        }
    }

    private RequestPattern buildRequestPatternFrom(Request request) {
        RequestPattern requestPattern = new RequestPattern(request.getMethod(), request.getUrl());
        if (!headersToMatch.isEmpty()) {
            for (HttpHeader header: request.getHeaders().all()) {
                if (headersToMatch.contains(header.caseInsensitiveKey())) {
                    requestPattern.addHeader(header.key(), ValuePattern.equalTo(header.firstValue()));
                }
            }
        }

        String body = request.getBodyAsString();
        if (!body.isEmpty()) {
            ValuePattern bodyPattern = ValuePattern.equalTo(request.getBodyAsString());
            requestPattern.setBodyPatterns(asList(bodyPattern));
        }

        return requestPattern;
    }

    private void writeToMappingAndBodyFile(Request request, Response response, RequestPattern requestPattern) {
        String fileId = idGenerator.generate();
        String mappingFileName = UniqueFilenameGenerator.generate(request, "mapping", fileId);
        String bodyFileName = UniqueFilenameGenerator.generate(request, "body", fileId);
        ResponseDefinition responseToWrite = new ResponseDefinition();
        responseToWrite.setStatus(response.getStatus());
        responseToWrite.setBodyFileName(bodyFileName);

        if (response.getHeaders().size() > 0) {
            responseToWrite.setHeaders(response.getHeaders());
        }

        StubMapping mapping = new StubMapping(requestPattern, responseToWrite);

        filesFileSource.writeBinaryFile(bodyFileName, response.getBody());
        mappingsFileSource.writeTextFile(mappingFileName, write(mapping));
    }

    private boolean requestNotAlreadyReceived(RequestPattern requestPattern) {
        VerificationResult verificationResult = admin.countRequestsMatching(requestPattern);
        verificationResult.assertRequestJournalEnabled();
        return (verificationResult.getCount() <= 1);
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

}
