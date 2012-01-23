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
package com.github.tomakehurst.wiremock.mapping;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.mapping.JsonMappingBinder.write;

import java.net.URI;
import java.util.Map;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.common.VeryShortIdGenerator;
import com.github.tomakehurst.wiremock.verification.RequestJournal;

public class MappingFileWriterListener implements RequestListener {
	
	private final FileSource mappingsFileSource;
	private final FileSource filesFileSource;
	private final RequestJournal requestJournal;
	private IdGenerator idGenerator;
	
	public MappingFileWriterListener(FileSource mappingsFileSource, FileSource filesFileSource, RequestJournal requestJournal) {
		this.mappingsFileSource = mappingsFileSource;
		this.filesFileSource = filesFileSource;
		this.requestJournal = requestJournal;
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
        
        for (Map.Entry<String, String> header: response.getHeaders().entrySet()) {
            responseToWrite.addHeader(header.getKey(), header.getValue());
        }
        
        RequestResponseMapping mapping = new RequestResponseMapping(requestPattern, responseToWrite);
        
        filesFileSource.writeTextFile(bodyFileName, response.getBodyAsString());
        mappingsFileSource.writeTextFile(mappingFileName, write(mapping));
    }

    private boolean requestNotAlreadyReceived(RequestPattern requestPattern) {
        return requestJournal.countRequestsMatching(requestPattern) <= 1;
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
