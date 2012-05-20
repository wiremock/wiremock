package com.github.tomakehurst.wiremock.httpserver;

import static com.github.tomakehurst.wiremock.WireMockApp.ADMIN_CONTEXT_ROOT;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.AbstractWireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.sun.net.httpserver.HttpServer;

public class HttpWireMockServer extends AbstractWireMockServer {
	
	private HttpServer httpServer;

	public HttpWireMockServer() {
		super();
	}

	public HttpWireMockServer(int port, FileSource fileSource, boolean enableBrowserProxying) {
		super(port, fileSource, enableBrowserProxying);
	}

	public HttpWireMockServer(int port) {
		super(port);
	}

	@Override
	public void start() {
		try {
			httpServer = HttpServer.create(new InetSocketAddress(port), 1000);
			Executor executor = new ThreadPoolExecutor(1, 50, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			httpServer.setExecutor(executor);
			httpServer.createContext(ADMIN_CONTEXT_ROOT, new DispatchingHandler(wireMockApp.getAdminRequestHandler(), notifier));
			httpServer.createContext("/", new DispatchingHandler(wireMockApp.getMockServiceRequestHandler(), notifier));
			httpServer.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		httpServer.stop(1);
	}
	

	
}
