package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.extension.Parameters;

public class ExtendedSettingsWrapper {

    private final Parameters extended;

    public ExtendedSettingsWrapper(@JsonProperty("extended") Parameters extended) {
        this.extended = extended;
    }

    public Parameters getExtended() {
        return extended;
    }
}
