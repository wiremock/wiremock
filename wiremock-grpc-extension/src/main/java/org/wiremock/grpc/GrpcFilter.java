package org.wiremock.grpc;

import com.example.grpc.GreetingServiceGrpc;
import com.example.grpc.HelloRequest;
import com.example.grpc.HelloResponse;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import io.grpc.servlet.jakarta.GrpcServlet;
import io.grpc.servlet.jakarta.ServletAdapter;
import io.grpc.stub.StreamObserver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GrpcFilter extends HttpFilter {

    private final GrpcServlet grpcServlet;
    private final StubRequestHandler stubRequestHandler;

    public GrpcFilter(StubRequestHandler stubRequestHandler) {
        this.stubRequestHandler = stubRequestHandler;
        grpcServlet = new GrpcServlet(List.of(new GreetingServiceGrpc.GreetingServiceImplBase() {
            @Override
            public void greeting(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
                String name = request.getName();
                responseObserver.onNext(HelloResponse.newBuilder().setGreeting("Hi " + name).build());
                responseObserver.onCompleted();
            }
        }));
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
