package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class GlobalSettingsUpdateTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        GlobalSettings newSettings = Json.read(request.getBodyAsString(), GlobalSettings.class);
        admin.updateGlobalSettings(newSettings);
        return ResponseDefinition.ok();
    }
}
