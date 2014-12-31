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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static java.net.HttpURLConnection.*;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

@JsonSerialize(include=Inclusion.NON_NULL)
public class ResponseDefinition {

	private int status;
	private byte[] body;
    private boolean isBinaryBody = false;
	private String bodyFileName;
	private HttpHeaders headers;
	private HttpHeaders additionalProxyRequestHeaders;
	private Integer fixedDelayMilliseconds;
	private String proxyBaseUrl;
	private String browserProxyUrl;
	private Fault fault;
	
	private boolean wasConfigured = true;
	private Request originalRequest;
	private List<String> responseTransformers;

	public static ResponseDefinition copyOf(ResponseDefinition original) {
	    ResponseDefinition newResponseDef = new ResponseDefinition();
	    newResponseDef.status = original.status;
	    newResponseDef.body = original.body;
        newResponseDef.isBinaryBody = original.isBinaryBody;
	    newResponseDef.bodyFileName = original.bodyFileName;
	    newResponseDef.headers = original.headers;
	    newResponseDef.additionalProxyRequestHeaders = original.additionalProxyRequestHeaders;
	    newResponseDef.fixedDelayMilliseconds = original.fixedDelayMilliseconds;
	    newResponseDef.proxyBaseUrl = original.proxyBaseUrl;
	    newResponseDef.fault = original.fault;
	    newResponseDef.wasConfigured = original.wasConfigured;
		newResponseDef.responseTransformers = original.responseTransformers;
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
		this.body = (bodyContent==null) ? null : bodyContent.getBytes(Charset.forName(UTF_8.name()));
	}

    public ResponseDefinition(final int statusCode, final byte[] bodyContent) {
        this.status = statusCode;
        this.body = bodyContent;
        isBinaryBody = true;
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
		return (!isBinaryBody && body!=null) ? new String(body,Charset.forName(UTF_8.name())) : null;
	}

    @JsonIgnore
    public byte[] getByteBody() {
        return body;
    }

    public String getBase64Body() {
        if (isBinaryBody && body != null) {
            return printBase64Binary(body);
        }

        return null;
    }

    public void setBase64Body(String base64Body) {
        isBinaryBody = true;
        body = parseBase64Binary(base64Body);
    }

    // Needs to be explicitly marked as a property, since an overloaded setter with the same
    // name is marked as ignored (see currently open JACKSON-783 bug)
    @JsonProperty
	public void setBody(final String body) {
		this.body = (body!=null) ? body.getBytes(Charset.forName(UTF_8.name())) : null;
        isBinaryBody = false;
	}

    @JsonIgnore
    public void setBody(final byte[] body) {
        this.body = body;
        isBinaryBody = true;
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
		return bodyFileName != null;
	}
	
	@JsonIgnore
	public boolean specifiesBodyContent() {
		return body != null;
	}

    @JsonIgnore
    public boolean specifiesBinaryBodyContent() {
        return (body!=null && isBinaryBody);
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

	public List<String> getResponseTransformers() {
		return responseTransformers;
	}

	public void setResponseTransformers(List<String> responseTransformers) {
		this.responseTransformers = responseTransformers;
	}

	public boolean hasTransformer(ResponseTransformer transformer) {
		return responseTransformers != null && responseTransformers.contains(transformer.name());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ResponseDefinition that = (ResponseDefinition) o;

		if (isBinaryBody != that.isBinaryBody) return false;
		if (status != that.status) return false;
		if (wasConfigured != that.wasConfigured) return false;
		if (additionalProxyRequestHeaders != null ? !additionalProxyRequestHeaders.equals(that.additionalProxyRequestHeaders) : that.additionalProxyRequestHeaders != null)
			return false;
		if (!Arrays.equals(body, that.body)) return false;
		if (bodyFileName != null ? !bodyFileName.equals(that.bodyFileName) : that.bodyFileName != null) return false;
		if (browserProxyUrl != null ? !browserProxyUrl.equals(that.browserProxyUrl) : that.browserProxyUrl != null)
			return false;
		if (fault != that.fault) return false;
		if (fixedDelayMilliseconds != null ? !fixedDelayMilliseconds.equals(that.fixedDelayMilliseconds) : that.fixedDelayMilliseconds != null)
			return false;
		if (headers != null ? !headers.equals(that.headers) : that.headers != null) return false;
		if (originalRequest != null ? !originalRequest.equals(that.originalRequest) : that.originalRequest != null)
			return false;
		if (proxyBaseUrl != null ? !proxyBaseUrl.equals(that.proxyBaseUrl) : that.proxyBaseUrl != null) return false;
		if (responseTransformers != null ? !responseTransformers.equals(that.responseTransformers) : that.responseTransformers != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = status;
		result = 31 * result + (body != null ? Arrays.hashCode(body) : 0);
		result = 31 * result + (isBinaryBody ? 1 : 0);
		result = 31 * result + (bodyFileName != null ? bodyFileName.hashCode() : 0);
		result = 31 * result + (headers != null ? headers.hashCode() : 0);
		result = 31 * result + (additionalProxyRequestHeaders != null ? additionalProxyRequestHeaders.hashCode() : 0);
		result = 31 * result + (fixedDelayMilliseconds != null ? fixedDelayMilliseconds.hashCode() : 0);
		result = 31 * result + (proxyBaseUrl != null ? proxyBaseUrl.hashCode() : 0);
		result = 31 * result + (browserProxyUrl != null ? browserProxyUrl.hashCode() : 0);
		result = 31 * result + (fault != null ? fault.hashCode() : 0);
		result = 31 * result + (wasConfigured ? 1 : 0);
		result = 31 * result + (originalRequest != null ? originalRequest.hashCode() : 0);
		result = 31 * result + (responseTransformers != null ? responseTransformers.hashCode() : 0);
		return result;
	}

	private static boolean byteBodyEquals(byte[] expecteds, byte[] actuals)
    {
        if (expecteds == actuals) return true;
        if (expecteds == null) return false;
        if (actuals == null) return false;

        int actualsLength= actuals.length;
        int expectedsLength= expecteds.length;
        if (actualsLength != expectedsLength)
            return false;

        for (int i= 0; i < expectedsLength; i++) {
            byte  expected= expecteds[i];
            byte  actual= actuals[i];
            if(expected!=actual) return false;
        }
        return true;
    }

    @Override
	public String toString() {
		return Json.write(this);
	}
}
