package com.github.tomakehurst.wiremock.jetty9;

import com.github.tomakehurst.wiremock.core.FaultInjector;
import com.google.common.base.Charsets;
import org.eclipse.jetty.io.ChannelEndPoint;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class JettyFaultInjector implements FaultInjector {

    private final Response response;
    private final Socket socket;

    public JettyFaultInjector(HttpServletResponse response) {
        this.response = (Response) response;
        this.socket = socket();
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
            byte[] stuff = "lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes(Charsets.UTF_8);
            socket.getChannel().write(BufferUtil.toBuffer(stuff));
            socket.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }

    }

    @Override
    public void randomDataAndCloseConnection() {
        try {
//            response.getHttpOutput().sendContent(new ByteArrayInputStream("lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes(Charsets.UTF_8)));
            byte[] stuff = "lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes(Charsets.UTF_8);
            socket.getChannel().write(BufferUtil.toBuffer(stuff));
            socket.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    private Socket socket() {
        ChannelEndPoint ep = (ChannelEndPoint) response.getHttpOutput().getHttpChannel().getEndPoint();
        return ep.getSocket();
    }

}
