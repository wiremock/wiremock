package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class QueuedThreadPoolAcceptanceTest extends AcceptanceTestBase {

    @BeforeClass
    public static void setupServer() {
        setupServer(new WireMockConfiguration().threadPoolFactory(new InstrumentedThreadPoolFactory()));
    }

    @Test
    public void serverUseCustomInstrumentedQueuedThreadPool() {
        assertThat(InstrumentedQueuedThreadPool.flag, is(true));
    }

    public static class InstrumentedQueuedThreadPool extends QueuedThreadPool {
        public static boolean flag = false;

        public InstrumentedQueuedThreadPool(int maxThreads) {
            this(maxThreads, 8);
        }

        public InstrumentedQueuedThreadPool(
                int maxThreads,
                int minThreads) {
            this(maxThreads, minThreads, 60000);
        }

        public InstrumentedQueuedThreadPool(
                int maxThreads,
                int minThreads,
                int idleTimeout) {
            super(maxThreads, minThreads, idleTimeout, null);
        }

        @Override
        protected void doStart() throws Exception {
            super.doStart();
            flag = true;
        }
    }

    public static class InstrumentedThreadPoolFactory implements ThreadPoolFactory {
        @Override
        public ThreadPool buildThreadPool(Options options) {
            return new InstrumentedQueuedThreadPool(options.containerThreads());
        }
    }
}
