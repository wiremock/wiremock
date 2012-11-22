package com.github.tomakehurst.wiremock.mapping;

import static com.github.tomakehurst.wiremock.client.HttpClientUtils.getEntityAsByteArrayAndCloseStream;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.RequestMethod;

public class NewRequestDispatcher {
	private static final Logger LOGGER = Logger
			.getLogger(NewRequestDispatcher.class);

	public static void dispatch(final NewRequest request) {
		Thread dt = new Thread(new NewRequestSender(request));
		dt.start();

	}

	private static final class NewRequestSender implements Runnable {
		private final NewRequest request;

		private NewRequestSender(NewRequest request) {
			this.request = request;
		}

		@Override
		public void run() {
			Object lock = new Object();

			synchronized (lock) {
				try {
					lock.wait(request.getFixedDelayMilliseconds());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			try {
				HttpClient client = HttpClientFactory.createClient();
				String absouteUrl = "http://" + request.getHost() + ":"
						+ request.getPort() + request.getUrl();

				HttpRequestBase httpRequest = createHttpMethod(request,
						absouteUrl);

				HttpResponse httpResponse = client.execute(httpRequest);
				getEntityAsByteArrayAndCloseStream(httpResponse);

				LOGGER.info("Response received["
						+ httpResponse.getStatusLine().getStatusCode() + "]");
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}

		}

		private HttpRequestBase createHttpMethod(final NewRequest request,
				String absouteUrl) throws UnsupportedEncodingException {

			if (RequestMethod.GET == request.getMethod()) {
				HttpGet httpRequest = new HttpGet(absouteUrl);
				return httpRequest;
			} else {
				HttpPost httpRequest = new HttpPost(absouteUrl);
				httpRequest.setEntity(new StringEntity(request.getBody()));
				return httpRequest;
			}

		}
	}

}
