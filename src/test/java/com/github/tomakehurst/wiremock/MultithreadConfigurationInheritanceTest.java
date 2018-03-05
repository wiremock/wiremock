package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

public class MultithreadConfigurationInheritanceTest {

    private static WireMockServer wireMockServer;

    @BeforeClass
    public static void setup(){
        wireMockServer = new WireMockServer(8082);
        wireMockServer.start();
        WireMock.configureFor(8082);
    }


    @AfterClass
    public static void shutdown(){
        wireMockServer.shutdown();
    }


    @Test(timeout = 5000) //Add a timeout so the test will execute in a new thread
    public void verifyConfigurationInherited(){
        //Make a call to the wiremock server. If this doesn't call to 8082 this will fail
        //with an exception
        stubFor(any(urlEqualTo("/foo/bar")).willReturn(aResponse().withStatus(200)));
    }
}
