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

public class CallbackRequestDispatcher {
	private static final Logger LOGGER = Logger.getLogger(CallbackRequestDispatcher.class);

	public static void dispatch(final CallbackRequest request, final Request originalRequest) {

		Thread dt = new Thread(new CallbackRequestSender(request, originalRequest));
		dt.start();

	}

	private static final class CallbackRequestSender implements Runnable {
		private final CallbackRequest newRequest;
		private final Request originalRequest;

		private CallbackRequestSender(CallbackRequest newRequest, Request originalRequest) {
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
				createHttpMethod(final CallbackRequest request, String absouteUrl) throws UnsupportedEncodingException {

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
