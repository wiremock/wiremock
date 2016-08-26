package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.http.RequestMethod;

public interface Router {

    void add(RequestMethod method, String urlTemplate, Class<? extends AdminTask> task);
    void add(RequestMethod method, String urlTemplate, AdminTask adminTask);
}
