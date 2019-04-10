package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.global.GlobalSettings;

public class GetGlobalSettingsResult {

    private final GlobalSettings settings;

    public GetGlobalSettingsResult(@JsonProperty("settings") GlobalSettings settings) {
        this.settings = settings;
    }

    public GlobalSettings getSettings() {
        return settings;
    }
}
