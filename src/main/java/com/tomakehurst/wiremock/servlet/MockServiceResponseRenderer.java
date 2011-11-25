package com.tomakehurst.wiremock.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Optional;
import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.TextFile;
import com.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.mapping.Response;

public class MockServiceResponseRenderer implements ResponseRenderer {
	
	private final FileSource fileSource;
	private final GlobalSettingsHolder globalSettingsHolder;

	public MockServiceResponseRenderer(FileSource fileSource, GlobalSettingsHolder globalSettingsHolder) {
		this.fileSource = fileSource;
		this.globalSettingsHolder = globalSettingsHolder;
	}

	@Override
	public void render(Response response, HttpServletResponse httpServletResponse) {
	    addDelayIfSpecified(response);
	    
		httpServletResponse.setStatus(response.getStatus());
		try {
			HttpHeaders headers = response.getHeaders();
			if (headers != null) {
				for (Map.Entry<String, String> header: headers.entrySet()) {
					httpServletResponse.addHeader(header.getKey(), header.getValue());
				}
			}
			
			if (response.getBodyFileName() != null) {
				TextFile bodyFile = fileSource.getTextFileNamed(response.getBodyFileName());
				httpServletResponse.getWriter().write(bodyFile.readContents());
			} else if (response.getBody() != null) {
				httpServletResponse.getWriter().write(response.getBody());
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

    private void addDelayIfSpecified(Response response) {
    	Optional<Integer> optionalDelay = getDelayFromResponseOrGlobalSetting(response);
        if (optionalDelay.isPresent()) {
	        try {
	            Thread.sleep(optionalDelay.get());
	        } catch (InterruptedException e) {
	            throw new RuntimeException(e);
	        }
	    }
    }
    
    private Optional<Integer> getDelayFromResponseOrGlobalSetting(Response response) {
    	Integer delay = response.getFixedDelayMilliseconds() != null ?
    			response.getFixedDelayMilliseconds() :
    			globalSettingsHolder.get().getFixedDelay();
    	
    	return Optional.fromNullable(delay);
    }
}
