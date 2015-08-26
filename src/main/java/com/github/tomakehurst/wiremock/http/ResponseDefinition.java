/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;

import java.util.List;
import java.util.Objects;

import static java.net.HttpURLConnection.*;

@JsonSerialize(include=Inclusion.NON_NULL)
public class ResponseDefinition {

	private int status;
	private Body body = Body.none();
	private String bodyFileName;
	private HttpHeaders headers;
	private HttpHeaders additionalProxyRequestHeaders;
	private Integer fixedDelayMilliseconds;
	private String proxyBaseUrl;
	private String browserProxyUrl;
	private Fault fault;

	private boolean wasConfigured = true;
	private Request originalRequest;
	private List<String> transformers;

	@JsonCreator
	public ResponseDefinition(@JsonProperty("status") int status,
							  @JsonProperty("body") String body,
							  @JsonProperty("jsonBody") JsonNode jsonBody,
							  @JsonProperty("base64Body") String base64Body,
							  @JsonProperty("bodyFileName") String bodyFileName,
							  @JsonProperty("headers") HttpHeaders headers,
							  @JsonProperty("additionalProxyRequestHeaders") HttpHeaders additionalProxyRequestHeaders,
							  @JsonProperty("fixedDelayMilliseconds") Integer fixedDelayMilliseconds,
							  @JsonProperty("proxyBaseUrl") String proxyBaseUrl,
							  @JsonProperty("fault") Fault fault,
							  @JsonProperty("transformers") List<String> transformers) {
		this(status, bodyFileName, headers, additionalProxyRequestHeaders, fixedDelayMilliseconds, proxyBaseUrl, fault, transformers);
		this.body = Body.fromOneOf(null, body, jsonBody, base64Body);
	}

	public ResponseDefinition(int status,
							  byte[] body,
							  JsonNode jsonBody,
							  String base64Body,
							  String bodyFileName,
							  HttpHeaders headers,
							  HttpHeaders additionalProxyRequestHeaders,
							  Integer fixedDelayMilliseconds,
							  String proxyBaseUrl,
							  Fault fault,
							  List<String> transformers) {
		this(status, bodyFileName, headers, additionalProxyRequestHeaders, fixedDelayMilliseconds, proxyBaseUrl, fault, transformers);
		this.body = Body.fromOneOf(body, null, jsonBody, base64Body);
	}

	private ResponseDefinition(int status,
							   String bodyFileName,
							   HttpHeaders headers,
							   HttpHeaders additionalProxyRequestHeaders,
							   Integer fixedDelayMilliseconds,
							   String proxyBaseUrl,
							   Fault fault,
							   List<String> transformers) {
		this.status = status > 0 ? status : 200;

		this.bodyFileName = bodyFileName;

		this.headers = headers;
		this.additionalProxyRequestHeaders = additionalProxyRequestHeaders;
		this.fixedDelayMilliseconds = fixedDelayMilliseconds;
		this.proxyBaseUrl = proxyBaseUrl;
		this.fault = fault;
		this.transformers = transformers;
	}

