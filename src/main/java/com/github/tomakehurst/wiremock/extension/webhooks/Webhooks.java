package com.github.tomakehurst.wiremock.extension.webhooks;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

public class Webhooks extends PostServeAction {

    @Override
    public String getName() {
        return "webhooks";
    }

    @Override
    public void doAction(ServeEvent serveEvent, Admin admin, Parameters parameters) {

    }

    public static WebhookDefinition webhook() {
        return new WebhookDefinition();
    }
}
