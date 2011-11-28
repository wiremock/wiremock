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
	public void render(ResponseDefinition response, HttpServletResponse httpServletResponse) {
	    addDelayIfSpecifiedIn(response);
	    
	    if (response.isProxyResponse()) {
	    	proxyResponseRenderer.render(response, httpServletResponse);
	    } else {
	    	renderResponseDirectly(response, httpServletResponse);
	    }
	}
	
	@Override
	public Response render(ResponseDefinition responseDefinition) {
		if (!responseDefinition.wasConfigured()) {
			return Response.notConfigured();
		}
		
		addDelayIfSpecifiedIn(responseDefinition);
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
		
		return response;
	}
	
	private void renderResponseDirectly(ResponseDefinition response, HttpServletResponse httpServletResponse) {
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

	private void addHeaders(ResponseDefinition response,
			HttpServletResponse httpServletResponse) {
		HttpHeaders headers = response.getHeaders();
		if (headers != null) {
			for (Map.Entry<String, String> header: headers.entrySet()) {
				httpServletResponse.addHeader(header.getKey(), header.getValue());
			}
		}
	}

    private void addDelayIfSpecifiedIn(ResponseDefinition response) {
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