	public static ResponseDefinition copyOf(ResponseDefinition original) {
		ResponseDefinition newResponseDef = new ResponseDefinition();
		newResponseDef.status = original.status;
		newResponseDef.body = original.body;
		newResponseDef.bodyFileName = original.bodyFileName;
		newResponseDef.headers = original.headers;
		newResponseDef.additionalProxyRequestHeaders = original.additionalProxyRequestHeaders;
		newResponseDef.fixedDelayMilliseconds = original.fixedDelayMilliseconds;
		newResponseDef.proxyBaseUrl = original.proxyBaseUrl;
		newResponseDef.fault = original.fault;
		newResponseDef.wasConfigured = original.wasConfigured;
		newResponseDef.transformers = original.transformers;
		return newResponseDef;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(final HttpHeaders headers) {
		this.headers = headers;
	}

	public HttpHeaders getAdditionalProxyRequestHeaders() {
		return additionalProxyRequestHeaders;
	}

	public void setAdditionalProxyRequestHeaders(final HttpHeaders additionalProxyRequestHeaders) {
		this.additionalProxyRequestHeaders = additionalProxyRequestHeaders;
	}

	public ResponseDefinition(final int statusCode, final String bodyContent) {
		this.status = statusCode;
		this.body = Body.fromString(bodyContent);
	}

	public ResponseDefinition(final int statusCode, final byte[] bodyContent) {
		this.status = statusCode;
		this.body = Body.fromBytes(bodyContent);
	}

	public ResponseDefinition() {
		this.status = HTTP_OK;
	}

	public static ResponseDefinition notFound() {
		return new ResponseDefinition(HTTP_NOT_FOUND, (byte[])null);
	}

	public static ResponseDefinition ok() {
		return new ResponseDefinition(HTTP_OK, (byte[])null);
	}

	public static ResponseDefinition created() {
		return new ResponseDefinition(HTTP_CREATED, (byte[])null);
	}

	public static ResponseDefinition redirectTo(String path) {
		return new ResponseDefinitionBuilder()
				.withHeader("Location", path)
				.withStatus(HTTP_MOVED_TEMP)
				.build();
	}

	public static ResponseDefinition notConfigured() {
		final ResponseDefinition response = new ResponseDefinition(HTTP_NOT_FOUND, (byte[])null);
		response.wasConfigured = false;
		return response;
	}

	public static ResponseDefinition browserProxy(Request originalRequest) {
		final ResponseDefinition response = new ResponseDefinition();
		response.browserProxyUrl = originalRequest.getAbsoluteUrl();
		return response;
	}

	public int getStatus() {
		return status;
	}

	public String getBody() {
		return !body.isBinary() ? body.asString() : null;
	}

	@JsonIgnore
	public byte[] getByteBody() {
		return body.asBytes();
	}

	public String getBase64Body() {
		return body.isBinary() ? body.asBase64() : null;
	}

	public void setBase64Body(String base64Body) {
		body = Body.fromOneOf(null, null, null, base64Body);
	}

	public void setJsonBody(JsonNode jsonBody) {
		body = Body.fromOneOf(null, null, jsonBody, null);
	}

	// Needs to be explicitly marked as a property, since an overloaded setter with the same
	// name is marked as ignored (see currently open JACKSON-783 bug)
	@JsonProperty
	public void setBody(final String body) {
		this.body = Body.fromString(body);
	}

	@JsonIgnore
	public void setBody(final byte[] body) {
		this.body = Body.fromBytes(body);
	}

	public void setStatus(final int status) {
		if (status == 0) {
			this.status = HTTP_OK;
		} else {
			this.status = status;
		}
	}

	public void setFixedDelayMilliseconds(final Integer fixedDelayMilliseconds) {
		this.fixedDelayMilliseconds = fixedDelayMilliseconds;
	}

	public String getBodyFileName() {
		return bodyFileName;
	}

	public void setBodyFileName(final String bodyFileName) {
		this.bodyFileName = bodyFileName;
	}

	public boolean wasConfigured() {
		return wasConfigured;
	}

	public Integer getFixedDelayMilliseconds() {
		return fixedDelayMilliseconds;
	}

	@JsonIgnore
	public String getProxyUrl() {
		if (browserProxyUrl != null) {
			return browserProxyUrl;
		}

		return proxyBaseUrl + originalRequest.getUrl();
	}

	public String getProxyBaseUrl() {
		return proxyBaseUrl;
	}

	public void setProxyBaseUrl(final String proxyBaseUrl) {
		this.proxyBaseUrl = proxyBaseUrl;
	}

	@JsonIgnore
	public boolean specifiesBodyFile() {
		return bodyFileName != null && body.isAbsent();
	}

	@JsonIgnore
	public boolean specifiesBodyContent() {
		return body.isPresent();
	}

	@JsonIgnore
	public boolean specifiesBinaryBodyContent() {
		return (body.isPresent() && body.isBinary());
	}

	@JsonIgnore
	public boolean isProxyResponse() {
		return browserProxyUrl != null || proxyBaseUrl != null;
	}

	public Request getOriginalRequest() {
		return originalRequest;
	}

	public void setOriginalRequest(final Request originalRequest) {
		this.originalRequest = originalRequest;
	}

	public Fault getFault() {
		return fault;
	}

	public void setFault(final Fault fault) {
		this.fault = fault;
	}

	public List<String> getTransformers() {
		return transformers;
	}

	public void setTransformers(List<String> transformers) {
		this.transformers = transformers;
	}

	public boolean hasTransformer(ResponseTransformer transformer) {
		return transformers != null && transformers.contains(transformer.name());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResponseDefinition that = (ResponseDefinition) o;
		return Objects.equals(status, that.status) &&
				Objects.equals(wasConfigured, that.wasConfigured) &&
				Objects.equals(body, that.body) &&
				Objects.equals(bodyFileName, that.bodyFileName) &&
				Objects.equals(headers, that.headers) &&
				Objects.equals(additionalProxyRequestHeaders, that.additionalProxyRequestHeaders) &&
				Objects.equals(fixedDelayMilliseconds, that.fixedDelayMilliseconds) &&
				Objects.equals(proxyBaseUrl, that.proxyBaseUrl) &&
				Objects.equals(browserProxyUrl, that.browserProxyUrl) &&
				Objects.equals(fault, that.fault) &&
				Objects.equals(originalRequest, that.originalRequest) &&
				Objects.equals(transformers, that.transformers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, body, bodyFileName, headers, additionalProxyRequestHeaders, fixedDelayMilliseconds, proxyBaseUrl, browserProxyUrl, fault, wasConfigured, originalRequest, transformers);
	}

	@Override
	public String toString() {
		return Json.write(this);
	}
}
