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

import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;

public class FaultInjectingHttpConnectionFactory extends HttpConnectionFactory {

    private final RequestDelayControl requestDelayControl;

    public FaultInjectingHttpConnectionFactory(RequestDelayControl requestDelayControl) {
        super();
        this.requestDelayControl = requestDelayControl;
    }

    public FaultInjectingHttpConnectionFactory(
            HttpConfiguration httpConfig,
            RequestDelayControl requestDelayControl) {
        super(httpConfig);
        this.requestDelayControl = requestDelayControl;
    }

    @Override
    public Connection newConnection(Connector connector, EndPoint endPoint) {
        requestDelayControl.delayIfRequired();
        return configure(
                new FaultInjectingHttpConnection(
                        getHttpConfiguration(),
                        connector,
                        endPoint
                ),
                connector,
                endPoint
        );
    }

}
