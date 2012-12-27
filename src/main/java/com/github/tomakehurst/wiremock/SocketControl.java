package com.github.tomakehurst.wiremock;

/**
 * Created with IntelliJ IDEA.
 * User: tomakehurst
 * Date: 18/12/2012
 * Time: 20:13
 * To change this template use File | Settings | File Templates.
 */
public interface SocketControl {
    void setDelay(int requestCount, long milliseconds);

    void delayIfRequired() throws InterruptedException;
}
