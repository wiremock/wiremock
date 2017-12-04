package com.github.tomakehurst.wiremock.verification.notmatched;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public abstract class NotMatchedRenderer implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        return render(admin, request);
    }

    protected abstract ResponseDefinition render(Admin admin, Request request);
}
