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
import com.github.tomakehurst.wiremock.common.VeryShortIdGenerator;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.RequestJournal;

import java.net.URI;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.common.Json.write;

public class StubMappingJsonRecorder implements RequestListener {
	
	private final FileSource mappingsFileSource;
	private final FileSource filesFileSource;
	private final Admin admin;
	private IdGenerator idGenerator;
	
	public StubMappingJsonRecorder(FileSource mappingsFileSource, FileSource filesFileSource, Admin admin) {
		this.mappingsFileSource = mappingsFileSource;
		this.filesFileSource = filesFileSource;
		this.admin = admin;
		idGenerator = new VeryShortIdGenerator();
	}

	@Override
	public void requestReceived(Request request, Response response) {
		RequestPattern requestPattern = new RequestPattern(request.getMethod(), request.getUrl());
		
		if (requestNotAlreadyReceived(requestPattern) && response.isFromProxy()) {
		    notifier().info(String.format("Recording mappings for %s", request.getUrl()));
		    writeToMappingAndBodyFile(request, response, requestPattern);
		} else {
		    notifier().info(String.format("Not recording mapping for %s as this has already been received", request.getUrl()));
		}
	}

    private void writeToMappingAndBodyFile(Request request, Response response, RequestPattern requestPattern) {
        String fileId = idGenerator.generate();
        String mappingFileName = generateNewUniqueFileNameFromRequest(request, "mapping", fileId);
        String bodyFileName = generateNewUniqueFileNameFromRequest(request, "body", fileId);
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
        return admin.countRequestsMatching(requestPattern) <= 1;
    }
	
	private String generateNewUniqueFileNameFromRequest(Request request, String prefix, String id) {
	    URI uri = URI.create(request.getUrl());
	    String[] pathNodes = uri.getPath().split("/");
	    StringBuilder sb = new StringBuilder(prefix).append("-");
	    if (pathNodes.length > 1) {
	        sb.append(pathNodes[pathNodes.length - 2]).append("-");
	    }
	        
        sb.append(pathNodes[pathNodes.length - 1])
            .append("-")
	        .append(id)
	        .append(".json");
        
        return sb.toString();
	}
	
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

}
