package com.tomakehurst.wiremock.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpClientFactory {
	
	public static HttpClient createClient(int maxConnections, int timeoutMilliseconds) {
		ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager();
		connectionManager.setMaxTotal(maxConnections);
		connectionManager.setDefaultMaxPerRoute(maxConnections);
		HttpClient client = new DefaultHttpClient(connectionManager);
        HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, timeoutMilliseconds);
        HttpConnectionParams.setSoTimeout(params, timeoutMilliseconds);
        return client;
	}
	
	public static HttpClient createClient(int timeoutMilliseconds) {
		return createClient(50, timeoutMilliseconds);
	}
	
	public static HttpClient createClient() {
		return createClient(30000);
	}
	
}
