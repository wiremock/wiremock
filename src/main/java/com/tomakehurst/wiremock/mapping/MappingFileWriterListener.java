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
package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.mapping.JsonMappingBinder.write;

import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.IdGenerator;
import com.tomakehurst.wiremock.common.TextFile;
import com.tomakehurst.wiremock.common.VeryShortIdGenerator;

public class MappingFileWriterListener implements RequestListener {
	
	private final FileSource mappingsFileSource;
	private final FileSource filesFileSource;
	private IdGenerator idGenerator;
	
	public MappingFileWriterListener(FileSource mappingsFileSource, FileSource filesFileSource) {
		this.mappingsFileSource = mappingsFileSource;
		this.filesFileSource = filesFileSource;
		idGenerator = new VeryShortIdGenerator();
	}

	@Override
	public void requestReceived(Request request, Response response) {
	    String fileId = idGenerator.generate();
		String mappingFileName = generateNewUniqueFileNameFromRequest(request, "mapping", fileId);
		String bodyFileName = generateNewUniqueFileNameFromRequest(request, "body", fileId);
		
		RequestPattern requestPattern = new RequestPattern(request.getMethod(), request.getUrl());
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
	
	private Function<TextFile, Integer> toNumberUsingPattern(final Pattern pattern) {
		return new Function<TextFile, Integer>() {
			public Integer apply(TextFile input) {
				Matcher matcher = pattern.matcher(input.name());
				matcher.find();
				return Integer.parseInt(matcher.group(1));
			}
		};
	}
	
	private Predicate<TextFile> matching(final Pattern pattern) {
		return new Predicate<TextFile>() {
			public boolean apply(TextFile input) {
				return pattern.matcher(input.name()).matches();
			}
		};
	}

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

}
