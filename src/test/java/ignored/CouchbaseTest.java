package ignored;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.admin.AdminRoutes;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.jetty9.JettyHttpServerFactory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.standalone.WireMockServerRunner;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;


/**
 * Created by keithstouffer on 3/7/17.
 */
public class CouchbaseTest {

    @Test
    public void testCouchbase() throws IOException {

        WireMockServer wms = new WireMockServer(WireMockConfiguration.options().port(8442));
        wms.stubFor(WireMock.get(WireMock.urlMatching(".*")).willReturn(WireMock.aResponse().proxiedFrom("http://localhost:8091/")));
        wms.start();

        CloseableHttpClient httpClient = HttpClientFactory.createClient();
        HttpGet httpGetPoolsWaitChange = new HttpGet("http://localhost:8442/pools/default?waitChange=0");
        System.out.println("Making first request to http://localhost:8442/pools/default?waitChange=0\n");
        CloseableHttpResponse response1 = httpClient.execute(httpGetPoolsWaitChange);
        System.out.println("First Request, Response Status: " + response1.getStatusLine());
        System.out.println("First Request, Headers:");
        for( Header h : response1.getAllHeaders()) System.out.println("    " + h.getName() + ": " + h.getValue());

        //putting a Thread.sleep(2000) here causes a success

        HttpGet httpGetPools = new HttpGet("http://localhost:8442/pools");
        System.out.println("First Request Completed.");
        System.out.println("Making second request to http://localhost:8442/pools\n");
        CloseableHttpResponse response2 = httpClient.execute(httpGetPools);
        System.out.println("Second Request, Response Status: " + response2.getStatusLine());
        System.out.println("Second Request, Headers:");
        for( Header h : response2.getAllHeaders()) System.out.println("    " + h.getName() + ": " + h.getValue());

        System.out.println("Second Request Completed.");

        response2.getStatusLine()

        wms.stop();
    }
}
