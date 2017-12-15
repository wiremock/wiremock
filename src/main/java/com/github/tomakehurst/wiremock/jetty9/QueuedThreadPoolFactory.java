package com.github.tomakehurst.wiremock.jetty9;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

public class QueuedThreadPoolFactory implements ThreadPoolFactory {

    @Override
    public ThreadPool buildThreadPool(Options options) {
        return new QueuedThreadPool(options.containerThreads());
    }
}
