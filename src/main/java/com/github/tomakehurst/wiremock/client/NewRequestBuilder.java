package com.github.tomakehurst.wiremock.client;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.mapping.NewRequest;

public class NewRequestBuilder {

	private String host;
	private Integer port;
	private String bodyContent;
	private List<HttpHeader> headers = newArrayList();
	private Integer fixedDelayMilliseconds;
	private String url;
	private RequestMethod method;
	private String echoFieldName;

	public NewRequestBuilder(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	public NewRequestBuilder toUrl(String url) {
		this.url = url;
		return this;
	}

	public NewRequestBuilder withHeader(String key, String value) {
		headers.add(new HttpHeader(key, value));
		return this;
	}

	public NewRequestBuilder withBody(String body) {
		this.bodyContent = body; // .getBytes(Charset.forName(UTF_8.name()));
		return this;
	}

	public NewRequestBuilder withMethod(RequestMethod method) {
		this.method = method;
		return this;
	}

	public NewRequestBuilder withDelay(int delay) {
		this.fixedDelayMilliseconds = delay;
		return this;
	}

	/**
	 * If request received has this filed, new request sent from the server will
	 * contain this field.
	 *
	 * Request sent by client needs to be in JSON format to support this
	 * feature.
	 *
	 * @param field
	 * @return
	 */
	public NewRequestBuilder setEchoField(String field) {
		this.echoFieldName = field;
		return this;
	}

	public NewRequest build() {
		NewRequest request = new NewRequest(host, port, url, method, new HttpHeaders(headers), bodyContent, getDelay());
		request.setEchoFieldName(echoFieldName);
		return request;
	}

	private Integer getDelay() {
		if (fixedDelayMilliseconds == null) {
			return new Integer(500);
		} else {
			return fixedDelayMilliseconds;
		}
	}
}
