package org.wiremock.grpc;

import io.grpc.servlet.jakarta.GrpcServlet;
import io.grpc.servlet.jakarta.ServletAdapter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

public class GrpcFilter extends HttpFilter {

    private final GrpcServlet grpcServlet;

    public GrpcFilter() {
        grpcServlet = new GrpcServlet(Collections.emptyList());
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!ServletAdapter.isGrpc(request)) {
            chain.doFilter(request, response);
            return;
        }

        grpcServlet.service(request, response);
    }
}
