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

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.util.concurrent.TimeUnit;

import static org.apache.http.client.params.ClientPNames.HANDLE_REDIRECTS;

public class HttpClientFactory {
	
	public static HttpClient createClient(int maxConnections, int timeoutMilliseconds) {
		ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager();
		connectionManager.setMaxTotal(maxConnections);
		connectionManager.setDefaultMaxPerRoute(maxConnections);
		HttpClient client = new DefaultHttpClient(connectionManager);
        HttpParams params = client.getParams();
        params.setParameter(HANDLE_REDIRECTS, false);
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
