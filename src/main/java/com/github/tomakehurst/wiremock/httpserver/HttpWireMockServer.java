package com.github.tomakehurst.wiremock.httpserver;

import static com.github.tomakehurst.wiremock.WireMockApp.ADMIN_CONTEXT_ROOT;
import static java.lang.System.nanoTime;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.AbstractWireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Timer;
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
			long start = nanoTime();

			httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
			Executor executor = new ThreadPoolExecutor(50, 50, 50, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			httpServer.setExecutor(executor);
			httpServer.createContext(ADMIN_CONTEXT_ROOT, new DispatchingHandler(wireMockApp.getAdminRequestHandler(), notifier));
			httpServer.createContext("/", new DispatchingHandler(wireMockApp.getMockServiceRequestHandler(), notifier));
			httpServer.start();
			
			System.out.println(String.format("HttpServer.create(): %sms", Timer.millisecondsFrom(start)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		httpServer.stop(1);
	}
}
