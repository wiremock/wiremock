package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

public abstract class PostServeAction implements Extension {

    /**
     * Do something after a request has been served.
     * Called when this extension is applied to a specific stub mapping.
     * @param serveEvent the serve event, including the request and the response definition
     * @param admin WireMock's admin functions
     * @param parameters the parameters passed to the extension from the stub mapping
     */
    public void doAction(ServeEvent serveEvent,
                         Admin admin,
                         Parameters parameters
    ) {};

    /**
     * Do something after a request has been served.
     * Called when this extension is applied to a specific stub mapping.
     * @param serveEvent the serve event, including the request and the response definition
     * @param admin WireMock's admin functions
     */
    public void doGlobalAction(ServeEvent serveEvent,
                               Admin admin
    ) {};
}
