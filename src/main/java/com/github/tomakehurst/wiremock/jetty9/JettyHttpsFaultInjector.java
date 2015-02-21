package com.github.tomakehurst.wiremock.jetty9;

import com.github.tomakehurst.wiremock.core.FaultInjector;
import com.google.common.base.Charsets;
import org.eclipse.jetty.io.SelectChannelEndPoint;
import org.eclipse.jetty.io.ssl.SslConnection;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ByteChannel;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class JettyHttpsFaultInjector implements FaultInjector {

    private static final byte[] GARBAGE = "lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes(Charsets.UTF_8);

    private final Response response;
    private final Socket socket;
    private final ByteChannel channel;

    public JettyHttpsFaultInjector(HttpServletResponse response) {
        this.response = (Response) response;

        HttpChannel<?> httpChannel = this.response.getHttpOutput().getHttpChannel();
        SslConnection.DecryptedEndPoint sslEndpoint = (SslConnection.DecryptedEndPoint) httpChannel.getEndPoint();
        SelectChannelEndPoint selectChannelEndPoint = (SelectChannelEndPoint) sslEndpoint.getSslConnection().getEndPoint();
        this.socket = selectChannelEndPoint.getSocket();
        this.channel = selectChannelEndPoint.getChannel();
    }

    @Override
    public void emptyResponseAndCloseConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    @Override
    public void malformedResponseChunk() {
        try {
            response.setStatus(200);
            response.flushBuffer();
            writeGarbageThenCloseSocket();
        } catch (IOException e) {
            throwUnchecked(e);
        }

    }

    @Override
    public void randomDataAndCloseConnection() {
        writeGarbageThenCloseSocket();
    }

    private void writeGarbageThenCloseSocket() {
        response.getHttpOutput().getHttpChannel().getEndPoint().write(new Callback() {
            @Override
            public void succeeded() {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable x) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, BufferUtil.toBuffer(GARBAGE));
    }

}
