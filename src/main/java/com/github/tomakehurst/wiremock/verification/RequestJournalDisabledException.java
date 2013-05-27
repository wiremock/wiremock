package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.core.ConfigurationException;

public class RequestJournalDisabledException extends ConfigurationException {

    public RequestJournalDisabledException() {
        super("The request journal is disabled, so no verification or request searching operations are available");
    }
}
