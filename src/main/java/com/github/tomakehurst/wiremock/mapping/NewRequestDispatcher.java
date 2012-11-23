package com.github.tomakehurst.wiremock.mapping;

import static com.github.tomakehurst.wiremock.client.HttpClientUtils.getEntityAsByteArrayAndCloseStream;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.RequestMethod;

public class NewRequestDispatcher {
	private static final Logger LOGGER = Logger.getLogger(NewRequestDispatcher.class);

	public static void dispatch(final NewRequest request, final Request originalRequest) {

		Thread dt = new Thread(new NewRequestSender(request, originalRequest));
		dt.start();

	}

	private static final class NewRequestSender implements Runnable {
		private final NewRequest newRequest;
		private final Request originalRequest;

		private NewRequestSender(NewRequest newRequest, Request originalRequest) {
			this.newRequest = newRequest;
			this.originalRequest = originalRequest;
		}

		@Override
		public void run() {
			Object lock = new Object();

			synchronized (lock) {
				try {
					lock.wait(newRequest.getFixedDelayMilliseconds());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			try {
				HttpClient client = HttpClientFactory.createClient();
				String absouteUrl = "http://" + newRequest.getHost() + ":" + newRequest.getPort() + newRequest.getUrl();

				HttpRequestBase httpRequest = createHttpMethod(newRequest, absouteUrl);

				for (HttpHeader header : newRequest.getHeaders().all()) {
					httpRequest.addHeader(header.key(), header.firstValue());
				}

				HttpResponse httpResponse = client.execute(httpRequest);
				getEntityAsByteArrayAndCloseStream(httpResponse);

				LOGGER.info("Response received[" + httpResponse.getStatusLine().getStatusCode() + "]");
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}

		}

		private HttpRequestBase
				createHttpMethod(final NewRequest request, String absouteUrl) throws UnsupportedEncodingException {

			if (RequestMethod.GET == request.getMethod()) {
				HttpGet httpRequest = new HttpGet(absouteUrl);
				return httpRequest;
			} else {
				HttpPost httpRequest = new HttpPost(absouteUrl);
				httpRequest.setEntity(new StringEntity(updatedContentWithEchoFields()));
				return httpRequest;
			}

		}

		private String updatedContentWithEchoFields() {
			if (newRequest.getEchoFieldName() != null) {
				try {
					Map origiReqJsonMap = readJson(originalRequest.getBodyAsString());
					Map newReqJsonMap = readJson(newRequest.getBody());
					if (origiReqJsonMap.containsKey(newRequest.getEchoFieldName())) {
						newReqJsonMap.put(newRequest.getEchoFieldName(),
								origiReqJsonMap.get(newRequest.getEchoFieldName()));
					}
					return Json.write(newReqJsonMap);
				} catch (Exception e) {
					LOGGER.error("Error trying to include echo filed", e);
				}
			}
			return newRequest.getBody();

		}

		private Map readJson(String str) {
			try {
				return Json.read(str, Map.class);
			} catch (Exception e) {
				throw new RuntimeException("Error decoding json from[" + str + "]");
			}
		}
	}

}
