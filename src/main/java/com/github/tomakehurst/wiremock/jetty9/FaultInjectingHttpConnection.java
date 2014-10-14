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
        if (info.getHttpFields().contains(Fault.class.getName(), Fault.EMPTY_RESPONSE.name())) {
            close();
            callback.succeeded();
//        } else if (info.getHttpFields().contains("InjectFault", Fault.RANDOM_DATA_THEN_CLOSE.name())) {
        } else {
            super.send(
                    info,
                    content,
                    lastContent,
                    callback
            );
        }
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
