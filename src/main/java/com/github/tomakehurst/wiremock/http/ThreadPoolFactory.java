package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.core.Options;
import org.eclipse.jetty.util.thread.ThreadPool;

public interface ThreadPoolFactory {

    ThreadPool buildThreadPool(Options options);
}
