package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.core.FaultInjector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NoFaultInjectorFactory implements FaultInjectorFactory {

    @Override
    public FaultInjector buildFaultInjector(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return new NoFaultInjector(httpServletResponse);
    }
}
