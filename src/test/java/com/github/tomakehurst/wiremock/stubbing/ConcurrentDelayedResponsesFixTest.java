package com.github.tomakehurst.wiremock.stubbing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestServer;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.After;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.WireMockServer.FILES_ROOT;
import static com.github.tomakehurst.wiremock.WireMockServer.MAPPINGS_ROOT;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ConcurrentDelayedResponsesFixTest {
    private static final File ROOT = new File(".");
    private static final String REQUEST_URL = "/proxied/resource?param=value";
    private static final String RESPONSE_BODY = "Lorem ipsum dolor sit amet, consectetur adipiscing elit";

    private static final int  POOLED_CONNECTIONS = 32;    
    private static final int  DEFAULT_CONCURRENT_REQUESTS = 2;  // the default HttpClient connection pool size for identical requests is 2
    private static final int  LOTS_OF_CONCURRENT_REQUESTS = 16; // should be more than the default HttpClient connection pool size
    private static final int  SOCKET_DELAY = 5000;              // milliseconds
    private static final long MIN_DELAY = SOCKET_DELAY - 250;
    private static final long MAX_DELAY = SOCKET_DELAY + 250;
    private static final long VARIANCE = 10;

    private FileSource mappingsFileSource;
    private FileSource filesFileSource;

    private String targetServiceBaseUrl;
    private WireMockServer targetService;
    private WireMock targetServiceAdmin;

    private WireMockServer proxyingService;
    private WireMock proxyingServiceAdmin;

    private WireMockTestClient testClient;
    private ExecutorService threadPool;

    private void init() {
        threadPool = Executors.newCachedThreadPool(new DaemonThreadFactory());
        mappingsFileSource = initFileSource(ROOT,MAPPINGS_ROOT);
        filesFileSource = initFileSource(ROOT,FILES_ROOT);

        targetService = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort());
        targetService.start();
        targetServiceAdmin = new WireMock("localhost", targetService.port());
        targetServiceBaseUrl = "http://localhost:" + targetService.port();
        
        targetServiceAdmin.register(get(urlEqualTo(REQUEST_URL))
                          .willReturn(aResponse()
                          .withStatus(200)
                          .withHeader("Content-Type", "text/plain")
                          .withBody(RESPONSE_BODY)
                          .withFixedDelay(SOCKET_DELAY)));

        WireMock.configureFor(targetService.port());
    }

    private void initWithDefaultConfig() {
        init();

        WireMockConfiguration proxyingServiceOptions = wireMockConfig();

        proxyingServiceOptions.dynamicPort();
        
        proxyingService = new WireMockServer(proxyingServiceOptions);
        proxyingService.enableRecordMappings(mappingsFileSource, filesFileSource);
        proxyingService.start();
        proxyingServiceAdmin = new WireMock(proxyingService.port());

        testClient = new WireMockTestClient(proxyingService.port());
    }

    private void initWithConcurrentConfig() {
        init();

        WireMockConfiguration proxyingServiceOptions = wireMockConfig();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

        proxyingServiceOptions.dynamicPort();
        cm.setDefaultMaxPerRoute(POOLED_CONNECTIONS);
        
        proxyingService = new WireMockTestServer(proxyingServiceOptions,cm);
        proxyingService.enableRecordMappings(mappingsFileSource, filesFileSource);
        proxyingService.start();
        proxyingServiceAdmin = new WireMock(proxyingService.port());
        
        testClient = new WireMockTestClient(proxyingService.port());
    }
    
    @After
    public void stop() {
        targetService.stop();
        proxyingService.stop();
        threadPool.shutdownNow();
    }
    
    /** 
     * Executes a 'multiple concurrent identical request test' with the stock WireMockServer, which 
     * is limited to two 'simultaneous' connections for the same request.
     * 
     * Ref. http://stackoverflow.com/questions/29534990/httpclient-unable-to-send-more-than-two-requests
     */
    @Test
    public void successfullyRecordStubsForDefaultConcurrentIdenticalRequests() throws Exception {
        initWithDefaultConfig();

        executeConcurrentIdenticalRequestsTest(DEFAULT_CONCURRENT_REQUESTS);
    }

    /** 
     * Executes a 'multiple concurrent identical request test' with a test WireMockServer which 
     * uses a PoolHttpConnectionManager to provide more than 'simultaneous' connections for 
     * the same request.
     */
    @Test
    public void successfullyRecordStubsFor16ConcurrentIdenticalRequests() throws Exception {
        initWithConcurrentConfig();

        executeConcurrentIdenticalRequestsTest(LOTS_OF_CONCURRENT_REQUESTS);
    }

    
    private void executeConcurrentIdenticalRequestsTest(int concurrentRequests) throws Exception {
        // ... set up proxy stub
        
        proxyingServiceAdmin.register(any(urlEqualTo(REQUEST_URL)).atPriority(10)
                .willReturn(aResponse()
                .proxiedFrom(targetServiceBaseUrl)));

        // ... set up concurrent requests
        
        List<Runnable> tasks = new ArrayList<Runnable>();
        Long[] duration = new Long[concurrentRequests];
        AtomicLong start = new AtomicLong();
        
        Arrays.fill(duration, 0L);
        
        for (int i=0; i<concurrentRequests; i++) {
            final int index = i;
            
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    try {
                        WireMockResponse response = testClient.get(REQUEST_URL);
                        long dt = System.currentTimeMillis() - start.get();
                        duration[index] = dt;
                        
                        assertThat("Invalid response code",response.statusCode(), is(200));
                        assertThat("Invalid response body",response.content(), is(RESPONSE_BODY));
                    } catch(Exception x) {
                        fail(x.toString());
                    }
                }
            });
        }

        // ... run test
        
        assertThat("Stub directory 'mappings' is not empty", countFilesInDir(new File(ROOT,MAPPINGS_ROOT)),is(0));
        assertThat("Stub directory '__files' is not empty", countFilesInDir(new File(ROOT,FILES_ROOT)),is(0));

        start.set(System.currentTimeMillis());
        
        for (Runnable task: tasks) {
            threadPool.submit(task);
        }
        
        threadPool.shutdown();
        threadPool.awaitTermination(30,TimeUnit.SECONDS);
        
        // ... verify recorded stubs
        
        assertThat("Stub directory 'mappings' does not contain the recorded request",countFilesInDir(new File(ROOT,MAPPINGS_ROOT)),greaterThan(0));
        assertThat("Stub directory '__files' does not contain the recorded response",countFilesInDir(new File(ROOT,FILES_ROOT)),greaterThan(0));
        assertThat("Stub directory 'mappings' contains more than the recorded request",countFilesInDir(new File(ROOT,MAPPINGS_ROOT)),is(1));
        assertThat("Stub directory '__files' contains more than the recorded response",countFilesInDir(new File(ROOT,FILES_ROOT)),is(1));
        
        // ... verify test concurrency
        
        assertThat("Invalid test",Arrays.asList(duration), everyItem(greaterThanOrEqualTo(MIN_DELAY)));
        assertThat("Invalid test",Arrays.asList(duration), everyItem(lessThanOrEqualTo(MAX_DELAY)));
        assertThat("Insufficiently concurrent",variance(duration), is(closeTo(0.0,VARIANCE)));
    }

    // UTILITY FUNCTIONS
    
    private static FileSource initFileSource(File root,String directory) {
        clear(new File(root,directory));

        FileSource dir = new SingleRootFileSource(root.getPath());
        dir.createIfNecessary();
        
        FileSource fileSource = dir.child(directory);
        fileSource.createIfNecessary();
     
        clear(new File(root,directory));
        
        return fileSource;
    }

    private static void clear(File dir) {
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file: files) {
                if (file.isDirectory()) {
                    clear(file);
                }
            
                file.delete();
            }
        }
    }

    private static int countFilesInDir(File dir) {
        int count = 0;
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file: files) {
                if (file.isDirectory()) {
                    count += countFilesInDir(file);
                } else {
                    count++;
                }
            }
        }
        
        return count;
    }

    private static double mean(Long[] values) {
        double total = 0.0;
        
        for (long v: values) {
            total += v;
        }
        
        return total/values.length;
      }
    
    private static double variance(Long[] values) {
        double mean = mean(values);
        double sum = 0.0;
        
        for (long v: values) {
          sum  += (mean - v) * (mean - v);
        }
        
        return sum/values.length;
      }

    private static class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            
            thread.setDaemon(true);
            
            return thread;
        }
    }
}
