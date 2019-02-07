package com.github.tomakehurst.wiremock.servlet;

import javax.servlet.http.HttpServletRequest;

public interface MultipartRequestConfigurer {

    String KEY = MultipartRequestConfigurer.class.getSimpleName();

    void configure(HttpServletRequest request);
}
