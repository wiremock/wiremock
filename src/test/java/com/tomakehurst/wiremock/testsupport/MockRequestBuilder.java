package com.tomakehurst.wiremock.testsupport;

import static com.tomakehurst.wiremock.http.RequestMethod.GET;

import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.Request;

public class MockRequestBuilder {

	private Mockery context;
	private String uri = "/";
	private RequestMethod method = GET;
	private HttpHeaders headers = new HttpHeaders();
	private String body = "";
	
	public MockRequestBuilder(Mockery context) {
		this.context = context;
	}
	
	public static MockRequestBuilder aRequest(Mockery context) {
		return new MockRequestBuilder(context);
	}

	public MockRequestBuilder withUri(String uri) {
		this.uri = uri;
		return this;
	}

	public MockRequestBuilder withMethod(RequestMethod method) {
		this.method = method;
		return this;
	}

	public MockRequestBuilder withHeader(String key, String value) {
		headers.put(key, value);
		return this;
	}
	
	public MockRequestBuilder withBody(String body) {
		this.body = body;
		return this;
	}
	
	public Request build() {
		final Request request = context.mock(Request.class);
		context.checking(new Expectations() {{
			allowing(request).getUri(); will(returnValue(uri));
			allowing(request).getMethod(); will(returnValue(method));
			for (Map.Entry<String, String> header: headers.entrySet()) {
				allowing(request).containsHeader(header.getKey()); will(returnValue(true));
				allowing(request).getHeader(header.getKey()); will(returnValue(header.getValue()));
			}
			allowing(request).containsHeader(with(any(String.class))); will(returnValue(false));
			allowing(request).getBodyAsString(); will(returnValue(body));
		}});
		
		return request;
	}
}
