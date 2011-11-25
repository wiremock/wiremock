package com.tomakehurst.wiremock.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.mapping.Response;

public class BasicResponseRenderer implements ResponseRenderer {

	@Override
	public void render(Response response, HttpServletResponse httpServletResponse) {
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
}
