package com.tomakehurst.wiremock.servlet;

import static com.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.tomakehurst.wiremock.http.RequestMethod.PUT;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.common.base.Optional;
import com.tomakehurst.wiremock.http.ContentTypeHeader;
import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.Request;
import com.tomakehurst.wiremock.mapping.Response;

public class ProxyResponseRenderer implements ResponseRenderer {

	public void render(Response response, HttpServletResponse httpServletResponse) {
		HttpClient client = new DefaultHttpClient();
		HttpUriRequest httpRequest = getHttpRequestFor(response);
		addRequestHeaders(httpRequest, response);
		
		try {
			addBodyIfPostOrPut(httpRequest, response);
			HttpResponse httpResponse = client.execute(httpRequest);
			httpServletResponse.setStatus(httpResponse.getStatusLine().getStatusCode());
			for (Header header: httpResponse.getAllHeaders()) {
				httpServletResponse.addHeader(header.getName(), header.getValue());
			}
			
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				entity.writeTo(httpServletResponse.getOutputStream());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static HttpUriRequest getHttpRequestFor(Response response) {
		RequestMethod method = response.getOriginalRequest().getMethod();
		String url = response.getProxyBaseUrl() + response.getOriginalRequest().getUrl();
		
		switch (method) {
		case GET:
			return new HttpGet(url);
		case POST:
			return new HttpPost(url);
		case PUT:
			return new HttpPut(url);
		case DELETE:
			return new HttpDelete(url);
		case HEAD:
			return new HttpHead(url);
		case OPTIONS:
			return new HttpOptions(url);
		case TRACE:
			return new HttpTrace(url);
		default:
			throw new RuntimeException("Cannot create HttpMethod for " + method);
		}
	}
	
	private static void addRequestHeaders(HttpRequest httpRequest, Response response) {
		Request originalRequest = response.getOriginalRequest(); 
		for (String key: originalRequest.getAllHeaderKeys()) {
			if (!key.equals("Content-Length")) {
				String value = originalRequest.getHeader(key);
				httpRequest.addHeader(key, value);
			}
		}
	}
	
	private static void addBodyIfPostOrPut(HttpRequest httpRequest, Response response) throws UnsupportedEncodingException {
		Request originalRequest = response.getOriginalRequest();
		if (originalRequest.getMethod() == POST || originalRequest.getMethod() == PUT) {
			HttpEntityEnclosingRequest requestWithEntity = (HttpEntityEnclosingRequest) httpRequest;
			Optional<ContentTypeHeader> optionalContentType = ContentTypeHeader.getFrom(originalRequest);
			String body = originalRequest.getBodyAsString();
			
			if (optionalContentType.isPresent()) {
				ContentTypeHeader header = optionalContentType.get();
				requestWithEntity.setEntity(new StringEntity(body,
						header.mimeTypePart(),
						header.encodingPart().isPresent() ? header.encodingPart().get() : "utf-8"));
			} else {
				requestWithEntity.setEntity(new StringEntity(body,
						"text/plain",
						"utf-8"));
			}
		}
	}

}
