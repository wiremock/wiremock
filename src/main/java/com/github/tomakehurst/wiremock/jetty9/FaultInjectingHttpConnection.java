package com.github.tomakehurst.wiremock.jetty9;

import com.github.tomakehurst.wiremock.http.Fault;
import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Callback;

import java.io.IOException;
import java.nio.ByteBuffer;

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

//    protected FaultInjectingHttpChannelOverHttp newHttpChannel(HttpInput<ByteBuffer> httpInput) {
//        return new FaultInjectingHttpChannelOverHttp(
//                getConnector(),
//                getHttpConfiguration(),
//                getEndPoint(),
//                this,
//                httpInput
//        );
//    }

    public void send(HttpGenerator.ResponseInfo info, ByteBuffer content, boolean lastContent, Callback callback) {
        String faultName = info.getHttpFields().get(Fault.class.getName());
        if (faultName != null) {
            Fault.valueOf(faultName).apply(
                    new JettyFaultInjector(
                            this,
                            callback
                    )
            );
        }
        super.send(
                info,
                content,
                lastContent,
                callback
        );
    }

//    public class FaultInjectingHttpChannelOverHttp extends HttpConnection.HttpChannelOverHttp {
//        public FaultInjectingHttpChannelOverHttp(
//                Connector connector,
//                HttpConfiguration config,
//                EndPoint endPoint,
//                HttpTransport transport,
//                HttpInput<ByteBuffer> input) {
//            super(
//                    connector,
//                    config,
//                    endPoint,
//                    transport,
//                    input
//            );
//        }
//    }
}
