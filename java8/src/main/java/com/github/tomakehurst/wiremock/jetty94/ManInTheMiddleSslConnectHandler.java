package com.github.tomakehurst.wiremock.jetty94;

import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.eclipse.jetty.http.HttpMethod.CONNECT;

/**
 * A Handler for the HTTP CONNECT method that, instead of opening up a
 * TCP tunnel between the downstream and upstream sockets, turns the connection
 * into an SSL connection allowing this server to handle it.
 */
class ManInTheMiddleSslConnectHandler extends AbstractHandler {

    private final SslConnectionFactory sslConnectionFactory;

    ManInTheMiddleSslConnectHandler(SslConnectionFactory sslConnectionFactory) {
        this.sslConnectionFactory = sslConnectionFactory;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        sslConnectionFactory.start();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        sslConnectionFactory.stop();
    }

    @Override
    public void handle(
        String target,
        Request baseRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        if (CONNECT.is(request.getMethod())) {
            baseRequest.setHandled(true);
            handleConnect(baseRequest, response);
        }
    }

    private void handleConnect(
        Request baseRequest,
        HttpServletResponse response
    ) throws IOException {
        sendConnectResponse(response);

        HttpChannel httpChannel = baseRequest.getHttpChannel();
        Connector connector = httpChannel.getConnector();
        EndPoint endpoint = httpChannel.getEndPoint();
        endpoint.setConnection(null);

        Connection connection = sslConnectionFactory.newConnection(connector, endpoint);
        endpoint.setConnection(connection);

        endpoint.onOpen();
        connection.onOpen();
    }

    private void sendConnectResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getOutputStream().close();
    }
}
