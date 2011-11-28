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
	private final ProxyResponseRenderer proxyResponseRenderer;

	public MockServiceResponseRenderer(FileSource fileSource,
			GlobalSettingsHolder globalSettingsHolder) {
		this.fileSource = fileSource;
		this.globalSettingsHolder = globalSettingsHolder;
		this.proxyResponseRenderer = new ProxyResponseRenderer();
	}

	@Override
	public void render(Response response, HttpServletResponse httpServletResponse) {
	    addDelayIfSpecifiedIn(response);
	    
	    if (response.isProxyResponse()) {
	    	proxyResponseRenderer.render(response, httpServletResponse);
	    } else {
	    	renderResponseDirectly(response, httpServletResponse);
	    }
	}
	
	private void renderResponseDirectly(Response response, HttpServletResponse httpServletResponse) {
		httpServletResponse.setStatus(response.getStatus());
		addHeaders(response, httpServletResponse);
			
		if (response.specifiesBodyFile()) {
			TextFile bodyFile = fileSource.getTextFileNamed(response.getBodyFileName());
			writeAndConvertExceptions(httpServletResponse, bodyFile.readContents());
		} else if (response.specifiesBodyContent()) {
			writeAndConvertExceptions(httpServletResponse, response.getBody());
		}
	}
	
	private void writeAndConvertExceptions(HttpServletResponse httpServletResponse, String content) {
		try {
			httpServletResponse.getWriter().write(content);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private void addHeaders(Response response,
			HttpServletResponse httpServletResponse) {
		HttpHeaders headers = response.getHeaders();
		if (headers != null) {
			for (Map.Entry<String, String> header: headers.entrySet()) {
				httpServletResponse.addHeader(header.getKey(), header.getValue());
			}
		}
	}

    private void addDelayIfSpecifiedIn(Response response) {
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
