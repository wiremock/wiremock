package com.github.tomakehurst.wiremock.client;

import static com.google.common.base.Charsets.UTF_8;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public class HttpClientUtils {

	public static String getEntityAsStringAndCloseStream(HttpResponse httpResponse) {
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			try {
				String content = EntityUtils.toString(entity, UTF_8.name());
				entity.getContent().close();
				return content;
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		
		return null;
	}
	
	public static byte[] getEntityAsByteArrayAndCloseStream(HttpResponse httpResponse) {
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			try {
				byte[] content = EntityUtils.toByteArray(entity);
				entity.getContent().close();
				return content;
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		
		return null;
	}
}
