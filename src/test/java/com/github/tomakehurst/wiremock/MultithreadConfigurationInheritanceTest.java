/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
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
