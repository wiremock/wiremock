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
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.common.Json.write;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;

public class StubMappingJsonRecorder implements RequestListener {

    private final FileSource mappingsFileSource;
    private final FileSource filesFileSource;
    private final Admin admin;
    private final List<CaseInsensitiveKey> headersToMatch;
    private IdGenerator idGenerator;
    private final List<LoggedRequest> recorded = new ArrayList<LoggedRequest>();

    public StubMappingJsonRecorder(FileSource mappingsFileSource, FileSource filesFileSource, Admin admin, List<CaseInsensitiveKey> headersToMatch) {
        this.mappingsFileSource = mappingsFileSource;
        this.filesFileSource = filesFileSource;
        this.admin = admin;
        this.headersToMatch = headersToMatch;
        idGenerator = new VeryShortIdGenerator();
    }

//    @Override
//    public void requestReceived(Request request, Response response) {
//        RequestPattern requestPattern = buildRequestPatternFrom(request);
//
//        if (requestNotAlreadyReceived(requestPattern) && response.isFromProxy()) {
//            notifier().info(String.format("Recording mappings for %s", request.getUrl()));
//            writeToMappingAndBodyFile(request, response, requestPattern);
//        } else {
//            notifier().info(String.format("Not recording mapping for %s as this has already been received", request.getUrl()));
//        }
//    }

    /* 
     * Updated <code>requestReceived</code> to correctly handle multiple concurrent identical 
     * requests. The initial implementation discarded any/all responses if a pending request
     * matched the request/response.
     * 
     * And 'synchronized' ensures that only one of the request/response pairs is recorded 
     * for the case where responses are received 'simultaneously' (i.e. within the execution
     * time of this method).
     * 
     * @see com.github.tomakehurst.wiremock.ConcurrentDelayedResponsesFixTest
     */
    @Override
    public synchronized void requestReceived(Request request, Response response) {
        RequestPattern requestPattern = buildRequestPatternFrom(request);

        if (requestNotAlreadyRecorded(requestPattern) && response.isFromProxy()) {
            notifier().info(String.format("Recording mappings for %s", request.getUrl()));
            writeToMappingAndBodyFile(request, response, requestPattern);
            recorded.add(LoggedRequest.createFrom(request));
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
            ValuePattern bodyPattern = valuePatternForContentType(request);
            requestPattern.setBodyPatterns(asList(bodyPattern));
        }

        return requestPattern;
    }

    private ValuePattern valuePatternForContentType(Request request) {
        String contentType = request.getHeader("Content-Type");
        if (contentType != null) {
            if (contentType.contains("json")) {
                return ValuePattern.equalToJson(request.getBodyAsString(), LENIENT);
            } else if (contentType.contains("xml")) {
                return ValuePattern.equalToXml(request.getBodyAsString());
            }
        }

        return ValuePattern.equalTo(request.getBodyAsString());
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

    private boolean requestNotAlreadyRecorded(RequestPattern requestPattern) {
        if (requestNotAlreadyReceived(requestPattern)) {
            return true;
        }
    
        if (size(filter(recorded, matchedBy(requestPattern))) < 1) {
            return true;
        }
        
        return false;
    }

    private boolean requestNotAlreadyReceived(RequestPattern requestPattern) {
        VerificationResult verificationResult = admin.countRequestsMatching(requestPattern);
        verificationResult.assertRequestJournalEnabled();
        return (verificationResult.getCount() <= 1);
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    private Predicate<Request> matchedBy(final RequestPattern requestPattern) {
        return new Predicate<Request>() {
            public boolean apply(Request input) {
                return requestPattern.isMatchedBy(input);
            }
        };
    }

}
