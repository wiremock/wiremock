package com.tomakehurst.wiremock.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.TextFile;
import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.mapping.Response;

public class FileBodyLoadingResponseRenderer implements ResponseRenderer {
	
	private FileSource fileSource;

	public FileBodyLoadingResponseRenderer(FileSource fileSource) {
		this.fileSource = fileSource;
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

    private static void addDelayIfSpecified(Response response) {
        if (response.getFixedDelayMilliseconds() != null) {
	        try {
	            Thread.sleep(response.getFixedDelayMilliseconds());
	        } catch (InterruptedException e) {
	            throw new RuntimeException(e);
	        }
	    }
    }
}
