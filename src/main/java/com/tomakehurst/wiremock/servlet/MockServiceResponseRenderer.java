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
package com.tomakehurst.wiremock.servlet;

import com.google.common.base.Optional;
import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.TextFile;
import com.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.tomakehurst.wiremock.mapping.Response;
import com.tomakehurst.wiremock.mapping.ResponseDefinition;

public class MockServiceResponseRenderer implements ResponseRenderer {
	
	private final FileSource fileSource;
	private final GlobalSettingsHolder globalSettingsHolder;
	private final ProxyResponseRenderer proxyResponseRenderer;

	public MockServiceResponseRenderer(FileSource fileSource,
			GlobalSettingsHolder globalSettingsHolder) {
		this.fileSource = fileSource;
		this.globalSettingsHolder = globalSettingsHolder;
		this.proxyResponseRenderer = new ProxyResponseRenderer();
	}

	@Override
	public Response render(ResponseDefinition responseDefinition) {
		if (!responseDefinition.wasConfigured()) {
			return Response.notConfigured();
		}
		
		addDelayIfSpecifiedGloballyOrIn(responseDefinition);
		if (responseDefinition.isProxyResponse()) {
	    	return proxyResponseRenderer.render(responseDefinition);
	    } else {
	    	return renderDirectly(responseDefinition);
	    }
	}
	
	private Response renderDirectly(ResponseDefinition responseDefinition) {
		Response response = new Response(responseDefinition.getStatus());
		response.addHeaders(responseDefinition.getHeaders());
		
		if (responseDefinition.specifiesBodyFile()) {
			TextFile bodyFile = fileSource.getTextFileNamed(responseDefinition.getBodyFileName());
			response.setBody(bodyFile.readContents());
		} else if (responseDefinition.specifiesBodyContent()) {
			response.setBody(responseDefinition.getBody());
		}
		
		if (responseDefinition.getFault() != null) {
			response.setFault(responseDefinition.getFault());
		}
		
		return response;
	}
	
    private void addDelayIfSpecifiedGloballyOrIn(ResponseDefinition response) {
    	Optional<Integer> optionalDelay = getDelayFromResponseOrGlobalSetting(response);
        if (optionalDelay.isPresent()) {
	        try {
	            Thread.sleep(optionalDelay.get());
	        } catch (InterruptedException e) {
	            throw new RuntimeException(e);
	        }
	    }
    }
    
    private Optional<Integer> getDelayFromResponseOrGlobalSetting(ResponseDefinition response) {
    	Integer delay = response.getFixedDelayMilliseconds() != null ?
    			response.getFixedDelayMilliseconds() :
    			globalSettingsHolder.get().getFixedDelay();
    	
    	return Optional.fromNullable(delay);
    }
}
