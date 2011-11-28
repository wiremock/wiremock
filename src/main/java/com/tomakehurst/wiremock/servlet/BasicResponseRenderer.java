package com.tomakehurst.wiremock.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.mapping.Response;
import com.tomakehurst.wiremock.mapping.ResponseDefinition;

public class BasicResponseRenderer implements ResponseRenderer {

	@Override
	public void render(ResponseDefinition response, HttpServletResponse httpServletResponse) {
		httpServletResponse.setStatus(response.getStatus());
		try {
			HttpHeaders headers = response.getHeaders();
			if (headers != null) {
				for (Map.Entry<String, String> header: headers.entrySet()) {
					httpServletResponse.addHeader(header.getKey(), header.getValue());
				}
			}
			
			if (response.getBody() != null) {
				httpServletResponse.getWriter().write(response.getBody());
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public Response render(ResponseDefinition responseDefinition) {
		Response response = new Response(responseDefinition.getStatus());
		response.addHeaders(responseDefinition.getHeaders());
		response.setBody(responseDefinition.getBody());
		return response;
	}
}
