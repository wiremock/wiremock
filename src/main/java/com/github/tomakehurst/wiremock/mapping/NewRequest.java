package com.github.tomakehurst.wiremock.mapping;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.RequestMethod;

@JsonSerialize(include = Inclusion.NON_NULL)
public class NewRequest {

	private String host;
	private Integer port;
	private String url;
	private RequestMethod method;
	private HttpHeaders headers;
	private String body;
	private Integer fixedDelayMilliseconds;
	private String echoFieldName;

	public NewRequest() {
		super();
	}

	public NewRequest(String host, Integer port, String url,
			RequestMethod method, HttpHeaders headers, String body,
			Integer fixedDelayMilliseconds) {
		super();
		this.host = host;
		this.port = port;
		this.url = url;
		this.method = method;
		this.headers = headers;
		this.body = body;
		this.fixedDelayMilliseconds = fixedDelayMilliseconds;
	}

	public String getUrl() {
		return url;
	}

	public RequestMethod getMethod() {
		return method;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public String getBody() {
		return body;
	}

	public Integer getFixedDelayMilliseconds() {
		return fixedDelayMilliseconds;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setMethod(RequestMethod method) {
		this.method = method;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setFixedDelayMilliseconds(Integer fixedDelayMilliseconds) {
		this.fixedDelayMilliseconds = fixedDelayMilliseconds;
	}

	public String getEchoFieldName() {
		return echoFieldName;
	}

	public void setEchoFieldName(String echoFieldName) {
		this.echoFieldName = echoFieldName;
	}

	@Override
	public String toString() {
		return "NewRequest [host=" + host + ", port=" + port + ", url=" + url
				+ ", method=" + method + ", headers=" + headers + ", body="
				+ body + ", fixedDelayMilliseconds=" + fixedDelayMilliseconds
				+ ", echoFieldName=" + echoFieldName + "]";
	}

}
