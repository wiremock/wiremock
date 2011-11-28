package com.tomakehurst.wiremock.testsupport;

import static com.google.common.io.ByteStreams.toByteArray;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class WireMockResponse {
	
	private HttpResponse httpResponse;
	
	public WireMockResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public int statusCode() {
		return httpResponse.getStatusLine().getStatusCode();
	}
	
	public String content() {
		try {
			HttpEntity entity = httpResponse.getEntity();
			return entity != null ? new String(toByteArray(entity.getContent())) : null; 
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		
	}
	
	public String header(String key) {
		return headers().get(key);
	}
	
	public Map<String, String> headers() {
		Header[] headers = httpResponse.getAllHeaders();
		Map<String, String> headerMap = new HashMap<String, String>();
		for (Header header: headers) {
			headerMap.put(header.getName(), header.getValue());
		}
		
		return headerMap;
	}

}
