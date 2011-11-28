package com.tomakehurst.wiremock.servlet;

import static com.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.tomakehurst.wiremock.http.RequestMethod.PUT;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

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
import com.tomakehurst.wiremock.common.FileSource;
import com.tomakehurst.wiremock.common.TextFile;
import com.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.tomakehurst.wiremock.http.ContentTypeHeader;
import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.Request;
import com.tomakehurst.wiremock.mapping.Response;

public class MockServiceResponseRenderer implements ResponseRenderer {
	
	private final FileSource fileSource;
	private final GlobalSettingsHolder globalSettingsHolder;

	public MockServiceResponseRenderer(FileSource fileSource, GlobalSettingsHolder globalSettingsHolder) {
		this.fileSource = fileSource;
		this.globalSettingsHolder = globalSettingsHolder;
	}

	@Override
	public void render(Response response, HttpServletResponse httpServletResponse) {
	    addDelayIfSpecifiedIn(response);
	    
	    if (response.isProxyResponse()) {
	    	renderProxyResponse(response, httpServletResponse);
	    } else {
	    	renderResponseDirectly(response, httpServletResponse);
	    }
	}
	
	private void renderProxyResponse(Response response, HttpServletResponse httpServletResponse) {
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
	
	private void renderResponseDirectly(Response response, HttpServletResponse httpServletResponse) {
		httpServletResponse.setStatus(response.getStatus());
		addHeaders(response, httpServletResponse);
			
		if (response.specifiesBodyFile()) {
			TextFile bodyFile = fileSource.getTextFileNamed(response.getBodyFileName());
			writeAndConvertExceptions(httpServletResponse, bodyFile.readContents());
		} else if (response.specifiesBodyContent()) {
			writeAndConvertExceptions(httpServletResponse, response.getBody());
		}
	}
	
	private void writeAndConvertExceptions(HttpServletResponse httpServletResponse, String content) {
		try {
			httpServletResponse.getWriter().write(content);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private void addHeaders(Response response,
			HttpServletResponse httpServletResponse) {
		HttpHeaders headers = response.getHeaders();
		if (headers != null) {
			for (Map.Entry<String, String> header: headers.entrySet()) {
				httpServletResponse.addHeader(header.getKey(), header.getValue());
			}
		}
	}

    private void addDelayIfSpecifiedIn(Response response) {
    	Optional<Integer> optionalDelay = getDelayFromResponseOrGlobalSetting(response);
        if (optionalDelay.isPresent()) {
	        try {
	            Thread.sleep(optionalDelay.get());
	        } catch (InterruptedException e) {
	            throw new RuntimeException(e);
	        }
	    }
    }
    
    private Optional<Integer> getDelayFromResponseOrGlobalSetting(Response response) {
    	Integer delay = response.getFixedDelayMilliseconds() != null ?
    			response.getFixedDelayMilliseconds() :
    			globalSettingsHolder.get().getFixedDelay();
    	
    	return Optional.fromNullable(delay);
    }
}
