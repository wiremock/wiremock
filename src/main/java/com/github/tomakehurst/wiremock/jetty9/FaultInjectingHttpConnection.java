package com.github.tomakehurst.wiremock.jetty9;

import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnection;

public class FaultInjectingHttpConnection extends HttpConnection {

    public FaultInjectingHttpConnection(
            HttpConfiguration config,
            Connector connector,
            EndPoint endPoint) {
        super(
                config,
                connector,
                endPoint
        );
    }
}
