package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.junit.Test;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.SocketAcceptDelaySpec.ofMilliseconds;
import static org.apache.http.client.params.ClientPNames.HANDLE_REDIRECTS;

public class SocketAcceptDelayAcceptanceTest extends AcceptanceTestBase {

    @Test(expected=SocketTimeoutException.class)
    public void addsDelayToSocketAcceptanceForDefinedNumberOfRequests() throws Exception {
        WireMock.addSocketAcceptDelay(ofMilliseconds(3000).forNumRequests(1));

        ClientConnectionManager conMan = new SingleClientConnManager();
        conMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
        HttpClient client = new DefaultHttpClient(conMan);
        HttpParams params = client.getParams();
        params.setParameter(HANDLE_REDIRECTS, false);
        HttpConnectionParams.setSoTimeout(params, 300);

        HttpClient httpClient = HttpClientFactory.createClient(50, 300);
        HttpGet get = new HttpGet("http://localhost:8080/anything");

        System.out.println("Getting...");
        httpClient.execute(get);
        System.out.println("Got");
    }
}
