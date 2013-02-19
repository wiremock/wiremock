package com.github.tomakehurst.wiremock.jetty;

import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.log.Log;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.Socket;

public class DelayableSslSocketConnector extends SslSocketConnector {

    private final RequestDelayControl requestDelayControl;

    public DelayableSslSocketConnector(RequestDelayControl requestDelayControl) {
        this.requestDelayControl = requestDelayControl;
    }

    @Override
    public void accept(int acceptorID) throws IOException, InterruptedException
    {
        try
        {
            final Socket socket = _serverSocket.accept();

            try {
                requestDelayControl.delayIfRequired();
            } catch (InterruptedException e) {
                if (!(isStopping() || isStopped())) {
                    Thread.interrupted(); // Clear the interrupt flag on the current thread
                }
            }

            configure(socket);
            Connection connection = new SslConnection(socket) {
                @Override
                public void run() {
                    ActiveSocket.set(socket);
                    super.run();
                    ActiveSocket.clear();
                }
            };
            connection.dispatch();
        }
        catch(SSLException e)
        {
            Log.warn(e);
            try
            {
                stop();
            }
            catch(Exception e2)
            {
                Log.warn(e2);
                throw new IllegalStateException(e2.getMessage());
            }
        }
    }
}
