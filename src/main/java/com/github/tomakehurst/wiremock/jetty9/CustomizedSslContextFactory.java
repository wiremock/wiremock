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
package com.github.tomakehurst.wiremock.jetty9;


import javax.net.ssl.*;

/**
 * Created by agupt13 on 6/9/16.
 *
 * This class is customized because the Android JVM classes do not support the `setEndpointIdentificationAlgorithm` for https communication.
 */
public class CustomizedSslContextFactory extends org.eclipse.jetty.util.ssl.SslContextFactory  {



    public void customize(SSLEngine sslEngine)
    {
        SSLParameters sslParams = sslEngine.getSSLParameters();
        // sslParams.setEndpointIdentificationAlgorithm(_endpointIdentificationAlgorithm);
        sslEngine.setSSLParameters(sslParams);

        if (super.getWantClientAuth())
            sslEngine.setWantClientAuth(super.getWantClientAuth());
        if (super.getNeedClientAuth())
            sslEngine.setNeedClientAuth(super.getNeedClientAuth());

        sslEngine.setEnabledCipherSuites(super.selectCipherSuites(
                sslEngine.getEnabledCipherSuites(),
                sslEngine.getSupportedCipherSuites()));

        sslEngine.setEnabledProtocols(super.selectProtocols(sslEngine.getEnabledProtocols(),sslEngine.getSupportedProtocols()));
    }



}