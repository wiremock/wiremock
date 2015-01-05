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
