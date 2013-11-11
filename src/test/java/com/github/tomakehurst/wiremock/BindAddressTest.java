package com.github.tomakehurst.wiremock;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;

public class BindAddressTest {

    private final int port = 8090;
    private String bindAddress = "127.0.0.1";
    private String nonBindAddress;
    private WireMockServer wireMockServer;

    @Test
    public void shouldRespondInTheBindAddressOnly() throws Exception {
        executeGetIn(bindAddress);
        try {
            executeGetIn(nonBindAddress);
            Assert.fail("Should not accept the connection in [" + nonBindAddress + "]");
        } catch (Exception ex) {
        }
    }

    @Before
    public void prepare() throws Exception {
        nonBindAddress = getIpAddressDifferentThan(bindAddress);
        if (nonBindAddress == null)
            Assert.fail("Impossible to validate the binding address. This machine has only a one Ip address ["
                    + bindAddress + "]");

        WireMockConfiguration cfg = new WireMockConfiguration();
        cfg.biddingAddress(bindAddress);
        cfg.port(port);

        wireMockServer = new WireMockServer(cfg);
        wireMockServer.start();
    }

    @After
    public void stop() {
        if (wireMockServer != null)
            wireMockServer.stop();
    }

    private void executeGetIn(String address) {
        WireMockTestClient wireMockClient = new WireMockTestClient(port, address);
        wireMockClient.addResponse(MappingJsonSamples.BASIC_MAPPING_REQUEST_WITH_RESPONSE_HEADER);
        WireMockResponse response = wireMockClient.get("/a/registered/resource");
        assertThat(response.statusCode(), is(401));
    }

    private String getIpAddressDifferentThan(String lopbackAddress) throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netInterface : Collections.list(networkInterfaces)) {
            Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
            for (InetAddress address : Collections.list(inetAddresses))
                if (address instanceof Inet4Address && !address.getHostAddress().equals(lopbackAddress)) {
                    return address.getHostAddress();
                }
        }
        return null;
    }
}
