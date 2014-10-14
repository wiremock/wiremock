package com.github.tomakehurst.wiremock.jetty9;

import com.github.tomakehurst.wiremock.core.FaultInjector;
import org.eclipse.jetty.util.Callback;

public class JettyFaultInjector implements FaultInjector {

    private final FaultInjectingHttpConnection faultInjectingHttpConnection;
    private final Callback callback;

    public JettyFaultInjector(FaultInjectingHttpConnection faultInjectingHttpConnection, Callback callback) {
        this.faultInjectingHttpConnection = faultInjectingHttpConnection;
        this.callback = callback;
    }

    @Override
    public void emptyResponseAndCloseConnection() {
        faultInjectingHttpConnection.close();
        callback.succeeded();
    }

    @Override
    public void malformedResponseChunk() {

    }

    @Override
    public void randomDataAndCloseConnection() {

    }
}
