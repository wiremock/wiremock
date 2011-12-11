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
package com.tomakehurst.wiremock.mapping;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.tomakehurst.wiremock.http.Fault;
import com.tomakehurst.wiremock.http.HttpHeaders;

@JsonSerialize(include=Inclusion.NON_NULL)
public class ResponseDefinition {

	private int status;
	private String body;
	private String bodyFileName;
	private HttpHeaders headers;
	private Integer fixedDelayMilliseconds;
	private String proxyBaseUrl;
	private Fault fault;
	
	private boolean wasConfigured = true;
	private Request originalRequest;
	
	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public ResponseDefinition(int statusCode, String bodyContent) {
		this.status = statusCode;
		this.body = bodyContent;
	}
	
	public ResponseDefinition() {
		this.status = HTTP_OK;
	}

	public static ResponseDefinition notFound() {
		return new ResponseDefinition(HTTP_NOT_FOUND, null);
	}
	
	public static ResponseDefinition ok() {
		return new ResponseDefinition(HTTP_OK, null);
	}
	
	public static ResponseDefinition created() {
		return new ResponseDefinition(HTTP_CREATED, null);
	}
	
	public static ResponseDefinition notConfigured() {
	    ResponseDefinition response = new ResponseDefinition(HTTP_NOT_FOUND, null);
	    response.wasConfigured = false;
	    return response;
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getBody() {
		return body;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public void addHeader(String key, String value) {
		if (headers == null) {
			headers = new HttpHeaders();
		}
		
		headers.put(key, value);
	}
	
	public void setFixedDelayMilliseconds(Integer fixedDelayMilliseconds) {
	    this.fixedDelayMilliseconds = fixedDelayMilliseconds;
	}

	public String getBodyFileName() {
		return bodyFileName;
	}

	public void setBodyFileName(String bodyFileName) {
		this.bodyFileName = bodyFileName;
	}
	
	public boolean wasConfigured() {
        return wasConfigured;
    }

    public Integer getFixedDelayMilliseconds() {
        return fixedDelayMilliseconds;
    }

	public String getProxyBaseUrl() {
		return proxyBaseUrl;
	}

	public void setProxyBaseUrl(String proxyBaseUrl) {
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
	public boolean isProxyResponse() {
		return proxyBaseUrl != null;
	}

	public Request getOriginalRequest() {
		return originalRequest;
	}

	public void setOriginalRequest(Request originalRequest) {
		this.originalRequest = originalRequest;
	}

	public Fault getFault() {
		return fault;
	}

	public void setFault(Fault fault) {
		this.fault = fault;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result
				+ ((bodyFileName == null) ? 0 : bodyFileName.hashCode());
		result = prime * result + ((fault == null) ? 0 : fault.hashCode());
		result = prime
				* result
				+ ((fixedDelayMilliseconds == null) ? 0
						: fixedDelayMilliseconds.hashCode());
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result
				+ ((originalRequest == null) ? 0 : originalRequest.hashCode());
		result = prime * result
				+ ((proxyBaseUrl == null) ? 0 : proxyBaseUrl.hashCode());
		result = prime * result + status;
		result = prime * result + (wasConfigured ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ResponseDefinition other = (ResponseDefinition) obj;
		if (body == null) {
			if (other.body != null) {
				return false;
			}
		} else if (!body.equals(other.body)) {
			return false;
		}
		if (bodyFileName == null) {
			if (other.bodyFileName != null) {
				return false;
			}
		} else if (!bodyFileName.equals(other.bodyFileName)) {
			return false;
		}
		if (fault != other.fault) {
			return false;
		}
		if (fixedDelayMilliseconds == null) {
			if (other.fixedDelayMilliseconds != null) {
				return false;
			}
		} else if (!fixedDelayMilliseconds.equals(other.fixedDelayMilliseconds)) {
			return false;
		}
		if (headers == null) {
			if (other.headers != null) {
				return false;
			}
		} else if (!headers.equals(other.headers)) {
			return false;
		}
		if (originalRequest == null) {
			if (other.originalRequest != null) {
				return false;
			}
		} else if (!originalRequest.equals(other.originalRequest)) {
			return false;
		}
		if (proxyBaseUrl == null) {
			if (other.proxyBaseUrl != null) {
				return false;
			}
		} else if (!proxyBaseUrl.equals(other.proxyBaseUrl)) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		if (wasConfigured != other.wasConfigured) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return JsonMappingBinder.write(this);
	}
	
}
